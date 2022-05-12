package ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.data.nodes.VectorAccess;


public abstract class RDataFrameVectorRexTruffleNode extends RexTruffleNode {

    @Child
    AccessorNode accessorNode;

    RDataFrameVectorRexTruffleNode(RAbstractVector vector) {
        RType type = vector.getRType();
        VectorAccess vectorAccess = vector.access();
        VectorAccess.RandomIterator iterator = vectorAccess.randomAccess(vector);
        switch(type) {
            case Integer:
                accessorNode = new IntAccessorNode(iterator, vectorAccess);
                break;
            case Double:
                accessorNode = new DoubleAccessorNode(iterator, vectorAccess);
                break;
            case Logical:
                accessorNode = new LogicalAccessorNode(iterator, vectorAccess);
                break;
            case Character:
                accessorNode = new CharacterAccessorNode(iterator, vectorAccess);
                break;
            default:
                CompilerDirectives.transferToInterpreter();
                throw new RuntimeException("Unexpected vector type: " + type);
        }
    }

    @Specialization
    public Object read(int index) {
        return accessorNode.execute(index);
    }

    @Specialization(guards = "o == null")
    public Object read(Object o) {
        return null;
    }


    static abstract class AccessorNode extends Node {
        @CompilerDirectives.CompilationFinal
        final VectorAccess.RandomIterator iterator;

        @Child
        VectorAccess vectorAccess;

        AccessorNode(VectorAccess.RandomIterator iterator, VectorAccess vectorAccess) {
            this.iterator = iterator;
            this.vectorAccess = vectorAccess;
        }

        abstract Object execute(int index);
    }

    static final class IntAccessorNode extends AccessorNode {
        IntAccessorNode(VectorAccess.RandomIterator iterator, VectorAccess vectorAccess) {
            super(iterator, vectorAccess);
        }

        @Override
        Object execute(int index) {
            return vectorAccess.getInt(iterator, index);
        }
    }
    static final class DoubleAccessorNode extends AccessorNode {
        DoubleAccessorNode(VectorAccess.RandomIterator iterator, VectorAccess vectorAccess) {
            super(iterator, vectorAccess);
        }

        @Override
        Object execute(int index) {
            return vectorAccess.getDouble(iterator, index);
        }
    }
    static final class LogicalAccessorNode extends AccessorNode {
        LogicalAccessorNode(VectorAccess.RandomIterator iterator, VectorAccess vectorAccess) {
            super(iterator, vectorAccess);
        }

        @Override
        Object execute(int index) {
            return vectorAccess.getLogical(iterator, index);
        }
    }
    static final class CharacterAccessorNode extends AccessorNode {
        CharacterAccessorNode(VectorAccess.RandomIterator iterator, VectorAccess vectorAccess) {
            super(iterator, vectorAccess);
        }

        @Override
        Object execute(int index) {
            return vectorAccess.getString(iterator, index);
        }
    }

}
