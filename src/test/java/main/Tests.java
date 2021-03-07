package main;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

        String name = null;
        String propertiesName = null;
        for(File f : files) {
            assertTrue(f.getName().startsWith("02x04"));
            if(f.getName().endsWith(".properties")) {
                propertiesName = f.getName();
            } else {
                name = f.getName();
            }
        }
        assertNotNull("No main file found", name);
        assertNotNull("No properties file found", propertiesName);

        assertTrue(name.endsWith(".mkv"));
        assertEquals(name + ".properties", propertiesName);
    }


    @Test
    public void testVersion() {
        System.out.println(Version.getFullNameVersion());
    }
}
