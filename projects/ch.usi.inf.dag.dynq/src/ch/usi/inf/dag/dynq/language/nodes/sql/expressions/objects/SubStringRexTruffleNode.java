package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class SubStringRexTruffleNode extends StringRexTruffleNode {

    @CompilerDirectives.CompilationFinal final int from;
    @CompilerDirectives.CompilationFinal final int to;

    @Child
    RexTruffleNode stringGetter;

    public SubStringRexTruffleNode(int from, int len, RexTruffleNode stringGetter) {
        this.from = from;
        this.to = from + len;
        this.stringGetter = stringGetter;
    }

    @Specialization
    public String executeCached(VirtualFrame frame, Object input,
                                @CachedLibrary(limit = "1") InteropLibrary interopRead,
                                @CachedLibrary(limit = "1") InteropLibrary interopAsString)
            throws InteropException, FrameSlotTypeException {

        String value = stringGetter.runString(frame, input);
        return value.substring(from, to);
    }

}
