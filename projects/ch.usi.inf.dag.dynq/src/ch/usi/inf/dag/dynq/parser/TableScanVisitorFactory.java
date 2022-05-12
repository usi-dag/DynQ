package ch.usi.inf.dag.dynq.parser;


public class TableScanVisitorFactory {

    public TableScanVisitor create() {
        return new TableScanVisitorImpl();
    }

}
