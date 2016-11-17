package main;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.GlobalConfig;
import data.Banner;
import data.Episode;
import db.DatabaseProcessingException;
import db.thetvdb.TheTvDbDatabase;
import util.ThreadUtils;

public class Main {
    private static TheTvDbDatabase db;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception {
        new Main().process(args);
    }

    private void process(String[] args) throws Exception {
        log.info("Invoked with args {}", (Object)args);
        List<String> positionals = GlobalConfig.parse(args);

        if(GlobalConfig.get().getBoolean(GlobalConfig.PRINT_VERSION)) {
            System.out.println(Version.getFullNameVersion());
            System.exit(0);
        }

        String lang = GlobalConfig.get().getString(GlobalConfig.LANGUAGE);
        db = new TheTvDbDatabase(lang);

        if(GlobalConfig.get().getBoolean(GlobalConfig.CLEAR_CACHE)) {
            db.clearCaches();
        }

        db.initialize();

        boolean largestFile = GlobalConfig.get().getBoolean(GlobalConfig.LARGEST_FILE_IN_DIR);

        for(String s : positionals) {
            File file = new File(s);
            if(largestFile) {
                file = preprocessLargestFile(file);
            }
            processFile(file);
        }
    }

    private void processFile(File f) {
        if(f.exists()) {
            if(f.isDirectory()) {
                if(GlobalConfig.get().getBoolean(GlobalConfig.RECURSE)) {
                    for(File sf : f.listFiles()) {
                        processFile(sf);
                    }
                } else {
                    log.info("Given file is a directory and there is no recursion.  File: {}", f.getName());
                }
            } else {
                boolean process = true;
                //TODO a way to pile these up in configs (Maybe use +=)
                List<String> exPatterns = GlobalConfig.get().getStringList(GlobalConfig.EXCLUDE_PATTERNS);
                for(String regex : exPatterns) {
                    if(Pattern.compile(regex).matcher(f.getAbsolutePath()).find()) {
                        process = false;

                        log.debug("Skipping file because of exclusion pattern.  File: {} Pattern: {}",
                                f.getAbsolutePath(), regex);
                        break;
                    }
                }

                if(process) {
                    try {
                        Episode e = FileParser.parse(f);
                        e = db.lookup(e.getSeries().getName(), e.getSeasonNum(), e.getEpisodeNum());
                        if(e != null) {
                            e.setOriginalFile(f);
                            if(GlobalConfig.get().getBoolean(GlobalConfig.RENAME)) {
                                String renamePattern = GlobalConfig.get().getString(GlobalConfig.RENAME_PATTERN);
                                boolean replaceFile = f.getName().matches("(?i).*proper.*");
                                f = renameFile(f, e, renamePattern, replaceFile);
                            }

                            if(GlobalConfig.get().getBoolean(GlobalConfig.PROPERTIES_FILE)) {
                                writeFile(f, e);
                            }

                            if(GlobalConfig.get().getBoolean(GlobalConfig.FANART)) {
                                storeFanart(e);
                            }
                        }
                    } catch(DatabaseProcessingException | FailedRenameException e) {
                        log.error("Skipping file due to error.  file: {}", f.getPath(), e);
                    }
                }
            }
        } else {
            log.error("File: {} does not exist.", f.getPath());
        }
    }

    private void storeFanart(Episode e) throws DatabaseProcessingException {
        String fanartDir = GlobalConfig.get().getString(GlobalConfig.FANART_DIR);
        if(fanartDir == null) {
            log.error("Fanart is enabled, but no directory is specified.  Fanart will NOT be downloaded");
            return;
        }

        List<Banner> banners = db.getBannerInfo(e.getSeries().getName());
        for(Banner b : banners) {
            StringBuffer dir = new StringBuffer(fanartDir);
            if(!fanartDir.endsWith(File.separator)) {
                dir.append(File.separator);
            }
            dir.append(e.getSeries().getName()).append(File.separator);

            if(b.getSeason() != null) {
                int season = 0;
                try {
                    season = Integer.parseInt(b.getSeason());
                } catch(NumberFormatException nfe) {
                    log.error("Problem determining season for banner.  Season reported as: {}",
                            b.getSeason(), nfe);
                    continue;
                }

                if(season == e.getSeasonNum()) {
                    dir.append("Season ").append(e.getSeasonNum());
                    dir.append(File.separator);
                } else {
                    continue;
                }
            }

            if(b.getBannerType().equalsIgnoreCase("season") ||
               b.getBannerType().equalsIgnoreCase("seasonwide") ||
               b.getBannerType().equalsIgnoreCase("poster")) {
                dir.append("Posters").append(File.separator);
            } else if(b.getBannerType().equalsIgnoreCase("fanart")) {
                dir.append("Backgrounds").append(File.separator);
            } else if(b.getBannerType().equalsIgnoreCase("series") &&
                    b.getBannerType2().equalsIgnoreCase("graphical")) {
                dir.append("Banners").append(File.separator);
            } else {
                log.debug("Skipping banner because it is of undesired type. Banner: {} Type: {} Type2: {}",
                        b.getBannerName(), b.getBannerType(), b.getBannerType2());
                continue;
            }

            log.debug("Downloading banner {}", b.getBannerName());
            try {
                db.downloadBanner(b, dir.toString());
            } catch(DatabaseProcessingException dbe) {
                //TODO make a separate exception that inherits from DatabaseProcessingException
                log.error("Failure downloading banner {} to {}", b, dir, dbe);
            }
        }
    }

    private File renameFile(File f, Episode e, String pattern, boolean replaceFile) throws FailedRenameException {
        //TODO option to specify decimal format in variable e.g. %season-num{00}%
        DecimalFormat df = new DecimalFormat("00");

        String newName = new String(pattern);

        newName = newName.replaceAll("%season-num%", df.format(e.getSeasonNum()));
        newName = newName.replaceAll("%episode-num%", df.format(e.getEpisodeNum()));
        newName = newName.replaceAll("%episode-title%",
                Matcher.quoteReplacement(e.getEpisodeTitle()));
        //get rid of unusable characters
        newName = newName.replaceAll("[/\\\\:?!;*]", " ");

        File parentFile = f.getParentFile();
        if(parentFile != null) {
            //TODO generic option to set path for file moving. Allow variable substitution
            if(!parentFile.getName().toLowerCase().startsWith("season")) {
                //assume that if it starts with "season" then it is a season folder
                String parent = parentFile.getPath();
                if(!parent.endsWith(File.separator)) {
                    parent = parent + File.separator;
                }

                parentFile = new File(parent + "Season " + e.getSeasonNum());
                if(!parentFile.exists() && !parentFile.mkdir()) {
                    throw new RuntimeException("Failed to make directory: " + parentFile.getAbsolutePath());
                }
            }

            String parent = parentFile.getPath();
            if(!parent.endsWith(File.separator)) {
                parent = parent + File.separator;
            }

            newName = parent + newName;
        }

        String oldName = f.getName();
        //preserve the original file extension
        newName += oldName.substring(oldName.lastIndexOf("."));



        File newFile = persistantRename(f, newName, replaceFile);

        //update the last mod time to be sure sage re-import this
        newFile.setLastModified(System.currentTimeMillis());
        return newFile;
    }

    private void writeFile(File f, Episode e) {
        try {
            new PropertiesFileWriter().writeFile(f, e);
        } catch (IOException ioe) {
            log.error("Could not write episode {} properties file {}", e, f, ioe);
        }
    }

    private File preprocessLargestFile(File file) throws IOException {
        log.debug("Processing Largest File {}", file);
        File dir = file.isDirectory() ? file : file.getParentFile();
        log.info("Looking for largest file in {}", dir);

        File largestFile = null;
        BigInteger largestSize = BigInteger.ZERO;

        for(File child : dir.listFiles((FileFilter)FileFilterUtils.fileFileFilter())) {
            BigInteger size = FileUtils.sizeOfAsBigInteger(child);
            log.debug("Looking at file {} with size {}", child, size);
            if(size.compareTo(largestSize) > 0) {
                largestFile = child;
                largestSize = size;
            }
        }

        if(largestFile == null) {
            log.error("Internal Error calculating largest file");
            throw new IllegalStateException("Internal Error calculating largest file");
        }

        log.info("Found largest file {}", largestFile);

        try {
            persistantMove(largestFile, dir.getParentFile());
            largestFile = new File(dir.getParentFile(), largestFile.getName());
        } catch (IOException e) {
            log.error("Failed to move file: {}", largestFile.getAbsolutePath(), e);
            throw e;
        }

        FileUtils.deleteQuietly(dir);

        return largestFile;
    }

    private void persistantMove(File file, File directory) throws IOException {
        int numTries = 10;
        boolean moved = false;
        for(int tries = 1; tries <= numTries && !moved; tries++) {
            try {
                FileUtils.moveFileToDirectory(file, directory, false);
                moved = true;
            } catch (IOException e) {
                log.debug("Failed to move file {} on try number {}", file, tries, e);
                ThreadUtils.sleep(30_000);
            }
        }

        if(!moved) {
            log.error("Failed to move file {} to directory {} after {} tries", file, directory, numTries);
            throw new IOException("Failed to move file");
        }
    }

    private File persistantRename(File f, String newName, boolean replaceFile) throws FailedRenameException {
        int numTries = 10;
        File newFile = new File(newName);

        boolean success = false;

        for(int tries = 1; tries <= numTries && !success; tries++) {
            CopyOption[] options =
                    (replaceFile ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} :  new CopyOption[0]);
            try {
                Files.move(f.toPath(), newFile.toPath(), options);
                success = true;
            } catch(FileAlreadyExistsException e) {
                log.error("Can not move file {} because target {} exisits", f, newFile);
            } catch (IOException e) {
                log.debug("failed to rename {} to {}", f, newFile, e);
            }
            if(!success) {
                ThreadUtils.sleep(30_000);
            }
        }

        if(success) {
            log.info("File renaming successful: {}", newFile);
        } else {
            log.error("Failed to rename {} to {} after {} tries", f, newFile, numTries);
            throw new FailedRenameException(f, newFile);
        }

        return newFile;
    }
}
