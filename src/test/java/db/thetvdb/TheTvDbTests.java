package db.thetvdb;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import data.Banner;
import data.Episode;
import db.DatabaseInitializationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;

public class TheTvDbTests {
    private static final String LANG = "en";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static Date date(int year, int month, int date) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(year, month - 1, date);

        return cal.getTime();
    }

    //This is a vanilla test that should work out of the box
    @Test
    public void testBones5x1() throws Exception {
        TheTvDbDatabase db = givenDb();

        Episode e = db.lookup("Bones", 5, 1);

        assertNotNull("Episode is null", e);
        assertNotNull("Series is null", e.getSeries());
        assertEquals("Episode title does not match", "Harbingers in the Fountain", e.getEpisodeTitle());
        assertEquals("Season number does not match", (Integer)5, e.getSeasonNum());
        assertEquals("Episode number does not match", (Integer)1, e.getEpisodeNum());
        assertEquals("Network does not match", "FOX", e.getSeries().getNetwork());
        assertEquals("Air Date does not match", date(2009, 9, 17), e.getFirstAirDate());
        assertEquals("Air Day does not match", "Tuesday", e.getSeries().getAirDay());
        assertEquals("ID does not match", "75682", e.getSeries().getId());
    }

    //This is a test that requires a lookup override in the series id file
    //@Test
    public void testCastle5x1() throws Exception {
        TheTvDbDatabase db = givenDb();

        Episode e = db.lookup("Castle", 5, 1);
        assertNotNull("Episode not linked to Series", e.getSeries());
        assertEquals("Fanart name doesn't match", "Castle (2009)", e.getSeries().getOriginalName());
        assertEquals("Episode title does not match", "After The Storm", e.getEpisodeTitle());
        assertEquals("Season number does not match", (Integer)5, e.getSeasonNum());
        assertEquals("Episode number does not match", (Integer)1, e.getEpisodeNum());
        assertEquals("FOX", e.getSeries().getNetwork());
    }

    @Test
    public void testMom3x1() throws Exception {
        TheTvDbDatabase db = givenDb();

        Episode e = db.lookup("Mom", 3, 12);
        assertNotNull("Episode not linked to Series", e.getSeries());
        assertEquals("Fanart name doesn't match", "Mom", e.getSeries().getOriginalName());
        assertEquals("Episode title does not match", "Diabetic Lesbians and a Blushing Bride", e.getEpisodeTitle());
        assertEquals("Season number does not match", (Integer)3, e.getSeasonNum());
        assertEquals("Episode number does not match", (Integer)12, e.getEpisodeNum());
        assertEquals("CBS", e.getSeries().getNetwork());
        assertEquals("Air Date does not match", date(2016, 2, 18), e.getFirstAirDate());
        assertEquals("Air Day does not match", "Thursday", e.getSeries().getAirDay());
        assertEquals("ID does not match", "266967", e.getSeries().getId());
    }

    @Test
    public void testBannerInfo() throws Exception {
        TheTvDbDatabase db = givenDb();
        Episode e = db.lookup("Bones", 5, 1);

        List<Banner> banners = db.getBannerInfo(e.getSeries());

        assertNotNull(banners);
        assertFalse(banners.isEmpty());

        for(Banner b : banners) {
            System.out.println(b.getBannerPath());
            assertNotNull(b);
            assertNotNull(b.getBannerName());
            assertNotNull(b.getBannerPath());
            assertNotNull(b.getBannerType());
            assertNotNull(b.getBannerType2());

            if(b.getBannerType().startsWith("season")) {
                assertNotNull(b.getSeason());
                Integer.parseInt(b.getSeason());
            } else {
                assertNull(b.getSeason());
            }

        }
    }

    private TheTvDbDatabase givenDb() throws IOException, DatabaseInitializationException {
        TheTvDbDatabase db = new TheTvDbDatabase(LANG, folder.newFolder());
        db.initialize();

        return db;
    }
}
