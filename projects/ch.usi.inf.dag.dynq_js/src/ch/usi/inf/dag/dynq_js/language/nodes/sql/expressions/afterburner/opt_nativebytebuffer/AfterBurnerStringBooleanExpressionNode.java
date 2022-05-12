package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;


abstract class AfterBurnerStringBooleanExpressionNode extends BooleanRexTruffleNode {

    final AfterBurnerColumnarAccessorNodeState accessor;

    public AfterBurnerStringBooleanExpressionNode(AfterBurnerColumnarAccessorNodeState accessor) {
        this.accessor = accessor;
    }
    
}
