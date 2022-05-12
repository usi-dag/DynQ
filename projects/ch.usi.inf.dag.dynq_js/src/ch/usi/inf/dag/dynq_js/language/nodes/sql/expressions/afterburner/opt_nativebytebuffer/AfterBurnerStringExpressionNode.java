package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.StringRexTruffleNode;


abstract class AfterBurnerStringExpressionNode extends StringRexTruffleNode {

    final AfterBurnerColumnarAccessorNodeState accessor;

    public AfterBurnerStringExpressionNode(AfterBurnerColumnarAccessorNodeState accessor) {
        this.accessor = accessor;
    }
}
