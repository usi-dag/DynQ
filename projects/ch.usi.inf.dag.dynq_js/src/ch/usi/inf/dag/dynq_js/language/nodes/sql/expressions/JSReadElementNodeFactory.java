package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.nodes.access.ReadElementNode;
import com.oracle.truffle.js.runtime.JSContext;


public class JSReadElementNodeFactory {
    public static ReadElementNode getJSReadElementNode() {
        return ReadElementNode.create(getJSContext());
    }

    @SuppressWarnings("deprecation") // TODO
    public static JSContext getJSContext() {
        return TruffleLanguage.LanguageReference.create(JavaScriptLanguage.class).get().getJSContext();
    }
}
