package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.adapter.enumerable.EnumerableLimit;


public class LimitVolcanoWrapper extends EnumerableLimit implements SingleRelNodeVolcanoWrapper {

    private VolcanoIteratorNode volcanoIteratorNode;
    private final RelNodeVolcanoWrapper child;

    public LimitVolcanoWrapper(EnumerableLimit limit, VolcanoIteratorNode volcanoIteratorNode, RelNodeVolcanoWrapper child) {
        super(limit.getCluster(), limit.getTraitSet(), child, limit.offset, limit.fetch);
        this.volcanoIteratorNode = volcanoIteratorNode;
        this.child = child;
    }

    @Override
    public RelNodeVolcanoWrapper getInput() {
        return child;
    }

    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        return child.getOutputDataAccessor(index);
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return volcanoIteratorNode;
    }

}
