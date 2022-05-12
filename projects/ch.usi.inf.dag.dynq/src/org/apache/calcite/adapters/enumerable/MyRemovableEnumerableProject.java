package org.apache.calcite.adapters.enumerable;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexNode;

import java.util.List;


public class MyRemovableEnumerableProject extends Project implements EnumerableRel {
    private boolean canRemove = false;

    public MyRemovableEnumerableProject(RelOptCluster cluster, RelTraitSet traitSet, RelNode input, List<? extends RexNode> projects, RelDataType rowType) {
        super(cluster, traitSet, ImmutableList.of(), input, projects, rowType);

        assert this.getConvention() instanceof EnumerableConvention;

    }

    public static MyRemovableEnumerableProject create(RelNode input, List<? extends RexNode> projects, RelDataType rowType) {
        RelOptCluster cluster = input.getCluster();
        RelMetadataQuery mq = cluster.getMetadataQuery();
        RelTraitSet traitSet = cluster.traitSet().replace(EnumerableConvention.INSTANCE).replaceIfs(RelCollationTraitDef.INSTANCE, () -> RelMdCollation.project(mq, input, projects));
        return new MyRemovableEnumerableProject(cluster, traitSet, input, projects, rowType);
    }

    public MyRemovableEnumerableProject copy(RelTraitSet traitSet, RelNode input, List<RexNode> projects, RelDataType rowType) {
        return new MyRemovableEnumerableProject(this.getCluster(), traitSet, input, projects, rowType);
    }

    @Override
    public Result implement(EnumerableRelImplementor implementor, Prefer pref) {
        return null;
    }

    public boolean isRemovable() {
        return canRemove;
    }

    public void setRemovable() {
        canRemove = true;
    }

    @Override
    public String toString() {
        return "MyRemovableEnumerableProject{}";
    }
}
