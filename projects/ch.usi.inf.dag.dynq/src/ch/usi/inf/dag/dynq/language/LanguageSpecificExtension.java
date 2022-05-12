package ch.usi.inf.dag.dynq.language;

import ch.usi.inf.dag.dynq.parser.TableScanVisitor;
import ch.usi.inf.dag.dynq.parser.TableScanVisitorFactory;
import ch.usi.inf.dag.dynq.parser.rex.RexTruffleNodeVisitorFactory;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;


public class LanguageSpecificExtension {

    private final RexTruffleNodeVisitorFactory rexTruffleNodeVisitorFactory;
    private final TableScanVisitorFactory tableScanVisitorFactory;

    public LanguageSpecificExtension() {
        this.rexTruffleNodeVisitorFactory = new RexTruffleNodeVisitorFactory();
        this.tableScanVisitorFactory = new TableScanVisitorFactory();
    }

    public LanguageSpecificExtension(RexTruffleNodeVisitorFactory rexTruffleNodeVisitorFactory) {
        this.rexTruffleNodeVisitorFactory = rexTruffleNodeVisitorFactory;
        this.tableScanVisitorFactory = new TableScanVisitorFactory();
    }

    public RexTruffleNodeVisitorFactory getRexTruffleNodeVisitorFactory() {
        return rexTruffleNodeVisitorFactory;
    }

    public TableScanVisitor getTableScanVisitor() {
        return tableScanVisitorFactory.create();
    }

    public TruffleLinqLanguage getCurrentLanguage() {
        return TruffleLinqLanguage.getCurrentLanguage();
    }

    public APISessionManagement getApiSessionManagement() {
        return new APISessionManagement();
    }
}
