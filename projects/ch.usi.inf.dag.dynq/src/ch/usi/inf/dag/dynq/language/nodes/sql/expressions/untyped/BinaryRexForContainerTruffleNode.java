package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.nodes.Node;


public abstract class BinaryRexForContainerTruffleNode extends Node implements Explainable {

    public abstract Object execute(Object left, Object right);

}


