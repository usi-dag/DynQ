package ch.usi.inf.dag.dynq.language.nodes;


import ch.usi.inf.dag.dynq.language.TruffleLinqLanguage;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.nodes.RootNode;


public abstract class TruffleLinqRootNode extends RootNode implements Explainable {
    public TruffleLinqRootNode(TruffleLinqLanguage truffleLinqLanguage) {
        super(truffleLinqLanguage);
    }

    @Override
    public String explain() {
        return Explainable.explain(this);
    }
}
