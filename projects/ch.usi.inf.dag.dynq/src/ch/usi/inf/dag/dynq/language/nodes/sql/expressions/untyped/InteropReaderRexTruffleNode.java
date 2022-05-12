package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class InteropReaderRexTruffleNode extends RexTruffleNode {

    final String member;

    @Child
    RexTruffleNode converter;


    public static InteropReaderRexTruffleNode create(String member) {
        return InteropReaderRexTruffleNodeGen.create(member);
    }

    InteropReaderRexTruffleNode(String member) {
        this.member = member;
        this.converter = InteropConverterRexTruffleNodeGen.create();
    }


    @Specialization
    public Object read(VirtualFrame frame, Object input,
                       @CachedLibrary(limit = "1") InteropLibrary interopRead) throws InteropException, FrameSlotTypeException {
        try {
            Object value = interopRead.readMember(input, member);
            return converter.executeWith(frame, value);
        } catch (NullPointerException | UnknownIdentifierException unused) {
            return null;
        }
    }

    @Override
    public String explain() {
        return super.explain() + "(" + member + ")" ;
    }

}
