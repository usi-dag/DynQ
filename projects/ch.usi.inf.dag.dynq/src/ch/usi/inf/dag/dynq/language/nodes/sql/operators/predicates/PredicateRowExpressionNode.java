package ch.usi.inf.dag.dynq.language.nodes.sql.operators.predicates;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

public abstract class PredicateRowExpressionNode extends PredicateNode {

    @Child
    RexTruffleNode expression;

    PredicateRowExpressionNode(RexTruffleNode expression) {
        this.expression = expression;
    }

    @Specialization
    boolean executeExpression(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
        return expression.runBoolean(frame, row);
    }

}
