package main;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import util.opt.InvalidOptionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OptionsTests {

    @Test
    public void testRenameUnspecifiedWithFile()
    throws FileNotFoundException, InvalidOptionException, IOException {
        String filename = "src/test/resources/test-props.properties";

        String[] args = new String[] {
                "--config", filename
        };

        GlobalConfig.parse(args);

        assertFalse(GlobalConfig.getOptions().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void testNoRenameWithFile()
    throws FileNotFoundException, InvalidOptionException, IOException {
        String filename = "src/test/resources/test-props.properties";

        String[] args = new String[] {
                "--config", filename, "--no-rename"
        };

        GlobalConfig.parse(args);

        assertFalse(GlobalConfig.getOptions().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void testRenameUnspecified()
    throws FileNotFoundException, InvalidOptionException, IOException {
        String[] args = new String[] {
        };

        GlobalConfig.parse(args);

        assertTrue(GlobalConfig.getOptions().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void testNoRename()
    throws FileNotFoundException, InvalidOptionException, IOException {
        String[] args = new String[] {
                "--no-rename"
        };

        GlobalConfig.parse(args);

        assertFalse(GlobalConfig.getOptions().getBoolean(GlobalConfig.RENAME));
    }
}
