package main;

import java.io.File;

public class FailedRenameException extends Exception {

    public FailedRenameException(File fromFile, File toFile) {
        super("Failed to rename " + fromFile + " to " + toFile);
    }

}
