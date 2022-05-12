package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.rel.RelNode;


public interface RelNodeVolcanoWrapper extends RelNode  {

    RexTruffleNode getInputDataAccessor(int index);

    RexTruffleNode getOutputDataAccessor(int index);

    VolcanoIteratorNode getVolcanoIteratorNode();

}
