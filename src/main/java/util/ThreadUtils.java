package util;

public final class ThreadUtils {

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private ThreadUtils() {}
}
