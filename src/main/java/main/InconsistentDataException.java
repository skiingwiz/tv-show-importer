package main;

@SuppressWarnings("serial")
public class InconsistentDataException extends RuntimeException {

    public InconsistentDataException(String fieldName, Object val1, Object val2) {
        super(String.format("Incosistent result from file parsing. %s was reported to be both %s and %s",
                fieldName, val1, val2));
    }

}
