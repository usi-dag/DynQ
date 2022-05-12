package ch.usi.inf.dag.dynq_js.language.nodes.sql.datacentric;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.LoopNode;


public final class DataCentricJSTableScanWithLoopNode extends TruffleLinqExecutableNode {

  private final long nElements;
  private final JSScanLoopNode scanLoop;

  @Child
  private LoopNode loopNode;


  public DataCentricJSTableScanWithLoopNode(Object input, long nElements, DataCentricConsumerNode consumerNode) {
    this.nElements = nElements;
    this.scanLoop = new JSScanLoopNode(input, nElements, consumerNode);
    this.loopNode = Truffle.getRuntime().createLoopNode(scanLoop);
  }

  @Override
  public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
    scanLoop.reset(frame);
    loopNode.execute(frame);
    return scanLoop.getFinalizedState(frame);
  }

  @Override
  public String explain() {
    return super.explain() + "(" + nElements + ")";
  }

}
