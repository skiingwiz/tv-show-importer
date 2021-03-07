package main;

public class Version {
    public static String getFullNameVersion() {
        return "TV File Processor (v" + getVersionString() + ")";
    }

    public static String getVersionString() {
        return "";
    }

    private Version() {
    }
}
