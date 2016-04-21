package main;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import data.Episode;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class FileParserTest {

    @Parameters(name="{0}")
    public static Object[][] data() {
        return new Object[][] {
            {"A:\\ShowName\\403_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"/ShowName/403_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"A:\\ShowName\\4x03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"/ShowName/4x03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"A:\\ShowName\\S4E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"/ShowName/S4E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"A:\\ShowName\\S04E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"/ShowName/S04E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"A:\\ShowName\\S04E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"/ShowName/S04E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},

            {"/ShowName/Season 4/S04E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"/ShowName/Season 2/S04E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, InconsistentDataException.class},

            {"/root/path/folder/ShowName - S04E03_-_Title_Of_Episode.ext", "ShowName", 4, 3, null},
            {"/root/path/folder/Show Name - S04E03_-_Title_Of_Episode.ext", "Show Name", 4, 3, null},
        };
    }

    private String path;
    private String showName;
    private int season;
    private int episode;

    @Rule
    public ExpectedException expected = ExpectedException.none();


    public FileParserTest(String path, String showName, int season, int episode, Class<? extends Throwable> expected) {
        this.path = path;
        this.showName = showName;
        this.season = season;
        this.episode = episode;

        if(expected != null) {
            this.expected.expect(expected);
        }

    }

    @Test
    public void test() {
        Episode e = FileParser.parse(new File(path));

        assertNotNull(e);
        assertNotNull(e.getSeries());
        assertEquals(showName, e.getSeries().getName());

        assertNotNull(e.getSeasonNum());
        assertNotNull(e.getEpisodeNum());
        assertEquals(season, (int)e.getSeasonNum());
        assertEquals(episode, (int)e.getEpisodeNum());

    }

}
