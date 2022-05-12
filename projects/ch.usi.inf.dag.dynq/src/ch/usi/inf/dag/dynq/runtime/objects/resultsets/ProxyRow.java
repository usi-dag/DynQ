package ch.usi.inf.dag.dynq.runtime.objects.resultsets;


import ch.usi.inf.dag.dynq.runtime.objects.data.DynQNullValue;
import ch.usi.inf.dag.dynq.runtime.utils.MemberSet;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import java.sql.Date;
import java.time.LocalDate;


@ExportLibrary(InteropLibrary.class)
public class ProxyRow implements TruffleObject {

    private final MemberSet members;
    private final Object[] row;

    private ProxyRow(MemberSet members, Object[] row) {
        this.members = members;
        this.row = row;
    }

    public static ProxyRowBuilder builder(String[] memberNames) {
        return new ProxyRowBuilder(memberNames);
    }

    public static class ProxyRowBuilder {
        private final MemberSet members;

        private ProxyRowBuilder(String[] memberNames) {
            this.members = new MemberSet(memberNames);
        }

        public ProxyRow build(Object[] array) {
            return new ProxyRow(members, array);
        }

    }

    // Interop

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
    public Object invokeMember(String member, Object... arguments) { return null; }


    @ExportMessage
    static class ReadMember {

        @Specialization // TODO improve/cache
        @CompilerDirectives.TruffleBoundary
        public static Object readMember(ProxyRow receiver, String member) throws UnknownIdentifierException {
            for (int i = 0; i < receiver.row.length; i++) {
                if(receiver.members.getIndex(i).equals(member)) {
                    Object field = receiver.row[i];
                    if(field instanceof LocalDate) {
                        return field.toString();
                    }
                    else if(field instanceof Date) {
                        return field.toString();
                    }
                    else if(field instanceof ShipToDynLangWithToString) {
                        return field.toString();
                    }
                    else if(field instanceof WrapsDynLangValue) {
                        return ((WrapsDynLangValue)field).getValue();
                    }
                    else if(field == null) {
                        return DynQNullValue.INSTANCE;
                    }
                    return field;
                }
            }
            throw UnknownIdentifierException.create(member);
        }

    }

}
