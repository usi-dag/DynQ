package ch.usi.inf.dag.dynq_r.language;

import ch.usi.inf.dag.dynq.language.LanguageSpecificExtension;
import ch.usi.inf.dag.dynq.language.TruffleLinqLanguage;
import ch.usi.inf.dag.dynq.parser.TableScanVisitor;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagementR;
import ch.usi.inf.dag.dynq_r.parser.RTableScanVisitor;
import ch.usi.inf.dag.dynq_r.parser.rex.RexRTruffleNodeVisitorFactory;


public class RLanguageSpecificExtension extends LanguageSpecificExtension {

    public RLanguageSpecificExtension() {
        super(new RexRTruffleNodeVisitorFactory());
    }

    @Override
    public TruffleLinqLanguage getCurrentLanguage() {
        return TruffleLinqRLanguage.getCurrentLanguage();
    }

    @Override
    public APISessionManagement getApiSessionManagement() {
        return new APISessionManagementR();
    }

    @Override
    public TableScanVisitor getTableScanVisitor() {
        return new RTableScanVisitor();
    }
}
