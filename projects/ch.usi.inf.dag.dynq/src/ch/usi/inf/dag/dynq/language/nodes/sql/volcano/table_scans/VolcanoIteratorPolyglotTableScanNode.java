package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.table_scans;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricPolyglotTableScanNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricPolyglotTableScanWithLoopNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.DataCentricPolyglotTableScanCTNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;


public class VolcanoIteratorPolyglotTableScanNode extends VolcanoIteratorNode {

  public static final boolean PUSH_TO_LOOP_NODE_CT = "true".equals(System.getenv("DYNQ_PUSH_TO_LOOP_NODE_CT"));
  public static final boolean PUSH_TO_CT = "true".equals(System.getenv("DYNQ_PUSH_TO_CT"));
  public static final boolean PUSH_TO_LOOP_NODE = "true".equals(System.getenv("DYNQ_PUSH_TO_LOOP_NODE"));

  final Object input;
  final long nElements;

  public static VolcanoIteratorPolyglotTableScanNode create(Object input, long nElements) {
    return new VolcanoIteratorPolyglotTableScanNode(input, nElements);
  }

  VolcanoIteratorPolyglotTableScanNode(Object input, long nElements) {
    this.input = input;
    this.nElements = nElements;
  }

  @Override
  public String explain() {
    return super.explain() + "(" + nElements + ")";
  }

  @Override
  public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
    if(PUSH_TO_LOOP_NODE_CT) {
      // TODO
    }
    if(PUSH_TO_CT) {
      return new DataCentricPolyglotTableScanCTNode(nElements, input, consumer);
    }
    if(PUSH_TO_LOOP_NODE) {
      return new DataCentricPolyglotTableScanWithLoopNode(input, nElements, consumer);
    }
    return DataCentricPolyglotTableScanNodeGen.create(input, nElements, consumer);
  }
}
