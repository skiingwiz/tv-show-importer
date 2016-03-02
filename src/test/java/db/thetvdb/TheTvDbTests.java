package db.thetvdb;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import data.Episode;
import db.DatabaseInitializationException;

import static org.junit.Assert.*;

public class TheTvDbTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    //This is a vanilla test that should work out of the box
    @Test
    public void testBones5x1() throws Exception {
        TheTvDbDatabase db = givenDb();

        Episode e = db.lookup("Bones", 5, 1);

        assertEquals("Episode title does not match", "Harbingers in the Fountain", e.getEpisodeTitle());
        assertEquals("Season number does not match", 5, e.getSeasonNum());
        assertEquals("Episode number does not match", 1, e.getEpisodeNum());

        System.out.println(e);
    }

    //This is a test that requires a lookup override in the series id file
    //@Test
    public void testCastle5x1() throws Exception {
        TheTvDbDatabase db = givenDb();

        Episode e = db.lookup("Castle", 5, 1);
        assertNotNull("Episode not linked to Series", e.getSeries());
        assertEquals("Fanart name doesn't match", "Castle (2009)", e.getSeries().getOriginalName());
        assertEquals("Episode title does not match", "After The Storm", e.getEpisodeTitle());
        assertEquals("Season number does not match", 5, e.getSeasonNum());
        assertEquals("Episode number does not match", 1, e.getEpisodeNum());

        System.out.println(e);
    }

    @Test
    public void testMom3x1() throws Exception {
        TheTvDbDatabase db = givenDb();

        Episode e = db.lookup("Mom", 3, 12);
        assertNotNull("Episode not linked to Series", e.getSeries());
        assertEquals("Fanart name doesn't match", "Mom", e.getSeries().getOriginalName());
        assertEquals("Episode title does not match", "Diabetic Lesbians and a Blushing Bride", e.getEpisodeTitle());
        assertEquals("Season number does not match", 3, e.getSeasonNum());
        assertEquals("Episode number does not match", 12, e.getEpisodeNum());

        System.out.println(e);
    }

    private TheTvDbDatabase givenDb() throws IOException, DatabaseInitializationException {
        TheTvDbDatabase db = new TheTvDbDatabase("en", folder.newFolder());
        db.initialize();

        return db;
    }
}
