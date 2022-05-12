package ch.usi.inf.dag.dynq_js.language;

import ch.usi.inf.dag.dynq.language.LanguageSpecificExtension;
import ch.usi.inf.dag.dynq.language.TruffleLinqLanguage;
import ch.usi.inf.dag.dynq.parser.TableScanVisitor;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagementJSLanguage;
import ch.usi.inf.dag.dynq_js.parser.JSTableScanVisitor;
import ch.usi.inf.dag.dynq_js.parser.rex.RexJSTruffleNodeVisitorFactory;


public class JSLanguageSpecificExtension extends LanguageSpecificExtension {

    public JSLanguageSpecificExtension() {
        super(new RexJSTruffleNodeVisitorFactory());
    }

    @Override
    public TruffleLinqLanguage getCurrentLanguage() {
        return TruffleLinqJSLanguage.getCurrentLanguage();
    }

    @Override
    public TableScanVisitor getTableScanVisitor() {
        return new JSTableScanVisitor();
    }

    @Override
    public APISessionManagement getApiSessionManagement() {
        return new APISessionManagementJSLanguage();
    }
}
