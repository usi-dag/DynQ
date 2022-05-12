package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class ColumnarTableScanNode extends TruffleLinqExecutableNode {

  private final int nElements;

  @Child
  DataCentricConsumerNode consumerNode;


  public ColumnarTableScanNode(long nElements, DataCentricConsumerNode consumerNode) {
    this.nElements = (int)nElements;
    this.consumerNode = consumerNode;
  }

  @Override
  public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
    consumerNode.init(frame, nElements);
    int current = -1;
    try {
      while(++current < nElements) {
        consumerNode.execute(frame, current);
      }
    } catch (EndOfComputation ignored) {}
    Object result = consumerNode.getFinalizedState(frame);
    consumerNode.free(frame);
    return result;  }

  @Override
  public String explain() {
    return super.explain() + "(" + nElements + ")";
  }


}
