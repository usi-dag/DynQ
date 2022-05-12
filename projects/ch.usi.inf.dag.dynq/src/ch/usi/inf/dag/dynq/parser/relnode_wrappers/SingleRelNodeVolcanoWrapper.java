package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;

public interface SingleRelNodeVolcanoWrapper extends RelNodeVolcanoWrapper {

    RelNodeVolcanoWrapper getInput();

    @Override
    default RexTruffleNode getInputDataAccessor(int index) {
        return getInput().getOutputDataAccessor(index);
    }

}
