package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.VirtualFrame;


public final class RexDynamicParameterTruffleNode extends RexTruffleNode {

  public final int index;

  public RexDynamicParameterTruffleNode(int index) {
    this.index = index;
  }

  @Override
  public Object executeWith(VirtualFrame frame, Object input) {
    return frame.getArguments()[index];
  }
}
