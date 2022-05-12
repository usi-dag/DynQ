package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.aggregations;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby.HashGroupByOperatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby.HashGroupByOperatorWithConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby.HashGroupByState;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public class VolcanoIteratorHashGroupByFastHashNode extends VolcanoIteratorNode {

  // TODO generalize this class (almost equal to other group by)

  @Child
  VolcanoIteratorNode child;

  @Child
  HashGroupByOperatorNode aggregation;

  HashGroupByFastHashDataCentricConsumerNode cachedConsumerNode;

  public static VolcanoIteratorHashGroupByFastHashNode create(HashGroupByOperatorNode aggregation, VolcanoIteratorNode child) {
    return new VolcanoIteratorHashGroupByFastHashNode(aggregation, child);
  }

  VolcanoIteratorHashGroupByFastHashNode(HashGroupByOperatorNode aggregation, VolcanoIteratorNode child) {
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
    if(cachedConsumerNode == null) {
      if(consumer == null || consumer instanceof FinalizerFillList) {
        cachedConsumerNode = new HashGroupByFastHashDataCentricConsumerNode(aggregation);
        return child.acceptConsumer(cachedConsumerNode);
      } else if(aggregation.hasDefaultFinalizer()) {
        HashGroupByOperatorWithConsumerNode newAgg = aggregation.acceptConsumer(consumer);
        cachedConsumerNode = new HashGroupByFastHashDataCentricConsumerNode(newAgg);
        return child.acceptConsumer(cachedConsumerNode);
      } else {
        throw new RuntimeException("Not Implemented: VolcanoIteratorHashGroupByFastHashNode with non-null consumer");
      }
    } else {
      cachedConsumerNode.keepStateForCaching = true;
      HashGroupByOperatorWithConsumerNode newAgg = aggregation.acceptConsumer(consumer);
      return new CachedHashGroupByFastHashTruffleExecutableNode(cachedConsumerNode, newAgg);
    }
  }

  private static final class HashGroupByFastHashDataCentricConsumerNode extends DataCentricConsumerNode {
    HashGroupByState state;

    @Child
    HashGroupByOperatorNode aggregation;

    @CompilerDirectives.CompilationFinal
    boolean keepStateForCaching = false;

    public HashGroupByFastHashDataCentricConsumerNode(HashGroupByOperatorNode aggregation) {
      this.aggregation = aggregation;
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      aggregation.execute(frame, this.state, row);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      // clean some space before returning aggregated data
      Object result = aggregation.finalize(frame, state);
      if(!keepStateForCaching) {
        this.state = null;
      }
      return result;
    }

    @Override
    public void init(VirtualFrame frame) {
      this.state = aggregation.getInitialState();
      aggregation.init(frame);
    }

    @Override
    public void init(VirtualFrame frame, int size) {
      this.state = aggregation.getInitialState(size);
      aggregation.init(frame);
    }
  }

  private static final class CachedHashGroupByFastHashTruffleExecutableNode extends TruffleLinqExecutableNode {
    private final HashGroupByFastHashDataCentricConsumerNode groupBy;

    @Child
    HashGroupByOperatorNode aggregation;

    public CachedHashGroupByFastHashTruffleExecutableNode(
            HashGroupByFastHashDataCentricConsumerNode groupBy, HashGroupByOperatorNode aggregation) {
      this.groupBy = groupBy;
      this.aggregation = aggregation;
    }

    @Override
    public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      aggregation.init(frame);
      return aggregation.finalize(frame, groupBy.state);
    }
  }

}
