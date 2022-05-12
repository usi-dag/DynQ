package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.LoopNode;


public final class ColumnarTableScanWithLoopNode extends TruffleLinqExecutableNode {

  private final long nElements;
  private final ColumnarScanLoopNode scanLoop;

  @Child
  private LoopNode loopNode;

  public ColumnarTableScanWithLoopNode(long nElements, DataCentricConsumerNode consumerNode) {
    this.nElements = nElements;
    this.scanLoop = new ColumnarScanLoopNode(nElements, consumerNode);
    this.loopNode = Truffle.getRuntime().createLoopNode(scanLoop);
  }

  @Override
  public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
    scanLoop.init(frame);
    loopNode.execute(frame);
    return scanLoop.getFinalizedState(frame);
  }

  @Override
  public String explain() {
    return super.explain() + "(" + nElements + ")";
  }


}
