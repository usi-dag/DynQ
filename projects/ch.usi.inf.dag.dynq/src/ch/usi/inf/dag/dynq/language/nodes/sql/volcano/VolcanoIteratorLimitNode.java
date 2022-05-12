package ch.usi.inf.dag.dynq.language.nodes.sql.volcano;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.structures.FinalArrayList;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.object.DynamicObject;


import java.util.List;


public final class VolcanoIteratorLimitNode extends VolcanoIteratorNode {

  private final int limit;

  @Child VolcanoIteratorNode child;

  public VolcanoIteratorLimitNode(VolcanoIteratorNode child, int limit) {
    this.child = child;
    this.limit = limit;
  }

  @Override
  public boolean isMaterializerNode() {
    return true;
  }

  @Override
  public Class<?> getMaterializerClass() {
    if(limit == 1) {
      return DynamicObject.class;
    }
    if(isArrayType(child.getMaterializerClass())) {
      return Object[][].class;
    } else {
      return List.class;
    }
  }

  @Override
  public Class<?> getOutputRowJavaType() {
    return child.getOutputRowJavaType();
  }


  @Override
  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    DataCentricConsumerNode myConsumer = (consumer == null || consumer instanceof FinalizerFillList)
            ? ((limit == 1) ? new LimitOneConsumerRootNode() : new LimitConsumerRootNode(limit))
            : new LimitConsumerNode(limit, consumer);
    return child.acceptConsumer(myConsumer);
  }

  static final class LimitConsumerNode extends DataCentricConsumerWithDestinationNode {
    private final int limit;
    private int currentNumElements = 0;

    LimitConsumerNode(int limit, DataCentricConsumerNode parentConsumer) {
      super(parentConsumer);
      this.limit = limit;
    }

    @Override
    public void execute(VirtualFrame frame, Object row)
            throws EndOfComputation, InteropException, FrameSlotTypeException {
      destination.execute(frame, row);
      if(++currentNumElements == limit) {
        throw new EndOfComputation();
      }
    }

    @Override
    public void init(VirtualFrame frame) {
      currentNumElements = 0;
      super.init(frame);
    }
  }


  static final class LimitConsumerRootNode extends DataCentricConsumerNode {
    private static final EndOfComputation END = new EndOfComputation();
    private final int limit;
    private int currentNumElements = 0;
    private FinalArrayList result;

    LimitConsumerRootNode(int limit) {
      this.limit = limit;
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws EndOfComputation {
      result.add(row);
      if(++currentNumElements == limit) {
        throw END;
      }
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) {
      Object theResult = result;
      result = null;
      return theResult;
    }

    @Override
    public void init(VirtualFrame frame) {
      currentNumElements = 0;
      result = new FinalArrayList(limit);
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
      currentNumElements = 0;
      result = new FinalArrayList(exactSize);
    }

    @Override
    public void free(VirtualFrame frame) {
      result = null;
    }
  }


  static final class LimitOneConsumerRootNode extends DataCentricConsumerNode {
    private static final EndOfComputation END = new EndOfComputation();
    private Object result;

    @Override
    public void execute(VirtualFrame frame, Object row) throws EndOfComputation {
      result = row;
      throw END;
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) {
      return result;
    }

    @Override
    public void init(VirtualFrame frame) {
      result = null;
    }
  }

}
