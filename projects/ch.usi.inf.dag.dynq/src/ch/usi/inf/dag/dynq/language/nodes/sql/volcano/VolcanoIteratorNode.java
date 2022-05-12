package ch.usi.inf.dag.dynq.language.nodes.sql.volcano;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.nodes.Node;


public abstract class VolcanoIteratorNode extends Node implements Explainable {

  @Override
  public VolcanoIteratorNode copy() {
    return (VolcanoIteratorNode) super.copy();
  }

  public boolean isMaterializerNode() {
    return false;
  }

  public Class<?> getMaterializerClass() {
    return null;
  }

  public Class<?> getOutputRowJavaType() {
    return Object[].class;
  }

  public String explain() {
    return getClass().getSimpleName();
  }

  public static boolean isArrayType(Class<?> c) {
    return c.isArray();
  }

  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    throw new RuntimeException("acceptConsumer not yet implemented: " + this.getClass().getName());
  }
}


