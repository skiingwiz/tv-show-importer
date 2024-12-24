package db;

import db.tvmaze.TvMazeDataSource;

public class TvInfoSourceFactory {

    public static TvInfoSource getInstance(String lang) {
        return new TvMazeDataSource(); //TODO Lang
    }
}
