package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;

import ch.usi.inf.dag.dynq.language.nodes.sql.operators.predicates.PredicateNode;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;


public abstract class DataCentricPolyglotTableScanWithPredicateNode extends TruffleLinqExecutableNode {

  final private Object input;
  final private long nElements;

  final private ConditionProfile predicateProfiler = ConditionProfile.createCountingProfile();

  @Child
  PredicateNode predicateNode;

  @Child
  DataCentricConsumerNode consumerNode;

  public DataCentricPolyglotTableScanWithPredicateNode(Object input, long nElements, PredicateNode predicateNode, DataCentricConsumerNode consumerNode) {
    this.input = input;
    this.nElements = nElements;
    this.predicateNode = predicateNode;
    this.consumerNode = consumerNode;
  }

  @Specialization
  Object executeScan(VirtualFrame frame, @CachedLibrary(limit = "1") InteropLibrary interopRead) throws InteropException, FrameSlotTypeException {
    consumerNode.init(frame);
    int current = -1;
    try {
      while(++current < nElements) {
        Object row = interopRead.readArrayElement(input, current);
        if(predicateProfiler.profile(predicateNode.execute(frame, row))) {
          consumerNode.execute(frame, row);
        }
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
