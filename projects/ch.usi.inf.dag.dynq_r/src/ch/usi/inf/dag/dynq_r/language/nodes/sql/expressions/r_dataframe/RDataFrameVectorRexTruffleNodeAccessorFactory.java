package ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.r_dataframe;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles.DoubleRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers.IntegerRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.StringRexTruffleNode;
import ch.usi.inf.dag.dynq.runtime.utils.unsafe.UnsafeDataAccessor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.CharSXPWrapper;
import com.oracle.truffle.r.runtime.data.NativeDataAccess;
import com.oracle.truffle.r.runtime.data.RStringCharSXPData;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.data.nodes.VectorAccess;

import java.time.LocalDate;
import java.util.Optional;

import static com.oracle.truffle.api.CompilerDirectives.*;

public abstract class RDataFrameVectorRexTruffleNodeAccessorFactory extends RexTruffleNode {

    public static RexTruffleNode create(RAbstractVector vector) {
        try {
            RType type = vector.getRType();
            NativeDataAccess.NativeMirror nativeMirror = vector.getNativeMirror();
            long address = nativeMirror.getDataAddress();
            UnsafeDataAccessor accessor = new UnsafeDataAccessor(address);
            switch(type) {
                case Integer:
                    return new IntAccessorNode(accessor);
                case Double:
                    return new DoubleAccessorNode(accessor);
                case Character:
                    VectorAccess vectorAccess = vector.access();
                    VectorAccess.RandomIterator iterator = vectorAccess.randomAccess(vector);
                    Object data = iterator.getStore();
                    if(data instanceof CharSXPWrapper[]) {
                        return new CharacterAccessorCharSXPWrapperOptimizedNode((CharSXPWrapper[]) data);
                    } else if(data instanceof RStringCharSXPData) {
                        return new CharacterAccessorCharSXPWrapperOptimizedNode(((RStringCharSXPData) data).getData());
                    } else {
                        throw new IllegalArgumentException("CharacterAccessorNode is optimized for CharSXPWrapper[]");
                    }
                default:
                    transferToInterpreter();
                    throw new RuntimeException("Unexpected vector type: " + type);
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Cannot optimize native access, got: " + e);
        }
    }


    static final class IntAccessorNode extends IntegerRexTruffleNode {
        final UnsafeDataAccessor accessor;
        IntAccessorNode(UnsafeDataAccessor accessor) {
            this.accessor = accessor;
        }

        @Override
        public Integer executeWith(VirtualFrame frame, Object input) {
            if(injectBranchProbability(FASTPATH_PROBABILITY, input != null)) {
                return accessor.readInt((int) input);
            } else {
                return null;
            }
        }
    }

    static final class DoubleAccessorNode extends DoubleRexTruffleNode {
        final UnsafeDataAccessor accessor;
        DoubleAccessorNode(UnsafeDataAccessor accessor) {
            this.accessor = accessor;
        }

        @Override
        public Double executeWith(VirtualFrame frame, Object input) {
            if(injectBranchProbability(FASTPATH_PROBABILITY, input != null)) {
                return accessor.readDouble((int) input);
            } else {
                return null;
            }
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedInDateRange(LocalDate from, LocalDate to) {
            final long fromInt = from.toEpochDay();
            final long toInt = to.toEpochDay();

            return Optional.of(new BooleanRexTruffleNode() {
                @Override
                public boolean runBoolean(VirtualFrame frame, Object input) {
                    if(injectBranchProbability(FASTPATH_PROBABILITY, input != null)) {
                        double val = accessor.readDouble((int) input);
                        return fromInt <= val && val < toInt;
                    } else {
                        return false;
                    }
                }

                @Override
                public String explain() {
                    return "OptimizedDataFrameValueInConstantDateRange";
                }
            });
        }
    }


    static final class CharacterAccessorCharSXPWrapperOptimizedNode extends StringRexTruffleNode {

        private final CharSXPWrapper[] charSXPWrappers;

        CharacterAccessorCharSXPWrapperOptimizedNode(CharSXPWrapper[] charSXPWrappers) {
            this.charSXPWrappers = charSXPWrappers;
        }

        @Override
        public String executeWith(VirtualFrame frame, Object input) {
            if(injectBranchProbability(FASTPATH_PROBABILITY, input != null)) {
                return get((int) input);
            } else {
                return null;
            }
        }

        @TruffleBoundary
        String get(int i) {
            return charSXPWrappers[i].getContents();
        }
    }

}
