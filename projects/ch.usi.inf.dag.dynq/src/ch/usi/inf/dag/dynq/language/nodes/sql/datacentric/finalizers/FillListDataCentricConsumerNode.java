package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.finalizers;


import ch.usi.inf.dag.dynq.structures.FinalArrayList;
import com.oracle.truffle.api.frame.VirtualFrame;


final class FillListDataCentricConsumerNode extends AbstractFillListDataCentricConsumer {
    static private final int INITIAL = 1024;

    private FinalArrayList state;

    @Override
    public void execute(VirtualFrame frame, Object row) {
        state.add(row);
    }

    @Override
    public FinalArrayList getFinalizedState(VirtualFrame frame) {
        return state;
    }

    @Override
    public void init(VirtualFrame frame) {
        state = new FinalArrayList(INITIAL);
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
        state = new FinalArrayList(exactSize);
    }

    @Override
    public void free(VirtualFrame frame) {
        state = null;
    }

}
