package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public final class IsTrueRexTruffleNode extends RexTruffleNode {

    @Child private RexTruffleNode child;
    @Child private CheckIsTrue isTrue;

    public IsTrueRexTruffleNode(RexTruffleNode child) {
        this.child = child;
        this.isTrue = IsTrueRexTruffleNodeFactory.CheckIsTrueNodeGen.create();
    }


    @Override
    public Boolean executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        Object result = child.executeWith(frame, input);
        return isTrue.execute(result);
    }


    static abstract class CheckIsTrue extends Node implements Explainable {

        public abstract boolean execute(Object input);

        @Specialization
        public boolean runWithBoolean(boolean input) {
            return input;
        }

        @Specialization
        public boolean runWithInt(int input) {
            return input == 1;
        }

        @Fallback
        public boolean runWithObject(Object input) {
            return Boolean.TRUE.equals(input);
        }

    }

}


