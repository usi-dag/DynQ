package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;


abstract class AbstractRightIteratorConsumerNode extends DataCentricConsumerWithDestinationNode {

    AbstractRightIteratorConsumerNode(DataCentricConsumerNode destination) {
        super(destination);
    }
}
