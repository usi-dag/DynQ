package ch.usi.inf.dag.dynq.parser;


public class SqlParsingException extends Exception {
    private static final long serialVersionUID = -2578309102815757553L;

    public SqlParsingException(String message, Exception cause) {
        super(message, cause);
    }
}
