package ch.usi.inf.dag.dynq.runtime.objects.api;

import ch.usi.inf.dag.dynq.runtime.utils.MemberSet;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;


@ExportLibrary(InteropLibrary.class)
public abstract class AbstractAPITruffleObject implements TruffleObject {

  private final MemberSet members;

  protected AbstractAPITruffleObject(String... members) {
    this.members = new MemberSet(members);
  }

  @ExportMessage
  public final boolean isMemberReadable(String member) { return members.contains(member); }

  @ExportMessage
  public boolean isMemberInvocable(String member) { return false; }

  @ExportMessage
  public final boolean hasMembers() { return true; }

  @ExportMessage
  public Object getMembers(boolean includeInternal) {
    return members;
  }

  @ExportMessage
  public Object readMember(String member) throws UnsupportedMessageException, UnknownIdentifierException { return null; }

  @ExportMessage
  public Object invokeMember(String member, Object... arguments) throws UnsupportedMessageException, ArityException, UnknownIdentifierException, UnsupportedTypeException { return null; }

}


