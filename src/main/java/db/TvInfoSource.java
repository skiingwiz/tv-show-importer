package db;

import data.Episode;

public interface TvInfoSource {

    void clearCaches();

    void initialize() throws DatabaseInitializationException;

    Episode lookup(String name, int seasonNum, int episodeNum) throws DatabaseProcessingException;
}
