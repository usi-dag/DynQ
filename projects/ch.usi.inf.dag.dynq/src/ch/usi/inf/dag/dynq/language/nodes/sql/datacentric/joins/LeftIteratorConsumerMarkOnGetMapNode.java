package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins;


import ch.usi.inf.dag.dynq.structures.MarkOnGetMap;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class LeftIteratorConsumerMarkOnGetMapNode extends AbstractLeftIteratorConsumerNode {

    MarkOnGetMap map = new MarkOnGetMap();

    @Override
    public void init(VirtualFrame frame) {
        map = new MarkOnGetMap();
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
        map = new MarkOnGetMap(exactSize);
    }

    @Override
    public MarkOnGetMap getFinalizedState(VirtualFrame frame) {
        return map;
    }

    public final void insert(Object key, Object value) {
        map.insert(key, value);
    }
}
