package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rex.RexNode;


public class FilterVolcanoWrapper extends Filter implements SingleRelNodeVolcanoWrapper {

    private final VolcanoIteratorNode volcanoIteratorNode;
    private final RelNodeVolcanoWrapper child;

    public FilterVolcanoWrapper(Filter filter, VolcanoIteratorNode volcanoIteratorNode, RelNodeVolcanoWrapper child) {
        super(filter.getCluster(), filter.getTraitSet(), child, filter.getCondition());
        this.volcanoIteratorNode = volcanoIteratorNode;
        this.child = child;
    }


    @Override
    public RelNodeVolcanoWrapper getInput() {
        return child;
    }

    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        return getInputDataAccessor(index);
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return volcanoIteratorNode;
    }

    @Override
    public Filter copy(RelTraitSet relTraitSet, RelNode relNode, RexNode rexNode) {
        throw new RuntimeException("Cannot copy a volcano wrapper filter node");
    }
}
