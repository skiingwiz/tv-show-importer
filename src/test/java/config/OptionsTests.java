package config;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OptionsTests {

    @Test
    public void testRenameUnspecifiedWithFile() throws Exception {
        String filename = "src/test/resources/test-props.properties";

        String[] args = new String[] {
                "--config", filename
        };

        GlobalConfig.parse(args);

        assertFalse(GlobalConfig.get().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void testNoRenameWithFile() throws Exception {
        String filename = "src/test/resources/test-props.properties";

        String[] args = new String[] {
                "--config", filename, "=Prename=false"
        };

        GlobalConfig.parse(args);

        assertFalse(GlobalConfig.get().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void testRenameUnspecified() throws Exception {
        String[] args = new String[] {
        };

        GlobalConfig.parse(args);

        assertTrue(GlobalConfig.get().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void testNoRename() throws Exception {
        String[] args = new String[] {
                "-Prename=false"
        };

        GlobalConfig.parse(args);

        assertFalse(GlobalConfig.get().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void renameFalseInfFileTrueOnCommandLine() throws Exception {
        String filename = "src/test/resources/test-props.properties";

        String[] args = new String[] {
                "--config", filename, "-Prename=true"
        };

        GlobalConfig.parse(args);

        assertTrue(GlobalConfig.get().getBoolean(GlobalConfig.RENAME));
    }

    @Test
    public void renameFalseInfFileOnCommandLine() throws Exception {
        String filename = "src/test/resources/test-props.properties";

        String[] args = new String[] {
                "--config", filename, "-Prename"
        };

        GlobalConfig.parse(args);

        assertTrue(GlobalConfig.get().getBoolean(GlobalConfig.RENAME));
    }
}
