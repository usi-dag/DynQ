package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayItemGetterNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.sorts.VolcanoIteratorSortNode;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;


public class SortVolcanoWrapper extends Sort implements SingleRelNodeVolcanoWrapper {

    private VolcanoIteratorSortNode volcanoIteratorNode;
    private final RelNodeVolcanoWrapper child;

    public SortVolcanoWrapper(Sort sort, VolcanoIteratorSortNode volcanoIteratorNode, RelNodeVolcanoWrapper child) {
        super(sort.getCluster(), sort.getTraitSet(), child, sort.collation);
        this.volcanoIteratorNode = volcanoIteratorNode;
        this.child = child;
    }

    @Override
    public RelNodeVolcanoWrapper getInput() {
        return child;
    }

    public void setVolcanoIteratorNode(VolcanoIteratorSortNode volcanoIteratorNode) {
        this.volcanoIteratorNode = volcanoIteratorNode;
    }

    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        if(volcanoIteratorNode.getOutputRowJavaType().isArray()) {
            return ArrayItemGetterNodeGen.create(index);
        }
        return child.getOutputDataAccessor(index);
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return volcanoIteratorNode;
    }

    public SortVolcanoWrapper withLimit(int limit) {
        return new SortVolcanoWrapper(this, volcanoIteratorNode.asLimited(limit), child);
    }

    @Override
    public Sort copy(RelTraitSet relTraitSet, RelNode relNode, RelCollation relCollation, RexNode rexNode, RexNode rexNode1) {
        throw new RuntimeException("Cannot copy a volcano wrapper sort node");
    }

}
