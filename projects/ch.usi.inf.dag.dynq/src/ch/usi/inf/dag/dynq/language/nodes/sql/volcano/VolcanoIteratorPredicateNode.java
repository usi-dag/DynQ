package ch.usi.inf.dag.dynq.language.nodes.sql.volcano;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.predicates.PredicateNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

import java.util.List;


public class VolcanoIteratorPredicateNode extends VolcanoIteratorNode {

  @Child
  VolcanoIteratorNode child;

  @Child
  private PredicateNode predicate;

  public static VolcanoIteratorPredicateNode create(VolcanoIteratorNode child) {
    return new VolcanoIteratorPredicateNode(child);
  }

  VolcanoIteratorPredicateNode(VolcanoIteratorNode child) {
    this.child = child;
  }

  public void setPredicate(PredicateNode predicate) {
    this.predicate = predicate;
  }

  // TODO predicate child should never be a materializer! predicate should be pushed down!

  @Override
  public Class<?> getOutputRowJavaType() {
    return child.getOutputRowJavaType();
  }

  @Override
  public boolean isMaterializerNode() {
    return child.isMaterializerNode();
  }

  public Class<?> getMaterializerClass() {
    return List.class;
  }



  @Override
  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    return child.acceptConsumer(new DataCentricConsumerPredicateNode(predicate, consumer));
  }

  public final static class DataCentricConsumerPredicateNode extends DataCentricConsumerWithDestinationNode {

    @Child PredicateNode predicateNode;

    DataCentricConsumerPredicateNode(PredicateNode predicateNode, DataCentricConsumerNode parentConsumer) {
      super(parentConsumer);
      this.predicateNode = predicateNode;
    }

    @Override
    public void execute(VirtualFrame frame, Object row)
            throws InteropException, FrameSlotTypeException, EndOfComputation {
      if(predicateNode.execute(frame, row)) {
        destination.execute(frame, row);
      }
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
      super.init(frame);
    }
  }

}
