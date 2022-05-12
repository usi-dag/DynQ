package ch.usi.inf.dag.dynq.language.nodes.sql.volcano;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.projections.ProjectNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public class VolcanoIteratorProjectionNode extends VolcanoIteratorNode {

  @Child
  VolcanoIteratorNode child;

  @Child
  ProjectNode project;

  public static VolcanoIteratorProjectionNode create(ProjectNode project, VolcanoIteratorNode child) {
      return new VolcanoIteratorProjectionNode(project, child);
  }

  VolcanoIteratorProjectionNode(ProjectNode project, VolcanoIteratorNode child) {
    this.project = project;
    this.child = child;
  }

  public void setProject(ProjectNode project) {
    this.project = project;
  }

  @Override
  public boolean isMaterializerNode() {
    return child.isMaterializerNode();
  }

  @Override
  public Class<?> getMaterializerClass() {
    if(!isMaterializerNode()) {
      return null;
    }
    return child.getMaterializerClass();
  }

  @Override
  public Class<?> getOutputRowJavaType() {
    return project.getOutputRowJavaType();
  }

  @Override
  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    return child.acceptConsumer(new DataCentricConsumerProjectNode(project, consumer));
  }

  public final static class DataCentricConsumerProjectNode extends DataCentricConsumerWithDestinationNode {

    @Child ProjectNode project;

    DataCentricConsumerProjectNode(ProjectNode project, DataCentricConsumerNode parentConsumer) {
      super(parentConsumer);
      this.project = project;
    }

    @Override
    public void execute(VirtualFrame frame, Object row)
            throws InteropException, FrameSlotTypeException, EndOfComputation {
      destination.execute(frame, project.execute(frame, row));
    }

  }

}
