package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;


public final class ColumnarTableScanCTNode extends TruffleLinqExecutableNode {

  static final int MORSEL_SIZE = Integer.parseInt(System.getenv().getOrDefault("DYNQ_CT_MORSEL_SIZE", "1000"));

  private final int nElements;
  int current;

  @Child DataCentricConsumerNode consumerNode;
  @Child DirectCallNode loopCall;

  public ColumnarTableScanCTNode(long nElements, DataCentricConsumerNode consumerNode) {
    this.nElements = (int) nElements;
    this.consumerNode = consumerNode;
    DataCentricRDataFrameTableScanCTNodeMainLoopRootNode mainLoopRootNode = new DataCentricRDataFrameTableScanCTNodeMainLoopRootNode(consumerNode);
    CallTarget ct = Truffle.getRuntime().createCallTarget(mainLoopRootNode);
    this.loopCall = Truffle.getRuntime().createDirectCallNode(ct);
  }

  @Override
  public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
    consumerNode.init(frame, nElements);
    current = 0;
    try {
      while(current < nElements) {
        loopCall.call();
      }
    } catch (RuntimeException e) {
      if(!(e.getCause() instanceof EndOfComputation)) {
        CompilerDirectives.transferToInterpreter();
        throw e;
      }
    }
    Object result = consumerNode.getFinalizedState(frame);
    consumerNode.free(frame);
    return result;
  }

  @Override
  public String explain() {
    return super.explain() + "(" + nElements + ")";
  }


  class DataCentricRDataFrameTableScanCTNodeMainLoopRootNode extends RootNode {

    protected DataCentricRDataFrameTableScanCTNodeMainLoopRootNode(DataCentricConsumerNode consumerNode) {
      super(null);
      this.consumerNode = consumerNode;
    }

    @Child
    private DataCentricConsumerNode consumerNode;

    @Override
    public Object execute(VirtualFrame frame) {
      try {
        int end = Math.min(MORSEL_SIZE, nElements - current);
        for (int i = 0; i < end; i++) {
          consumerNode.execute(frame, current++);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return null;
    }
  }


  // TODO:
  //  think about the following implementation (note: the consumerNode is defined only in this class, not in the outer one)
  //  it should be better using this implementation, however it gets deoptimized for "Profiled return type" (CONTINUE)
  /*
  static final class DataCentricRDataFrameTableScanCTNodeMainLoopRootNode extends RootNode {
    private static final EndOfComputation END = new EndOfComputation();
    private final int nElements;
    int current;

    private DataCentricRDataFrameTableScanCTNodeMainLoopRootNode(DataCentricConsumerNode consumerNode, long nElements) {
      super(TruffleLinqRLanguage.getCurrentLanguage());
      this.consumerNode = consumerNode;
      this.nElements = (int) nElements;
    }

    @Child
    private DataCentricConsumerNode consumerNode;

    @Override
    public Object execute(VirtualFrame frame) {
      int _current = current;
      int end = Math.min(_current + MORSEL_SIZE, nElements);

      try {
        for (int i = _current; i < end; i++) {
          consumerNode.execute(frame, i);
        }
      } catch (EndOfComputation ignore) {
      } catch (Exception e) {
        CompilerDirectives.transferToInterpreter();
        throw new RuntimeException(e);
      }

      current = end;
      try {
        return end < nElements ? CONTINUE : consumerNode.getFinalizedState(frame);
      } catch (InteropException | FrameSlotTypeException e) {
        CompilerDirectives.transferToInterpreter();
        throw new RuntimeException(e);
      }
    }
  }
   */

}
