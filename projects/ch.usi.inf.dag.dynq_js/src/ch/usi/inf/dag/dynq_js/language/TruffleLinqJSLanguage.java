package ch.usi.inf.dag.dynq_js.language;

import ch.usi.inf.dag.dynq.language.TruffleLinqContext;
import ch.usi.inf.dag.dynq.language.TruffleLinqException;
import ch.usi.inf.dag.dynq.language.TruffleLinqLanguage;
import ch.usi.inf.dag.dynq.runtime.objects.api.APIEntryPoint;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;


@TruffleLanguage.Registration(id = TruffleLinqJSLanguage.ID, name = "TruffleLINQ_JS", version = "0.0.1")
public class TruffleLinqJSLanguage extends TruffleLinqLanguage {
  public static final String ID = "TruffleLINQ_JS";

  @Override
  protected TruffleLinqContext createContext(Env env) {
    return new TruffleLinqContext(env);
  }

  @SuppressWarnings("deprecation") // TODO
  public static TruffleLinqJSLanguage getCurrentLanguage() {
    return TruffleLanguage.getCurrentLanguage(TruffleLinqJSLanguage.class);
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws TruffleLinqException {
    if(!request.getSource().getCharacters().toString().equals("API")) {
      throw new IllegalArgumentException(ID + " Offers only API as main entry point");
    }

    return Truffle.getRuntime().createCallTarget(new RootNode(this) {
      @Override
      public Object execute(VirtualFrame frame) {
        return new APIEntryPoint(new JSLanguageSpecificExtension());
      }
    });
  }

}

