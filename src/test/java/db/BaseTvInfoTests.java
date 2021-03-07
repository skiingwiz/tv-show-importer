package db;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import data.Image;
import data.Episode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class BaseTvInfoTests {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static void dateCompare(int year, int month, int day, Date date, String field) {
        Calendar val = Calendar.getInstance();
        val.setTime(date);
        assertEquals(field + " year does no match", year, val.get(Calendar.YEAR));
        assertEquals(field + " month does no match", month - 1, val.get(Calendar.MONTH));
        assertEquals(field + " day does no match", day, val.get(Calendar.DAY_OF_MONTH));
    }

    //This is a vanilla test that should work out of the box
    @Test
    public void testBones5x1() throws Exception {
        TvInfoSource db = givenDb();

        Episode e = db.lookup("Bones", 5, 1);

        assertNotNull("Episode is null", e);
        assertNotNull("Series is null", e.getSeries());
        assertEquals("Episode title does not match", "Harbingers in the Fountain", e.getEpisodeTitle());
        assertEquals("Season number does not match", (Integer)5, e.getSeasonNum());
        assertEquals("Episode number does not match", (Integer)1, e.getEpisodeNum());
        assertEquals("Network does not match", "FOX", e.getSeries().getNetwork());
        dateCompare(2009, 9, 17, e.getFirstAirDate(), "Air Date");
    }

    //This is a test that requires a lookup override in the series id file
    //@Test
    public void testCastle5x1() throws Exception {
        TvInfoSource db = givenDb();

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
        TvInfoSource db = givenDb();

        Episode e = db.lookup("Mom", 3, 12);
        assertNotNull("Episode not linked to Series", e.getSeries());
        assertEquals("Fanart name doesn't match", "Mom", e.getSeries().getOriginalName());
        assertEquals("Episode title does not match", "Diabetic Lesbians and a Blushing Bride", e.getEpisodeTitle());
        assertEquals("Season number does not match", (Integer)3, e.getSeasonNum());
        assertEquals("Episode number does not match", (Integer)12, e.getEpisodeNum());
        assertEquals("Network does not match", "CBS", e.getSeries().getNetwork());
        dateCompare(2016, 2, 18, e.getFirstAirDate(), "Air Date");
    }

    @Test
    public void testBannerInfo() throws Exception {
        TvInfoSource db = givenDb();
        Episode e = db.lookup("Bones", 5, 1);

        Collection<Image> banners = e.getSeries().getImages();

        assertNotNull(banners);
        assertFalse(banners.isEmpty());

        for(Image b : banners) {
            assertNotNull(b);
            assertNotNull(b.getName());
            assertNotNull(b.getUrl());
            assertNotNull(b.getType());
        }
    }

    protected abstract TvInfoSource givenDb() throws Exception;
}
