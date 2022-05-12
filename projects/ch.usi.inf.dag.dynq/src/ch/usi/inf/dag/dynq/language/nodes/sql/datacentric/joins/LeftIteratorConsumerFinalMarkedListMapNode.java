package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins;

import ch.usi.inf.dag.dynq.structures.FinalMarkedListMap;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class LeftIteratorConsumerFinalMarkedListMapNode extends AbstractLeftIteratorConsumerNode {

    private FinalMarkedListMap map = new FinalMarkedListMap();

    @Override
    public void init(VirtualFrame frame) {
        map = new FinalMarkedListMap();
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
        map = new FinalMarkedListMap(exactSize);
    }

    @Override
    public FinalMarkedListMap getFinalizedState(VirtualFrame frame) {
        return map;
    }

    public final void insert(Object key, Object value) {
        map.insert(key, value);
    }

    public FinalMarkedListMap getMap() {
        return map;
    }
    public void freeMap() {
        map = null;
    }
}
