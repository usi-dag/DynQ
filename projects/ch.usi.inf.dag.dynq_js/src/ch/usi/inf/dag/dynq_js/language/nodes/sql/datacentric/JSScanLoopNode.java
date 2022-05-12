package ch.usi.inf.dag.dynq_js.language.nodes.sql.datacentric;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.JSReadElementNodeFactory;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.js.nodes.access.ReadElementNode;


public final class JSScanLoopNode extends Node implements RepeatingNode, Explainable {

    private int current = 0;
    private final long nElements;

    @Child
    JSLoopStepNode jsLoopStepNode;

    JSScanLoopNode(Object input, long nElements, DataCentricConsumerNode consumerNode) {
        this.nElements = nElements;
        this.jsLoopStepNode = JSScanLoopNodeFactory.JSLoopStepNodeGen.create(input, consumerNode);
    }

    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        try {
            jsLoopStepNode.execute(frame, current);
            return ++current < nElements;
        }
        catch (EndOfComputation ignored) {return false;}
    }

    public void reset(VirtualFrame frame) {
        current = 0;
        jsLoopStepNode.init(frame);
    }

    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
        return jsLoopStepNode.getFinalizedState(frame);
    }

    @ImportStatic({JSReadElementNodeFactory.class})
    static abstract class JSLoopStepNode extends Node implements Explainable {

        final private Object input;
        @Child
        private DataCentricConsumerNode consumerNode;

        JSLoopStepNode(Object input, DataCentricConsumerNode consumerNode) {
            this.input = input;
            this.consumerNode = consumerNode;
        }

        abstract void execute(VirtualFrame frame, int current) throws EndOfComputation;

        @Specialization
        void executeScan(VirtualFrame frame, int i,
                         @Cached(value = "getJSReadElementNode()", uncached = "getUncachedRead()") ReadElementNode readNode)
                throws EndOfComputation {
            try {
                consumerNode.execute(frame, readNode.executeWithTargetAndIndex(input, i));
            } catch (InteropException | FrameSlotTypeException ignored) {}
        }

        public void init(VirtualFrame frame) {
            consumerNode.init(frame);
        }

        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            Object result = consumerNode.getFinalizedState(frame);
            consumerNode.free(frame);
            return result;
        }

    }

}

