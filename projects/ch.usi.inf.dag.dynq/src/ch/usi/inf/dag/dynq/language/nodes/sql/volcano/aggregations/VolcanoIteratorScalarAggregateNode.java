package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.aggregations;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationScalarAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class VolcanoIteratorScalarAggregateNode extends VolcanoIteratorNode {

  @Child
  VolcanoIteratorNode child;

  @Child
  AggregationScalarAggregatorNode aggregation;

  DataCentricConsumerNode cachedDataCentricConsumerNode;

  public VolcanoIteratorScalarAggregateNode(AggregationScalarAggregatorNode aggregation, VolcanoIteratorNode child) {
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
    if(cachedDataCentricConsumerNode == null) {
      if(consumer == null || consumer instanceof FinalizerFillList) {
        cachedDataCentricConsumerNode = new ScalarAggregationDataCentricConsumerNode(aggregation);
      } else {
        cachedDataCentricConsumerNode = new ScalarAggregationDataCentricConsumerWithParentConsumerNode(aggregation, consumer);
      }
      // TODO currently this caching is disabled because there are no queries that match this pattern
      //      this can be enabled just by removing {cachedDataCentricConsumerNode = null;}
      //      but it should be tested before
      DataCentricConsumerNode myConsumer = cachedDataCentricConsumerNode;
      cachedDataCentricConsumerNode = null;
      return child.acceptConsumer(myConsumer);
    } else {
      CacheableConsumer cacheableConsumer = (CacheableConsumer) cachedDataCentricConsumerNode;
      if(consumer == null || consumer instanceof FinalizerFillList) {
        return new CachedScalarAggregationDataCentricConsumerNode(cacheableConsumer);
      } else {
        return new CachedScalarAggregationDataCentricConsumerWithParentConsumerNode(cacheableConsumer, consumer);
      }
    }
  }

  interface CacheableConsumer {
    Object[] getFinalizedAggregation();
  }

  public static final class ScalarAggregationDataCentricConsumerNode extends DataCentricConsumerNode implements CacheableConsumer {
    Object state;
    Object[] finalized;

    @Child
    AggregationScalarAggregatorNode aggregation;

    ScalarAggregationDataCentricConsumerNode(AggregationScalarAggregatorNode aggregation) {
      this.aggregation = aggregation;
    }

    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      state = aggregation.aggregate(frame, state, row);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) {
      finalized = new Object[]{aggregation.finalize(frame, state)};
      state = null;
      return new Object[][]{finalized};
    }

    @Override
    public void init(VirtualFrame frame) {
      this.state = aggregation.getInitialState();
    }

    @Override
    public Object[] getFinalizedAggregation() {
      return finalized;
    }
  }

  public static final class CachedScalarAggregationDataCentricConsumerNode extends TruffleLinqExecutableNode {
    final CacheableConsumer cached;

    public CachedScalarAggregationDataCentricConsumerNode(CacheableConsumer cached) {
      this.cached = cached;
    }

    @Override
    public Object[] execute(VirtualFrame frame) {
      return cached.getFinalizedAggregation();
    }
  }

  public static final class ScalarAggregationDataCentricConsumerWithParentConsumerNode
          extends DataCentricConsumerWithDestinationNode implements CacheableConsumer {

    Object state;
    Object[] finalized;

    @Child AggregationScalarAggregatorNode aggregation;

    ScalarAggregationDataCentricConsumerWithParentConsumerNode(AggregationScalarAggregatorNode aggregation,
                                                               DataCentricConsumerNode parentConsumer) {
      super(parentConsumer);
      this.aggregation = aggregation;
    }

    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      state = aggregation.aggregate(frame, state, row);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      finalized = new Object[]{aggregation.finalize(frame, state)};
      try {
        destination.execute(frame, finalized);
      } catch (EndOfComputation ignored) {}
      return destination.getFinalizedState(frame);
    }

    @Override
    public void init(VirtualFrame frame) {
      state = aggregation.getInitialState();
      destination.init(frame, 1);
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
      init(frame);
    }

    @Override
    public void free(VirtualFrame frame) {
      state = null;
    }

    @Override
    public Object[] getFinalizedAggregation() {
      return finalized;
    }
  }

  public static final class CachedScalarAggregationDataCentricConsumerWithParentConsumerNode
          extends TruffleLinqExecutableNode {
    final CacheableConsumer cached;
    @Child DataCentricConsumerNode destination;

    public CachedScalarAggregationDataCentricConsumerWithParentConsumerNode(
            CacheableConsumer cached, DataCentricConsumerNode destination) {
      this.cached = cached;
      this.destination = destination;
    }

    @Override
    public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      try {
        destination.execute(frame, cached.getFinalizedAggregation());
      } catch (EndOfComputation ignored) {}
      return destination.getFinalizedState(frame);
    }
  }
}
