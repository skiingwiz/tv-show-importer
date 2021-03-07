package db;

public class DatabaseProcessingException extends Exception {
    private static final long serialVersionUID = -761138714271716923L;

    public DatabaseProcessingException(Throwable t) {
        super(t);
    }

    public DatabaseProcessingException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public DatabaseProcessingException(String msg) {
        super(msg);
    }
}
