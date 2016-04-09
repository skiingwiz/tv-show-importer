package main;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.Banner;
import data.Episode;
import db.DatabaseProcessingException;
import db.thetvdb.TheTvDbDatabase;

//TODO handle replacing previous files when there is a PROPER file downloaded
public class Main {
	private static TheTvDbDatabase db;

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) throws Exception {
	    new Main().process(args);
	}

	private void process(String[] args) throws Exception {
	    log.info("Invoked with args {}", (Object)args);
		GlobalConfig.parse(args);

		if(GlobalConfig.getOptions().getBoolean(GlobalConfig.VERSION)) {
			System.out.println(Version.getFullNameVersion());
			System.exit(0);
		}

		String lang = GlobalConfig.getOptions().getString(GlobalConfig.LANGUAGE);
		db = new TheTvDbDatabase(lang);

		if(GlobalConfig.getOptions().getBoolean(GlobalConfig.CLEAR_CACHE)) {
            db.clearCaches();
        }

		db.initialize();

		boolean largestFile = GlobalConfig.getOptions().getBoolean(GlobalConfig.LARGEST_FILE_IN_DIR);

		for(String s : GlobalConfig.getOptions().getPositionals()) {
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
				if(GlobalConfig.getOptions().getBoolean(GlobalConfig.RECURSE)) {
					for(File sf : f.listFiles()) {
						processFile(sf);
					}
				} else {
					log.info("Given file is a directory and there is no recursion.  File: {}", f.getName());
				}
			} else {
				boolean process = true;
				String[] exEndings = GlobalConfig.getOptions().getString(GlobalConfig.EXCLUDED_ENDINGS).split("\\|");
				for(String s : exEndings) {
					if(f.getName().endsWith(s)) {
						process = false;

						log.debug("Skipping file because of ending.  File: {} Ending: {}", f.getName(), s);
						break;
					}
				}

				if(process) {
					try {
						Episode e = FileParser.parse(f);
						e = db.lookup(e.getSeries().getName(), e.getSeasonNum(), e.getEpisodeNum());
						if(e != null) {
							e.setOriginalFile(f);
							if(GlobalConfig.getOptions().getBoolean(GlobalConfig.RENAME)) {
								String renamePattern = GlobalConfig.getOptions().getString(GlobalConfig.RENAME_PATTERN);
								f = renameFile(f, e, renamePattern);
							}

							if(GlobalConfig.getOptions().getBoolean(GlobalConfig.PROPERTIES_FILE)) {
								writeFile(f, e);
							}

							if(GlobalConfig.getOptions().getBoolean(GlobalConfig.FANART)) {
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
		String fanartDir = GlobalConfig.getOptions().getString(GlobalConfig.FANART_DIR);
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
			    log.info("Skipping banner because it is of undesired type. Banner: {} Type: {} Type2: {}",
			            b.getBannerName(), b.getBannerType(), b.getBannerType2());
				continue;
			}

			log.info("Downloading banner {}", b.getBannerName());
			try {
				db.downloadBanner(b, dir.toString());
			} catch(DatabaseProcessingException dbe) {
				//TODO make a separate exception that inherits from DatabaseProcessingException
				log.error("Failure downloading banner {} to {}", b, dir, dbe);
			}
		}
	}

	private File renameFile(File f, Episode e, String pattern) throws FailedRenameException {
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



		File newFile = persistantRename(f, newName);

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
                log.warn("Failed to move file {} on try number {}", file, tries, e);
                try {
                    Thread.sleep(30_000);
                } catch (InterruptedException ie) {
                    log.error("Sleep interrupted", ie);
                    Thread.currentThread().interrupt();
                }
            }
	    }

	    if(!moved) {
	        log.error("Failed to move file {} to directory {} after {} tries", file, directory, numTries);
	        throw new IOException("Failed to move file");
	    }
	}

	private File persistantRename(File f, String newName) throws FailedRenameException {
	    int numTries = 10;
	    File newFile = new File(newName);
	    boolean success = false;

	    for(int tries = 1; tries <= numTries && !success; tries++) {
	        success = f.renameTo(newFile);
	        if(!success) {
	            try {
                    Thread.sleep(30_000);
                } catch (InterruptedException ie) {
                    log.error("Sleep interrupted", ie);
                    Thread.currentThread().interrupt();
                }
	        }
	    }

	    if(success) {
	        log.info("File renaming successful.");
	    } else {
	        log.error("Failed to rename {} to {} after {} tries", f, newFile, numTries);
	        throw new FailedRenameException(f, newFile);
	    }

	    return newFile;
	}
}
