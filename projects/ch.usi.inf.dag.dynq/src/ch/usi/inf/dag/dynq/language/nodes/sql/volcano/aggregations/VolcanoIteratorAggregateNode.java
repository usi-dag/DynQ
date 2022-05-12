package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.aggregations;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationMultipleAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;



public class VolcanoIteratorAggregateNode extends VolcanoIteratorNode {

  @Child
  VolcanoIteratorNode child;

  @Child
  AggregationMultipleAggregatorNode aggregation;

  // TODO: AggregationCollectorNode currently always returns an array:
  //  either generalize or change AggregationCollectorNode.execute (i.e., returns array)

  public static VolcanoIteratorAggregateNode create(AggregationMultipleAggregatorNode aggregation,
                                                    VolcanoIteratorNode child) {
    return new VolcanoIteratorAggregateNode(aggregation, child);
  }

  VolcanoIteratorAggregateNode(AggregationMultipleAggregatorNode aggregation, VolcanoIteratorNode child) {
    this.aggregation = aggregation;
    this.child = child;
  }

  @Override
  public Class<?> getOutputRowJavaType() {
    // TODO revise this, it should depend on aggregation node
    return Object[].class;
  }

  @Override
  public Class<?> getMaterializerClass() {
    // TODO revise this, it should depend on aggregation node
    return Object[].class;
  }

  @Override
  public boolean isMaterializerNode() {
    return true;
  }

  @Override
  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    if(consumer == null || consumer instanceof FinalizerFillList) {
      AggregationDataCentricConsumerNode myConsumer = new AggregationDataCentricConsumerNode(aggregation);
      return child.acceptConsumer(myConsumer);
    } else {
      DataCentricConsumerNode myConsumer = new AggregationDataCentricConsumerWithParentConsumerNode(aggregation, consumer);
      return child.acceptConsumer(myConsumer);
    }
  }

  public static final class AggregationDataCentricConsumerNode extends DataCentricConsumerNode {
    Object[] state;

    @Child
    AggregationMultipleAggregatorNode aggregation;

    AggregationDataCentricConsumerNode(AggregationMultipleAggregatorNode aggregation) {
      this.aggregation = aggregation;
    }

    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      aggregation.aggregate(frame, state, row);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) {
      Object[] finalized = aggregation.finalize(frame, state);
      return new Object[][]{finalized};
    }

    @Override
    public void init(VirtualFrame frame) {
      state = aggregation.getInitialState();
    }

    @Override
    public void free(VirtualFrame frame) {
      super.free(frame);
      state = null;
    }
  }

  public static final class AggregationDataCentricConsumerWithParentConsumerNode extends DataCentricConsumerWithDestinationNode {

    Object[] state;

    @Child AggregationMultipleAggregatorNode aggregation;

    AggregationDataCentricConsumerWithParentConsumerNode(AggregationMultipleAggregatorNode aggregation,
                                                         DataCentricConsumerNode parentConsumer) {
      super(parentConsumer);
      this.aggregation = aggregation;
    }

    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      aggregation.aggregate(frame, state, row);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      destination.init(frame, 1);
      Object finalized = aggregation.finalize(frame, state);
      try {
        destination.execute(frame, finalized);
      } catch (EndOfComputation ignored) {}
      return destination.getFinalizedState(frame);
    }

    @Override
    public void init(VirtualFrame frame) {
      this.state = aggregation.getInitialState();
    }

    @Override
    public void free(VirtualFrame frame) {
      this.state = null;
      super.free(frame);
    }
  }
}
