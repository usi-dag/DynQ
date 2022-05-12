package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.LoopNode;


public final class DataCentricPolyglotTableScanWithLoopNode extends TruffleLinqExecutableNode {

  private final long nElements;
  private final ScanLoopNode scanLoop;

  @Child private LoopNode loopNode;


  public DataCentricPolyglotTableScanWithLoopNode(Object input, long nElements, DataCentricConsumerNode consumerNode) {
    this.nElements = nElements;
    this.scanLoop = new ScanLoopNode(input, nElements, consumerNode);
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
