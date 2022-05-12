package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.PairLeftGetterNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.PairRightGetterNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinInfo;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexVisitorImpl;

import java.util.Set;
import java.util.stream.Collectors;


public class JoinVolcanoWrapper extends Join implements RelNodeVolcanoWrapper {

    private final Join join;
    private VolcanoIteratorNode volcanoIteratorNode;
    private final RelNodeVolcanoWrapper left, right;
    private final CheckJoinSideRexVisitor joinSideRexVisitor;

    public JoinVolcanoWrapper(Join join, VolcanoIteratorNode volcanoIteratorNode, RelNodeVolcanoWrapper left, RelNodeVolcanoWrapper right) {
        super(join.getCluster(), join.getTraitSet(), join.getHints(), left, right, join.getCondition(), join.getVariablesSet(), join.getJoinType());
        this.join = join;
        this.volcanoIteratorNode = volcanoIteratorNode;
        this.left = left;
        this.right = right;
        this.joinSideRexVisitor = new CheckJoinSideRexVisitor(left.getRowType().getFieldCount());
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return volcanoIteratorNode;
    }

    public JoinInfo getJoinInfo()  {
        return join.analyzeCondition();
    }

    public void setVolcanoIteratorNode(VolcanoIteratorNode volcanoIteratorNode) {
        this.volcanoIteratorNode = volcanoIteratorNode;
    }

    public RelNodeVolcanoWrapper getLeftWrapper() {
        return left;
    }

    public RelNodeVolcanoWrapper getRightWrapper() {
        return right;
    }

    @Override
    public RexTruffleNode getInputDataAccessor(int index) {
        int leftFieldsCount = left.getRowType().getFieldCount();
        if(index < leftFieldsCount) {
            return left.getOutputDataAccessor(index);
        } else {
            return right.getOutputDataAccessor(index - leftFieldsCount);
        }
    }

    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        if(!joinType.projectsRight()) { // i.e., SEMI or ANTI
            return left.getOutputDataAccessor(index);
        }
        int leftFieldsCount = left.getRowType().getFieldCount();

        RexTruffleNode readFromPair;
        if(index < leftFieldsCount) {
            readFromPair = PairLeftGetterNodeGen.create();
        } else {
            readFromPair = PairRightGetterNodeGen.create();
        }
        RexTruffleNode fromInput = getInputDataAccessor(index);
        return readFromPair.andThen(fromInput);
    }

    /**
     * Checks whether all input in a given RexCall come from the left side of this join
     * */
    public boolean canBeResolvedOnLeftSide(RexNode rexNode) {
        return rexNode.accept(joinSideRexVisitor) == JoinDependency.LEFT;
    }

    /**
     * Checks whether all input in a given RexCall come from the right side of this join
     * */
    public boolean canBeResolvedOnRightSide(RexNode rexNode) {
        return rexNode.accept(joinSideRexVisitor) == JoinDependency.RIGHT;
    }

    enum JoinDependency {
        LEFT, RIGHT, BOTH, NONE
    }

    static class CheckJoinSideRexVisitor extends RexVisitorImpl<JoinDependency> {
        int leftFieldsCount;

        CheckJoinSideRexVisitor(int leftFieldsCount) {
            super(true);
            this.leftFieldsCount = leftFieldsCount;
        }

        @Override
        public JoinDependency visitCall(RexCall call) {
            Set<JoinDependency> deps = call.operands.stream()
                    .map(op -> op.accept(this))
                    .collect(Collectors.toSet());

            switch (deps.size()) {
                case 2: return JoinDependency.BOTH;
                case 1: return deps.iterator().next();
                case 0: return JoinDependency.NONE;
            }
            return null;
        }

        @Override
        public JoinDependency visitInputRef(RexInputRef inputRef) {
            return (inputRef.getIndex() < leftFieldsCount) ? JoinDependency.LEFT : JoinDependency.RIGHT;
        }
    }


    @Override
    public Join copy(RelTraitSet relTraitSet, RexNode rexNode, RelNode relNode, RelNode relNode1, JoinRelType joinRelType, boolean b) {
        throw new RuntimeException("Cannot copy a volcano wrapper join node");
    }
}
