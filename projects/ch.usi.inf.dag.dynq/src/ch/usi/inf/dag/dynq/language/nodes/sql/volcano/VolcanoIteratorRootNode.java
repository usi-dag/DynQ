package ch.usi.inf.dag.dynq.language.nodes.sql.volcano;


import ch.usi.inf.dag.dynq.language.TruffleLinqLanguage;
import ch.usi.inf.dag.dynq.language.nodes.TruffleLinqRootNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricRootNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;


public final class VolcanoIteratorRootNode implements Explainable {
    final TruffleLinqLanguage truffleLinqLanguage;

    public VolcanoIteratorRootNode(TruffleLinqLanguage truffleLinqLanguage) {
        this.truffleLinqLanguage = truffleLinqLanguage;
    }

    public VolcanoIteratorNode child;

    public void setChild(VolcanoIteratorNode child) {
        this.child = child;
    }

    public TruffleLinqRootNode push() {
        return new DataCentricRootNode(truffleLinqLanguage, child.acceptConsumer(null));
    }

    @Override
    public String explain() {
        return Explainable.explain(this.child);
    }

}
