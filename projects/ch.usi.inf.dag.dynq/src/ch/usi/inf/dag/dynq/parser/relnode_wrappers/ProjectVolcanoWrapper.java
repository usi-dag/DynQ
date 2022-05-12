package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayItemGetterNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexNode;

import java.util.List;


public class ProjectVolcanoWrapper extends Project implements SingleRelNodeVolcanoWrapper {

    private final VolcanoIteratorNode volcanoIteratorNode;
    private final RelNodeVolcanoWrapper child;

    public ProjectVolcanoWrapper(Project project, VolcanoIteratorNode volcanoIteratorNode, RelNodeVolcanoWrapper child) {
        super(project.getCluster(), project.getTraitSet(), project.getHints(), child, project.getProjects(), project.getRowType());
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
        throw new RuntimeException("Cannot create output data accessor for projection, unexpected return type: " + volcanoIteratorNode.getOutputRowJavaType());
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return volcanoIteratorNode;
    }

    @Override
    public Project copy(RelTraitSet relTraitSet, RelNode relNode, List<RexNode> list, RelDataType relDataType) {
        throw new RuntimeException("Cannot copy a volcano wrapper project node");
    }
}
