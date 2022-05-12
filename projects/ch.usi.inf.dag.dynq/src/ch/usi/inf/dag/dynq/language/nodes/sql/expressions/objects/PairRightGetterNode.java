package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;
import org.graalvm.collections.Pair;


public abstract class PairRightGetterNode extends RexTruffleNode {

    @Specialization
    public Object execute(Pair<?, ?> input) {
        return input.getRight();
    }

}
