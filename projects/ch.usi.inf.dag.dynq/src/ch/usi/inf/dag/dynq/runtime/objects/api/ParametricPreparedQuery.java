package ch.usi.inf.dag.dynq.runtime.objects.api;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.RexDynamicParameterTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.DirectCallNode;


@ExportLibrary(InteropLibrary.class)
public class ParametricPreparedQuery extends AbstractAPITruffleObject {

  @CompilerDirectives.CompilationFinal
  final DirectCallNode callNode;

  public ParametricPreparedQuery(DirectCallNode callNode, RexDynamicParameterTruffleNode[] parameterNodes) {
    this.callNode = callNode;
  }

  @ExportMessage
  public boolean isExecutable() {
    return true;
  }

  @ExportMessage
  public static Object execute(ParametricPreparedQuery receiver, Object[] arguments,
                               @Cached("receiver.callNode") DirectCallNode callNode) {
    return callNode.call(arguments);
  }

}
