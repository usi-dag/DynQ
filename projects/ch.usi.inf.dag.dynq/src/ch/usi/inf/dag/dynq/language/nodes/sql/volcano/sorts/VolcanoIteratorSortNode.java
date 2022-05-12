package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.sorts;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.RexComparatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.structures.FinalArrayList;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class VolcanoIteratorSortNode extends VolcanoIteratorNode {

  @Child
  VolcanoIteratorNode child;

  @Child
  RexComparatorNode comparatorNode;

  public static VolcanoIteratorSortNode create(VolcanoIteratorNode child, RexComparatorNode comparator) {
    return new VolcanoIteratorSortNode(child, comparator);
  }

  VolcanoIteratorSortNode(VolcanoIteratorNode child, RexComparatorNode comparator) {
    this.child = child;
    this.comparatorNode = comparator;
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

  public VolcanoIteratorLimitedSortNode asLimited(int limit) {
    return VolcanoIteratorLimitedSortNode.create(child, comparatorNode, limit);
  }

  @Override
  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    if(consumer == null || consumer instanceof FinalizerFillList) {
      return new TruffleLinqExecutableSortNode(child.acceptConsumer(consumer), comparatorNode);
    }
    DataCentricConsumerNode myConsumer = new SortConsumerNode(consumer, comparatorNode);
    return child.acceptConsumer(myConsumer);
  }
  static final class TruffleLinqExecutableSortNode extends TruffleLinqExecutableNode {

    @Child TruffleLinqExecutableNode linqExecutableNode;

    @Child RexComparatorNode comparatorNode;

    public TruffleLinqExecutableSortNode(TruffleLinqExecutableNode linqExecutableNode, RexComparatorNode comparatorNode) {
      this.linqExecutableNode = linqExecutableNode;
      this.comparatorNode = comparatorNode;
    }

      @Override
      public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
          Object result = linqExecutableNode.execute(frame);
          if(result instanceof Object[][]) {
              Arrays.sort((Object[]) result, getComparator(frame));
              return result;
          } else if(result instanceof List) {
            ((List<?>)result).sort(getComparator(frame));
            return result;
          }
          throw new RuntimeException("Unexpected type for materializer child: " + result.getClass());
      }

    // TODO fix ugly copy-paste from enclosing class
    Comparator<Object> getComparator(VirtualFrame frame) {
      return (o1, o2) -> {
        try {
          return comparatorNode.compare(frame, o1, o2);
        } catch (InteropException | FrameSlotTypeException e) {
          throw new RuntimeException(e);
        }
      };
    }
  }


  static final class SortConsumerNode extends DataCentricConsumerWithDestinationNode {
    static private final int INITIAL = 1024;

    @Child RexComparatorNode comparatorNode;

    private FinalArrayList result;


    public SortConsumerNode(DataCentricConsumerNode parentConsumer,
                            RexComparatorNode comparatorNode) {
      super(parentConsumer);
      this.comparatorNode = comparatorNode;
    }

    @Override
    public void init(VirtualFrame frame) {
      super.init(frame);
      result = new FinalArrayList(INITIAL);
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
      result = new FinalArrayList(exactSize);
      super.init(frame, exactSize);
    }

    @Override
    public void free(VirtualFrame frame) {
      result = null;
      super.free(frame);
    }

    @Override
    public void execute(VirtualFrame frame, Object row) {
      result.add(row);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
      result.sort(getComparator(frame));
      try {
        for (int i = 0; i < result.size(); i++) {
          destination.execute(frame, result.get(i));
        }
      } catch (EndOfComputation ignored) {}
      result = null;
      return destination.getFinalizedState(frame);
    }

    Comparator<Object> getComparator(VirtualFrame frame) {
      return (o1, o2) -> {
        try {
          return comparatorNode.compare(frame, o1, o2);
        } catch (InteropException | FrameSlotTypeException e) {
          CompilerDirectives.transferToInterpreter();
          throw new RuntimeException(e);
        }
      };
    }
  }



}
