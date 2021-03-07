package db.thetvdb;

import java.io.IOException;

import org.junit.Ignore;

import db.BaseTvInfoTests;
import db.DatabaseInitializationException;
import db.TvInfoSource;

@Ignore
public class TheTvDbTests extends BaseTvInfoTests {
    private static final String LANG = "en";

    @Override
    protected TvInfoSource givenDb() throws IOException, DatabaseInitializationException {
        TheTvDbDatabase db = new TheTvDbDatabase(LANG, folder.newFolder());
        db.initialize();

        return db;
    }
}
