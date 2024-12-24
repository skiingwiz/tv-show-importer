package db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.GlobalConfig;
import data.Series;

public class SeriesCache {
    private static final String SERIES_ID_FILE = "series_ids.dat";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Properties seriesIds;
    private final File cacheDir;
    private final File seriesIdFile;
    private final ObjectMapper om;

    public SeriesCache(String cacheName) {
        String cacheDirStr = GlobalConfig.get().getString(GlobalConfig.CACHE_DIR);
        cacheDir = new File(cacheDirStr, cacheName);
        seriesIdFile = new File(cacheDir, SERIES_ID_FILE);
        om = new ObjectMapper();
    }

    public SeriesCache(String cacheName, File cacheDir) throws DatabaseInitializationException {
        this.cacheDir = cacheDir;
        seriesIdFile = new File(cacheDir, SERIES_ID_FILE);
        om = new ObjectMapper();
        initialize();
    }


    public String getSeriesId(String seriesName) {
        if(seriesIds == null) {
            seriesIds = new Properties();
            if(seriesIdFile.exists()) {
                log.debug("Loading Series ID cache from ", seriesIdFile.getAbsolutePath());

                try (InputStream in = FileUtils.openInputStream(seriesIdFile)){
                    seriesIds.load(in);
                } catch (IOException e) {
                    log.error("Could not load Series ID file {}.  Updates will not be written to disk.",
                            seriesIdFile.getAbsolutePath(), e);
                    return null;
                }

            } else {
                log.warn("Series ID cache file not found at {}", seriesIdFile.getAbsolutePath());
                return null;
            }
        }

        return seriesIds.getProperty(seriesName);
    }

    public void storeSeriesId(String seriesName, String id) {
        seriesIds.put(seriesName, id);
        try {
            seriesIds.store(new BufferedWriter(new FileWriter(seriesIdFile)), "");
        } catch (IOException e) {
            log.error("Could not save series id file {}.  Updates will not be written to disk.",
                    seriesIdFile.getAbsolutePath(), e);
        }

    }

    public Series getSeries(String id) {
        File seriesFile = getSeriesFile(id);

        Series series = null;
        if(seriesFile.exists()) {
            try {
                series = om.readValue(seriesFile, Series.class);
                series.setOriginalName(series.getName());
            } catch (IOException e) {
                log.error("Failed to parse series info JSON file {}", seriesFile, e);
            }
        }

        return series;
    }


    public void storeSeries(Series series) {
        if(!seriesIds.containsKey(series.getId())) {
            storeSeriesId(series.getName(), series.getId());
        }

        File seriesFile = getSeriesFile(series.getId());
        try {
            om.writeValue(seriesFile, series);
        } catch (IOException e) {
            log.error("Failed to store series data {}", series);
        }
    }


    public void clear() {
        if(cacheDir.exists()) {
            for(File f : cacheDir.listFiles()) {
                FileUtils.deleteQuietly(f);
            }
        }
    }


    public void initialize() throws DatabaseInitializationException {
        if(!cacheDir.exists()) {
            if(!cacheDir.mkdirs()) {
                throw new DatabaseInitializationException("Faile to create cache directory: " + cacheDir);
            }
        }
    }

    private File getSeriesFile(String id) {
        return new File(cacheDir,  id + ".json");
    }
}
