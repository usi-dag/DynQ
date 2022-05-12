package ch.usi.inf.dag.dynq_js.language.nodes.sql.datacentric;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.JSReadElementNodeFactory;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.js.nodes.access.ReadElementNode;


@ImportStatic({JSReadElementNodeFactory.class})
public abstract class DataCentricJSTableScanNode extends TruffleLinqExecutableNode {

  final Object input;
  final int nElements;

  @Child
  DataCentricConsumerNode consumerNode;

  public DataCentricJSTableScanNode(Object input, long nElements, DataCentricConsumerNode consumerNode) {
    this.input = input;
    this.nElements = (int) nElements;
    this.consumerNode = consumerNode;
  }

  @Specialization
  Object executeScan(VirtualFrame frame,
                     @Cached(value = "getJSReadElementNode()", uncached = "getUncachedRead()") ReadElementNode readNode)
  throws InteropException, FrameSlotTypeException {

    consumerNode.init(frame, nElements);
    try {
      for (int i = 0; i < nElements; i++) {
        consumerNode.execute(frame, readNode.executeWithTargetAndIndex(input, i));
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
