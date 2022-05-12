package ch.usi.inf.dag.dynq.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.exception.AbstractTruffleException;


public class TruffleLinqException extends AbstractTruffleException {
  private static final long serialVersionUID = 8614211550329856579L;


  @TruffleBoundary
  public TruffleLinqException(String message) {
    super(message);
    System.err.println(message);
  }

  @TruffleBoundary
  TruffleLinqException() {
  }

}
