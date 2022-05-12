package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayItemGetterNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.util.ImmutableBitSet;

import java.util.List;


public class AggregateVolcanoWrapper extends Aggregate implements SingleRelNodeVolcanoWrapper {

    private final VolcanoIteratorNode volcanoIteratorNode;
    private final RelNodeVolcanoWrapper child;

    public AggregateVolcanoWrapper(Aggregate aggregate, VolcanoIteratorNode volcanoIteratorNode, RelNodeVolcanoWrapper child) {
        super(aggregate.getCluster(), aggregate.getTraitSet(), aggregate.getHints(), aggregate.getInput(), aggregate.getGroupSet(), aggregate.groupSets, aggregate.getAggCallList());
        this.volcanoIteratorNode = volcanoIteratorNode;
        this.child = child;
    }

    @Override
    public RelNodeVolcanoWrapper getInput() {
        return child;
    }

    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        if(volcanoIteratorNode.getOutputRowJavaType().isArray()) {
            return ArrayItemGetterNodeGen.create(index);
        }
        throw new RuntimeException("Cannot create output data accessor for aggregate, unexpected return type: " + volcanoIteratorNode.getOutputRowJavaType());
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return volcanoIteratorNode;
    }

    @Override
    public Aggregate copy(RelTraitSet relTraitSet, RelNode relNode, ImmutableBitSet immutableBitSet, List<ImmutableBitSet> list, List<AggregateCall> list1) {
        throw new RuntimeException("Cannot copy a volcano wrapper aggregate node");
    }

}
