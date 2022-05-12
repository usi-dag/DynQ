package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;


public abstract class CastToFloatRexTruffleNode extends RexTruffleNode {

    @Specialization
    public float executeFloat(float f) {
        return f;
    }

    @Specialization
    public float executeDouble(double d) {
        return (float) d;
    }

}
