package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class StringFromInteropRexTruffleNode extends StringRexTruffleNode {

    private final String member;

    public StringFromInteropRexTruffleNode(String member) {
        this.member = member;
    }

    @Specialization
    public String executeCached(Object input,
                                @CachedLibrary(limit = "1") InteropLibrary interopRead,
                                @CachedLibrary(limit = "1") InteropLibrary interopAsString)
            throws InteropException {
        return interopAsString.asString(interopRead.readMember(input, member));
    }

    @Override
    public String explain() {
        return super.explain() + "(" + member + ")" ;
    }
}
