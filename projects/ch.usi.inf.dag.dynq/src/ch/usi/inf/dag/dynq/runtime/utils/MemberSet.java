package ch.usi.inf.dag.dynq.runtime.utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;


@ExportLibrary(InteropLibrary.class)
public final class MemberSet implements TruffleObject {

  @CompilerDirectives.CompilationFinal(dimensions = 1) private final String[] values;

  public MemberSet(String... values) {
    this.values = values;
  }

  public boolean contains(String value) {
    for(String key : values) {
      if(key.equals(value)) return true;
    }
    return false;
  }

  public String getIndex(int i) {
    return values[i];
  }

  @ExportMessage
  @SuppressWarnings("static-method")
  public boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  public long getArraySize() {
    return values.length;
  }

  @ExportMessage
  public boolean isArrayElementReadable(long index) {
    return index >= 0 && index < values.length;
  }

  @ExportMessage
  public Object readArrayElement(long index) throws InvalidArrayIndexException {
    if ((index < 0) || (index >= values.length)) {
      CompilerDirectives.transferToInterpreter();
      throw InvalidArrayIndexException.create(index);
    }
    return values[(int) index];
  }

}