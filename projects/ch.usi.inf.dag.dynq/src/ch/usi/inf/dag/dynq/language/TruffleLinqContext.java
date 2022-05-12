package ch.usi.inf.dag.dynq.language;


import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.nodes.Node;


public class TruffleLinqContext {

  public TruffleLinqContext(Env env) {
  }

  private static final TruffleLanguage.ContextReference<TruffleLinqContext> REFERENCE =
          TruffleLanguage.ContextReference.create(TruffleLinqLanguage.class);

  public static TruffleLinqContext get(Node node) {
    return REFERENCE.get(node);
  }

}
