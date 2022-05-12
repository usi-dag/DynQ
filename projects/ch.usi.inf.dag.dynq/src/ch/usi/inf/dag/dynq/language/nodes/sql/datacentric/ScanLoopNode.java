package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;

import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;


public final class ScanLoopNode extends Node implements RepeatingNode, Explainable {

    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    final private long nElements;
    final private Object input;
    private int current = 0;

    @Child private InteropLibrary interopRead;
    @Child private DataCentricConsumerNode consumerNode;

    ScanLoopNode(Object input, long nElements, DataCentricConsumerNode consumerNode) {
        this.nElements = nElements;
        this.input = input;
        this.consumerNode = consumerNode;
        this.interopRead = super.insert((INTEROP_LIBRARY_.createDispatched(1)));
    }

    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        try {
            Object row = interopRead.readArrayElement(input, current);
            consumerNode.execute(frame, row);
        }
        catch (EndOfComputation ignored) {return false;}
        catch (InteropException | FrameSlotTypeException ignored) {}
        return ++current < nElements;
    }

    public void reset(VirtualFrame frame) {
        current = 0;
        consumerNode.init(frame);
    }

    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
        return consumerNode.getFinalizedState(frame);
    }

}

