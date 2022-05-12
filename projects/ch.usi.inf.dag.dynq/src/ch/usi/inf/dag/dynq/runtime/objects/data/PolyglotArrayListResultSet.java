package ch.usi.inf.dag.dynq.runtime.objects.data;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;

import java.util.ArrayList;

@ExportLibrary(InteropLibrary.class)
public class PolyglotArrayListResultSet implements TruffleObject {

  final ArrayList<PolyglotArrayRow> data;
  public PolyglotArrayListResultSet(ArrayList<PolyglotArrayRow> data) {
    this.data = data;
  }

  @ExportMessage
  @SuppressWarnings("static-method")
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  @SuppressWarnings("static-method")
  boolean isArrayElementReadable(long index) {
    return index >= 0 && index < data.size();
  }

  @ExportMessage
  @SuppressWarnings("static-method")
  long getArraySize() {
    return 1;
  }

  @ExportMessage
  Object readArrayElement(long index, @Cached BranchProfile exception) throws InvalidArrayIndexException {
    if (!isArrayElementReadable(index)) {
      exception.enter();
      throw InvalidArrayIndexException.create(index);
    }
    return data.get((int)index);
  }


}
