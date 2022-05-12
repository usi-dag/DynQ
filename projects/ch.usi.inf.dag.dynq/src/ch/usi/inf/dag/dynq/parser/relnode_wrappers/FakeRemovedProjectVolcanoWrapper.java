package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;

import java.util.HashMap;
import java.util.List;


public class FakeRemovedProjectVolcanoWrapper extends Project implements SingleRelNodeVolcanoWrapper {

    private final RelNodeVolcanoWrapper child;
    private final HashMap<Integer, Integer> mapping;

    public FakeRemovedProjectVolcanoWrapper(Project project, RelNodeVolcanoWrapper child) {
        super(project.getCluster(), project.getTraitSet(), project.getHints(), child, project.getProjects(), project.getRowType());
        this.child = child;
        mapping = new HashMap<>();
        List<RexNode> projections = project.getProjects();
        int outputIndex = 0;
        for(RexNode expr : projections) {
            if(expr instanceof RexInputRef) {
                mapping.put(outputIndex, ((RexInputRef) expr).getIndex());
            } else {
                throw new IllegalArgumentException("Only RexLiteral allowed");
            }
            outputIndex++;
        }
    }

    @Override
    public RelNodeVolcanoWrapper getInput() {
        return child;
    }

    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        return child.getOutputDataAccessor(mapping.get(index));
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return child.getVolcanoIteratorNode();
    }

    @Override
    public Project copy(RelTraitSet relTraitSet, RelNode relNode, List<RexNode> list, RelDataType relDataType) {
        throw new RuntimeException("Cannot copy a volcano wrapper project node");
    }
}
