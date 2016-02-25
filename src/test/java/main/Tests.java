package main;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.InputSource;

import data.Episode;
import data.Series;
import db.thetvdb.TheTvDbDatabase;
import db.thetvdb.xml.BaseSeriesXmlHandler;

import static org.junit.Assert.*;

public class Tests {

	//This is a vanilla test that should work out of the box
	@Test
	public void testBones5x1() throws Exception {
		TheTvDbDatabase db = new TheTvDbDatabase("en");
		db.initialize();

		Episode e = db.lookup("Bones", 5, 1);

		assertEquals("Episode title does not match", "Harbingers in the Fountain", e.getEpisodeTitle());
		assertEquals("Season number does not match", 5, e.getSeasonNum());
		assertEquals("Episode number does not match", 1, e.getEpisodeNum());

		System.out.println(e);
	}

	//This is a test that requires a lookup override in the series id file
	//@Test
	public void testCastle5x1() throws Exception {
		TheTvDbDatabase db = new TheTvDbDatabase("en");
		db.initialize();

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
        TheTvDbDatabase db = new TheTvDbDatabase("en");
        db.initialize();

        Episode e = db.lookup("Mom", 3, 12);
        assertNotNull("Episode not linked to Series", e.getSeries());
        assertEquals("Fanart name doesn't match", "Mom", e.getSeries().getOriginalName());
        assertEquals("Episode title does not match", "Diabetic Lesbians and a Blushing Bride", e.getEpisodeTitle());
        assertEquals("Season number does not match", 3, e.getSeasonNum());
        assertEquals("Episode number does not match", 12, e.getEpisodeNum());

        System.out.println(e);
    }

	@Test
	public void testBadUTFFileXml() throws Exception {
	    File seriesIdFile = new File("src/test/resources/266967.xml");

	    Series series = new Series();

	    InputStream inputStream= new FileInputStream(seriesIdFile);
	    Reader reader = new InputStreamReader(inputStream,"UTF-8");

	    InputSource is = new InputSource(reader);
	    is.setEncoding("UTF-8");
        SAXParserFactory.newInstance().newSAXParser().parse(is/*seriesIdFile*/, new BaseSeriesXmlHandler(series));

	}

	@Test
	public void testVersion() {
	    System.out.println(Version.getFullNameVersion());
	}
}
