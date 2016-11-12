package main;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.InputSource;

import data.Series;
import db.thetvdb.xml.BaseSeriesXmlHandler;

import static org.junit.Assert.*;

public class Tests {


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testLargestFilePreprocessing() throws Exception {
        File dir = folder.newFolder("Bones", "some_other_folder");

        File showDir = dir.getParentFile();
        File seasonDir = new File(showDir, "Season 2");

        File file1 = new File(dir, "5x01_some_file.mkv");
        File file2 = new File(dir, "4x02_some_file.mkv");
        File file3 = new File(dir, "3x03_some_file.mkv");
        File file4 = new File(dir, "2x04_some_file.mkv");
        File file5 = new File(dir, "1x05_some_file.mkv");

        FileUtils.writeStringToFile(file1, "A string");
        FileUtils.writeStringToFile(file2, "A string2");
        FileUtils.writeStringToFile(file3, "A string33");
        FileUtils.writeStringToFile(file4, "A string that is obviously the longest");
        FileUtils.writeStringToFile(file5, "A string here too");

        String[] args = {"-Pcache-dir=" + folder.newFolder().getAbsolutePath(),
                "-Pno-fanart", "-Ppreprocess-largest-file-in-dir", file1.getAbsolutePath()};
        Main.main(args);

        assertFalse(dir.exists());
        assertTrue(showDir.exists());
        assertTrue(seasonDir.exists());

        File[] files = seasonDir.listFiles();
        assertEquals(2, files.length);
        assertTrue(files[0].getName().startsWith("02x04"));
        assertTrue(files[0].getName().endsWith(".mkv"));
        assertEquals(files[0].getName() + ".properties", files[1].getName());
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
