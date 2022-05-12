package ch.usi.inf.dag.dynq.parser.rex;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexProgram;
import org.graalvm.collections.Pair;

public class RexTruffleNodeVisitorFactory {

    public RexTruffleNodeVisitor create(APISessionManagement session, RelNodeVolcanoWrapper relNode, RexProgram rexProgram) {
        return new RexTruffleNodeVisitor(session, relNode, rexProgram);
    }

    public RexTruffleNodeVisitor create(APISessionManagement session, RelNodeVolcanoWrapper relNode) {
        return new RexTruffleNodeVisitor(session, relNode);
    }

    public Pair<RexTruffleNodeVisitor, RexTruffleNodeVisitor> createPair(APISessionManagement session, RelNodeVolcanoWrapper relNodeLeft, RelNodeVolcanoWrapper relNodeRight) {
        RexTruffleNodeVisitor leftVisitor = new RexTruffleNodeVisitor(session, relNodeLeft) {
            @Override
            public RexTruffleNode visitInputRef(RexInputRef rexInputRef) {
                return relNodeLeft.getOutputDataAccessor(rexInputRef.getIndex());
            }
        };
        RexTruffleNodeVisitor rightVisitor = new RexTruffleNodeVisitor(session, relNodeLeft) {
            @Override
            public RexTruffleNode visitInputRef(RexInputRef rexInputRef) {
                int idx = rexInputRef.getIndex() - relNodeLeft.getRowType().getFieldCount();
                return relNodeRight.getOutputDataAccessor(idx);
            }
        };
        return Pair.create(leftVisitor, rightVisitor);
    }

}
