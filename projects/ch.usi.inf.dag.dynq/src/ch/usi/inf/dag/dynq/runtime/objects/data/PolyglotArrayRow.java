package ch.usi.inf.dag.dynq.runtime.objects.data;


import ch.usi.inf.dag.dynq.runtime.utils.MemberSet;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;

@ExportLibrary(InteropLibrary.class)
public class PolyglotArrayRow implements TruffleObject {


  final Object[] data;
  final String[] members;
  final MemberSet memberSet;

  public PolyglotArrayRow(Object[] data, String... members) {
    this.data = data;
    this.members = members;
    this.memberSet = new MemberSet(members);
  }

  @ExportMessage
  public Object readMember(String member) throws UnknownIdentifierException {
    int idx = getMemberIndex(member);
    if(idx == -1) {
      throw UnknownIdentifierException.create(member);
    }
    return data[idx];
  }

  @ExplodeLoop
  int getMemberIndex(String member) {
    for (int i = 0; i < members.length; i++) {
      if(members[i].equals(member)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < members.length; i++) {
      sb.append(members[i]).append(": ").append(data[i]).append(", ");
    }
    return sb.append("}").toString();
  }

  @ExportMessage
  public final boolean isMemberReadable(String member) { return memberSet.contains(member); }

  @ExportMessage
  public boolean isMemberInvocable(String member) { return false; }

  @ExportMessage
  public final boolean hasMembers() { return true; }

  @ExportMessage
  public Object getMembers(boolean includeInternal) {
    return memberSet;
  }

  @ExportMessage
  public Object invokeMember(String member, Object... arguments) { return null; }


}
