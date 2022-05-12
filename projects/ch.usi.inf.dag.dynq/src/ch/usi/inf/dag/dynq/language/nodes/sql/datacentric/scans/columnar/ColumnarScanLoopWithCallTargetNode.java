package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.RootNode;


public final class ColumnarScanLoopWithCallTargetNode extends Node implements RepeatingNode, Explainable {


    static final int MORSEL_SIZE = Integer.parseInt(System.getenv().getOrDefault("DYNQ_CT_MORSEL_SIZE", "1000"));

    final private int nElements;
    private int current = 0;

    @Child private DataCentricConsumerNode consumerNode;

    @Child private DirectCallNode loopCall;

    ColumnarScanLoopWithCallTargetNode(long nElements, DataCentricConsumerNode consumerNode) {
        this.nElements = (int) nElements;
        this.consumerNode = consumerNode;
        RScanLoopWithCallTargetNodeMainLoopRootNode mainLoopRootNode = new RScanLoopWithCallTargetNodeMainLoopRootNode(consumerNode);
        CallTarget ct = Truffle.getRuntime().createCallTarget(mainLoopRootNode);
        this.loopCall = Truffle.getRuntime().createDirectCallNode(ct);
        loopCall.forceInlining();
    }

    ColumnarScanLoopWithCallTargetNode(long nElements, DataCentricConsumerNode consumerNode, int current) {
        this(nElements, consumerNode);
        this.current = current;
    }

    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        try {
            loopCall.call();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof EndOfComputation) {
                return false;
            }
            CompilerDirectives.transferToInterpreter();
            if (!(cause instanceof InteropException |
                    cause instanceof FrameSlotTypeException)) {
                throw e;
            }
        }
        return current < nElements;
    }

    public void init(VirtualFrame frame) {
        current = 0;
        consumerNode.init(frame, nElements);
    }

    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
        Object result = consumerNode.getFinalizedState(frame);
        consumerNode.free(frame);
        return result;
    }


    class RScanLoopWithCallTargetNodeMainLoopRootNode extends RootNode {

        protected RScanLoopWithCallTargetNodeMainLoopRootNode(DataCentricConsumerNode consumerNode) {
            super(null);
            this.consumerNode = consumerNode;
        }

        @Child
        private DataCentricConsumerNode consumerNode;

        @Override
        public Object execute(VirtualFrame frame) {
            int end = Math.min(MORSEL_SIZE, nElements - current);
            try {
                for (int i = 0; i < end; i++) {
                    consumerNode.execute(frame, current++);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}

