package ch.usi.inf.dag.dynq.runtime.utils;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;


public class InteropUtils {

  public static final InteropLibrary INTEROP = InteropLibrary.getFactory().getUncached();


  public static String expectString(Object argument) throws UnsupportedTypeException {
    return expectString(argument, "Expected a string");
  }

  public static String expectString(Object argument, String errorMessage) throws UnsupportedTypeException {
    CompilerAsserts.neverPartOfCompilation();
    try {
      return INTEROP.asString(argument);
    } catch (UnsupportedMessageException e) {
      CompilerDirectives.transferToInterpreter();
      throw UnsupportedTypeException.create(new Object[]{argument}, errorMessage);
    }
  }

  public static int expectInt(Object number) throws UnsupportedTypeException {
    try {
      return INTEROP.asInt(number);
    } catch (UnsupportedMessageException e) {
      CompilerDirectives.transferToInterpreter();
      throw UnsupportedTypeException.create(new Object[]{number}, "expected integer number argument");
    }
  }

  public static long expectLong(Object number, String message) throws UnsupportedTypeException {
    try {
      return INTEROP.asLong(number);
    } catch (UnsupportedMessageException e) {
      CompilerDirectives.transferToInterpreter();
      throw UnsupportedTypeException.create(new Object[]{number}, message);
    }
  }

  public static void checkArgumentLength(Object[] arguments, int expected) throws ArityException {
    if (arguments.length != expected) {
      CompilerDirectives.transferToInterpreter();
      throw ArityException.create(expected, expected, arguments.length);
    }
  }
  public static void checkArgumentMinMaxLength(Object[] arguments, int lower, int upper) throws ArityException {
    if (arguments.length < lower || arguments.length > upper) {
      CompilerDirectives.transferToInterpreter();
      throw ArityException.create(lower, upper, arguments.length);
    }
  }

  public static boolean isExecutable(Object executable) {
    return INTEROP.isExecutable(executable);
  }

  public static Object expectExecutable(Object executable, String msg) throws UnsupportedTypeException {
    if(!isExecutable(executable)) {
      throw UnsupportedTypeException.create(new Object[]{executable}, msg);
    }
    return executable;
  }

}
