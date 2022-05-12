package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class DataCentricPolyglotTableScanNode extends TruffleLinqExecutableNode {

  final private Object input;
  final private long nElements;

  @Child
  DataCentricConsumerNode consumerNode;

  public DataCentricPolyglotTableScanNode(Object input, long nElements, DataCentricConsumerNode consumerNode) {
    this.input = input;
    this.nElements = nElements;
    this.consumerNode = consumerNode;
  }

  @Specialization
  Object executeScan(VirtualFrame frame, @CachedLibrary(limit = "1") InteropLibrary interopRead) throws InteropException, FrameSlotTypeException {
    consumerNode.init(frame);
    int current = -1;
    try {
      while(++current < nElements) {
        Object row = interopRead.readArrayElement(input, current);
        consumerNode.execute(frame, row);
      }
    } catch (EndOfComputation ignored) {}
    Object result = consumerNode.getFinalizedState(frame);
    consumerNode.free(frame);
    return result;
  }

  @Override
  public String explain() {
    return super.explain() + "(" + nElements + ")";
  }

}
