package ch.usi.inf.dag.dynq_js.parser;

import ch.usi.inf.dag.dynq.parser.TableScanVisitor;
import ch.usi.inf.dag.dynq.parser.TableScanVisitorFactory;


public class JSTableScanVisitorFactory extends TableScanVisitorFactory {

    @Override
    public TableScanVisitor create() {
        return new JSTableScanVisitor();
    }

}
