package db.tvmaze;

import db.BaseTvInfoTests;
import db.TvInfoSource;

public class TvMazeTests extends BaseTvInfoTests {

    @Override
    protected TvInfoSource givenDb() throws Exception {
        return new TvMazeDataSource(folder.newFolder());
    }

}
