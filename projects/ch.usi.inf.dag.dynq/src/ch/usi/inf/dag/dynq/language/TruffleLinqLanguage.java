package ch.usi.inf.dag.dynq.language;

import ch.usi.inf.dag.dynq.runtime.objects.api.APIEntryPoint;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;


@TruffleLanguage.Registration(id = TruffleLinqLanguage.ID, name = TruffleLinqLanguage.ID, version = "0.0.1")
public class TruffleLinqLanguage extends TruffleLanguage<TruffleLinqContext>{
  public static final String ID = "TruffleLINQ";

  @Override
  protected TruffleLinqContext createContext(Env env) {
    return new TruffleLinqContext(env);
  }

  @SuppressWarnings("deprecation") // TODO
  public static TruffleLinqLanguage getCurrentLanguage() {
    return TruffleLanguage.getCurrentLanguage(TruffleLinqLanguage.class);
  }

  private static final LanguageReference<TruffleLinqLanguage> REFERENCE =
          LanguageReference.create(TruffleLinqLanguage.class);

  public static TruffleLinqLanguage get(Node node) {
    return REFERENCE.get(node);
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws TruffleLinqException {
    return Truffle.getRuntime().createCallTarget(new RootNode(this) {
      @Override
      public Object execute(VirtualFrame frame) {
        return new APIEntryPoint(new LanguageSpecificExtension());
      }
    });
  }

}

