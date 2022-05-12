package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins;


import ch.usi.inf.dag.dynq.structures.FinalListMap;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class LeftIteratorConsumerFinalListMapNode extends AbstractLeftIteratorConsumerNode {

    FinalListMap map = new FinalListMap();

    @Override
    public void init(VirtualFrame frame) {
        map = new FinalListMap();
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
        map = new FinalListMap(exactSize);
    }

    @Override
    public FinalListMap getFinalizedState(VirtualFrame frame) {
        return map;
    }

    public final void insert(Object key, Object value) {
        map.insert(key, value);
    }
}
