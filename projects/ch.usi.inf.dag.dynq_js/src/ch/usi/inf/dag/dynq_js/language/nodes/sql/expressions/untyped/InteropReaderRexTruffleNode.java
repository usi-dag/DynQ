package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.JSReadElementNodeFactory;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.nodes.access.ReadElementNode;


@ImportStatic({JSReadElementNodeFactory.class})
public abstract class InteropReaderRexTruffleNode extends RexTruffleNode  {

    final String member;

    @Child RexTruffleNode converter;

    public InteropReaderRexTruffleNode(String member) {
        this.member = member;
        this.converter = InteropConverterRexTruffleNodeGen.create();
    }


    @Specialization
    public Object readJS(VirtualFrame frame, DynamicObject input,
                         @Cached(value = "getJSReadElementNode()", uncached = "getUncachedRead()") ReadElementNode readNode)
            throws InteropException, FrameSlotTypeException {
        try {
            Object value = readNode.executeWithTargetAndIndexOrDefault(input, member, null);
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
