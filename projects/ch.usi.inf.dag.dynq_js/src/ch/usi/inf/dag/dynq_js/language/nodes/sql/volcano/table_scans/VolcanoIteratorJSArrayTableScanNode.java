package ch.usi.inf.dag.dynq_js.language.nodes.sql.volcano.table_scans;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.table_scans.VolcanoIteratorPolyglotTableScanNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.datacentric.DataCentricJSTableScanCTNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.datacentric.DataCentricJSTableScanNodeGen;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.datacentric.DataCentricJSTableScanWithLoopNode;


public class VolcanoIteratorJSArrayTableScanNode extends VolcanoIteratorNode {

  // TODO get rid of nElements and read array length before scan

  final Object input;
  final long nElements;


  public static VolcanoIteratorJSArrayTableScanNode create(Object input, long nElements) {
    return new VolcanoIteratorJSArrayTableScanNode(input, nElements);
  }

  VolcanoIteratorJSArrayTableScanNode(Object input, long nElements) {
    this.input = input;
    this.nElements = nElements;
  }


  @Override
  public String explain() {
    return super.explain() + "(" + nElements + ")";
  }

  @Override
  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    if(VolcanoIteratorPolyglotTableScanNode.PUSH_TO_LOOP_NODE_CT) {
      // TODO
    }
    if(VolcanoIteratorPolyglotTableScanNode.PUSH_TO_CT) {
      return new DataCentricJSTableScanCTNode(nElements, input, consumer);
    }
    if(VolcanoIteratorPolyglotTableScanNode.PUSH_TO_LOOP_NODE) {
      return new DataCentricJSTableScanWithLoopNode(input, nElements, consumer);
    }
    return DataCentricJSTableScanNodeGen.create(input, nElements, consumer);
  }
}
