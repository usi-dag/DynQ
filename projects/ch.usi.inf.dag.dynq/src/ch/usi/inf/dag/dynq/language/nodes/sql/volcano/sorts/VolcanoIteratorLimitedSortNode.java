package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.sorts;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.RexComparatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.structures.truffle_pq.PqAddNode;
import ch.usi.inf.dag.dynq.structures.truffle_pq.PqPollNode;
import ch.usi.inf.dag.dynq.structures.truffle_pq.PriorityQueue;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

import java.util.List;


public class VolcanoIteratorLimitedSortNode extends VolcanoIteratorSortNode {

  private final int limit;

  public static VolcanoIteratorLimitedSortNode create(VolcanoIteratorNode child,
                                                      RexComparatorNode comparator,
                                                      int limit) {
    return new VolcanoIteratorLimitedSortNode(child, comparator, limit);
  }

  VolcanoIteratorLimitedSortNode(VolcanoIteratorNode child, RexComparatorNode comparator, int limit) {
    super(child, comparator);
    this.limit = limit;
  }


  @Override
  public boolean isMaterializerNode() {
    return true;
  }

  @Override
  public Class<?> getMaterializerClass() {
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
            ? ((limit == 1) ? new LimitedOneSortConsumerWithNoParentPQNode(comparatorNode) : new LimitedSortConsumerWithNoParentPQNode(limit, comparatorNode))
            : ((limit == 1) ? new LimitedOneSortConsumerPQNode(comparatorNode, consumer) : new LimitedSortConsumerPQNode(limit, comparatorNode, consumer));
    return child.acceptConsumer(myConsumer);
  }



  static final class LimitedSortConsumerWithNoParentPQNode extends DataCentricConsumerNode {
    private final int limit;
    private PriorityQueue<Object[]> priorityQueue;

    @Child RexComparatorNode comparatorNode;
    @Child
    PqAddNode pqAdd;

    @Child
    PqPollNode pqPoll;



    LimitedSortConsumerWithNoParentPQNode(int limit, RexComparatorNode comparatorNode) {
      this.limit = limit;
      this.comparatorNode = comparatorNode;
      comparatorNode = comparatorNode.reversed();
      this.pqAdd = new PqAddNode(comparatorNode);
      this.pqPoll = new PqPollNode(comparatorNode);
    }

    @Override
    public void init(VirtualFrame frame) {
      priorityQueue = new PriorityQueue<>(limit);
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      if(priorityQueue.size() == limit) {
        if(comparatorNode.compare(frame, row, priorityQueue.peek()) < 0) {
          pqPoll.poll(frame, priorityQueue);
          pqAdd.add(frame, priorityQueue, row);
        }
      } else {
        pqAdd.add(frame, priorityQueue, row);
      }
    }

    @Override
    public Object[][] getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      int size = Math.min(limit, priorityQueue.size());
      Object[][] result = new Object[size][];
      for (int i = size-1; i >= 0; i--) {
        result[i] = (Object[]) pqPoll.poll(frame, priorityQueue);
      }
      priorityQueue = null;
      return result;
    }
  }


  static final class LimitedOneSortConsumerWithNoParentPQNode extends DataCentricConsumerNode {
    @Child RexComparatorNode comparatorNode;
    private Object state;

    LimitedOneSortConsumerWithNoParentPQNode(RexComparatorNode comparatorNode) {
      this.comparatorNode = comparatorNode;
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      if (state != null) {
        if (row != null && comparatorNode.compare(frame, row, state) < 0) {
          state = row;
        }
      } else {
        state = row;
      }
    }

    @Override
    public void init(VirtualFrame frame) {
      state = null;
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) {
      return state;
    }
  }


  static final class LimitedSortConsumerPQNode extends DataCentricConsumerWithDestinationNode {
    private final int limit;
    private PriorityQueue<Object> priorityQueue;

    //    @Child RexComparatorNode comparatorNode;
    @Child
    PqAddNode pqAdd;

    @Child
    PqPollNode pqPoll;


    LimitedSortConsumerPQNode(int limit, RexComparatorNode comparatorNode,
                              DataCentricConsumerNode parentConsumer) {
      super(parentConsumer);
      this.limit = limit;
      this.pqAdd = new PqAddNode(comparatorNode);
      this.pqPoll = new PqPollNode(comparatorNode);
//      this.comparatorNode = comparatorNode;
    }

    @Override
    public void init(VirtualFrame frame) {
//        priorityQueue = new PriorityQueue<>(limit, getComparator(frame.materialize()));
        priorityQueue = new PriorityQueue<>(limit);
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      pqAdd.add(frame, priorityQueue, row);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      try {
        for (int i = 0; i < Math.min(limit, priorityQueue.size()); i++) {
//          destination.execute(frame, priorityQueue.poll());
          destination.execute(frame, pqPoll.poll(frame, priorityQueue));
        }
      } catch (EndOfComputation ignored) {}
      priorityQueue = null;
      return destination.getFinalizedState(frame);
    }
  }

  static final class LimitedOneSortConsumerPQNode extends DataCentricConsumerWithDestinationNode {
    private Object state;

    @Child RexComparatorNode comparatorNode;

    LimitedOneSortConsumerPQNode(RexComparatorNode comparatorNode, DataCentricConsumerNode parentConsumer) {
      super(parentConsumer);
      this.comparatorNode = comparatorNode;
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
      if (CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, state != null)) {
        if (row != null && comparatorNode.compare(frame, row, state) < 0) {
          state = row;
        }
      } else {
        state = row;
      }
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      try {
        destination.execute(frame, state);
      } catch (EndOfComputation ignored) {}
      return destination.getFinalizedState(frame);
    }
  }

}
