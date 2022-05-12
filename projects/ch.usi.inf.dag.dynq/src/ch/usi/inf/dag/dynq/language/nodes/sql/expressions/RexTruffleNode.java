package ch.usi.inf.dag.dynq.language.nodes.sql.expressions;

import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;

import java.time.LocalDate;


/**
 *
 * Generic Row Expression Node
 *
 * */

public abstract class RexTruffleNode extends Node implements Explainable, WithOptimizedComposedExpression {

    public abstract Object executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;


    // TODO protect casts with custom exception, e.g., TruffleSqlException.typeError
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return (boolean) executeWith(frame, input);
    }

    public int runInt(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return (int) executeWith(frame, input);
    }

    public long runLong(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return (long) executeWith(frame, input);
    }

    public double runDouble(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return (double) executeWith(frame, input);
    }

    public LocalDate runDate(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return (LocalDate) executeWith(frame, input);
    }

    public String runString(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        // TODO think about that
        return executeWith(frame, input).toString();
    }

    @Override
    public String explain() {
        return getClass().getSimpleName();
    }

    public RexTruffleNode andThen(RexTruffleNode after) {
        return new RexTruffleNode() {
            @Child RexTruffleNode fst = RexTruffleNode.this;
            @Child RexTruffleNode snd = after;

            @Override
            public Object executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
                return snd.executeWith(frame, fst.executeWith(frame, input));
            }

            @Override
            public String explain() {
                return "ComposedNode first:" + fst.explain() + " and then: " + snd.explain();
            }
        };
    }
}
