package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.WithOptimizedComposedExpression;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.StringLikeRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles.DoubleRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers.IntegerRexTruffleNode;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.AfterBurnerDB;
import ch.usi.inf.dag.dynq_js.runtime.types.AfterBurnerDateWrapper;
import ch.usi.inf.dag.dynq_js.runtime.types.AfterBurnerStringWrapper;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.array.TypedArray;
import com.oracle.truffle.js.runtime.array.TypedArrayFactory;
import com.oracle.truffle.js.runtime.builtins.JSArrayBuffer;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;



public abstract class AfterBurnerColumnarItemGetter extends RexTruffleNode implements WithOptimizedComposedExpression {

    public static RexTruffleNode createAccessor(DynamicObject buffer, AfterBurnerDB.Column column) {
        int offset = (int) column.getOffset();
        switch (column.getDataType()) {
            case INT:
                return AfterBurnerColumnarItemGetterFactory
                        .IntAccessorNodeGen.create(buffer, offset);
            case DOUBLE:
                return AfterBurnerColumnarItemGetterFactory
                        .DoubleAccessorNodeGen.create(buffer, offset);
            case STRING:
                return AfterBurnerColumnarItemGetterFactory
                        .StringAccessorNodeGen.create(buffer, offset);
            case DATE:
                return AfterBurnerColumnarItemGetterFactory
                        .DateAccessorNodeGen.create(buffer, offset);
            case CHAR:
                return AfterBurnerColumnarItemGetterFactory
                        .CharAccessorNodeGen.create(buffer, offset);
        }
        throw new IllegalArgumentException("Unknown AfterBurner column type: " + column.getDataType());
    }

    private static class AfterBurnerColumnarAccessorNodeState {
        final DynamicObject buffer;
        final int offset;
        final boolean isDirect;

        public AfterBurnerColumnarAccessorNodeState(DynamicObject buffer, int offset) {
            this.buffer = buffer;
            this.offset = offset;
            this.isDirect = JSArrayBuffer.isJSDirectArrayBuffer(buffer);
        }

        int getPosition(int index) {
            return (offset + (index << 2)) >> 2;
        }
    }

    static abstract class IntAccessorNode extends IntegerRexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        private final TypedArray.TypedIntArray int32Array;
        IntAccessorNode(DynamicObject buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
            this.int32Array = (TypedArray.TypedIntArray)
                    TypedArrayFactory.Int32Array.createArrayType(state.isDirect, false);
        }

        @Specialization
        Integer execute(int index) {
            return int32Array.getInt(state.buffer, state.getPosition(index), null);
        }

        @Specialization(guards = "nullObj == null")
        Integer execute(Object nullObj) {
            return null;
        }
    }

    static abstract class DoubleAccessorNode extends DoubleRexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        private final TypedArray.TypedFloatArray floatArray;
        DoubleAccessorNode(DynamicObject buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
            this.floatArray = (TypedArray.TypedFloatArray)
                    TypedArrayFactory.Float32Array.createArrayType(state.isDirect, false);
        }

        @Specialization
        Double execute(int index) {
            return floatArray.getDouble(state.buffer, state.getPosition(index), null);
        }

        @Specialization(guards = "nullObj == null")
        Double execute(Object nullObj) {
            return null;
        }
    }

    static abstract class DateAccessorNode extends RexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        private final TypedArray.TypedIntArray int32Array;
        DateAccessorNode(DynamicObject buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
            this.int32Array = (TypedArray.TypedIntArray)
                    TypedArrayFactory.Int32Array.createArrayType(state.isDirect, false);
        }

        @Specialization
        AfterBurnerDateWrapper execute(int index) {
            return new AfterBurnerDateWrapper(int32Array.getInt(state.buffer, state.getPosition(index), null));
        }

        @Specialization(guards = "nullObj == null")
        Object execute(Object nullObj) {
            return null;
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedInDateRange(LocalDate from, LocalDate to) {
            return Optional.of(new AfterBurnerDateInRangeNode(
                    int32Array, state.buffer, state.offset, from.toEpochDay(), to.toEpochDay()));
        }
    }

    static abstract class StringAccessorNode extends RexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        private final TypedArray.TypedIntArray int32Array;
        private final TypedArray.TypedIntArray int8Array;

        StringAccessorNode(DynamicObject buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
            this.int32Array = (TypedArray.TypedIntArray)
                    TypedArrayFactory.Int32Array.createArrayType(state.isDirect, false);
            this.int8Array = (TypedArray.TypedIntArray)
                    TypedArrayFactory.Int8Array.createArrayType(state.isDirect, false);
        }

        @Specialization
        AfterBurnerStringWrapper execute(int index) {
            int pointer = int32Array.getInt(state.buffer, state.getPosition(index), null);
            return new AfterBurnerStringWrapper(state.buffer, int8Array, pointer);
        }

        @Specialization(guards = "nullObj == null")
        Object execute(Object nullObj) {
            return null;
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedEquals(Object constant) {
            if(!(constant instanceof CharSequence)) {
                return Optional.empty();
            }
            String str = ((CharSequence) constant).toString();
            return Optional.of(new AfterBurnerStringEqualsNode(int32Array, int8Array, state.buffer, state.offset, str));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedNotEquals(Object constant) {
            if(!(constant instanceof CharSequence)) {
                return Optional.empty();
            }
            String str = ((CharSequence) constant).toString();
            return Optional.of(new AfterBurnerStringNotEqualsNode(int32Array, int8Array, state.buffer, state.offset, str));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedSubstring(int from, int len) {
            return Optional.of(new AfterBurnerSubStringNode(int32Array, int8Array, state.buffer, state.offset, from, len));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedInConstantStringSet(Set<String> constants) {
            return Optional.of(new AfterBurnerStringInConstantStringSetNode(int32Array, int8Array, state.buffer, state.offset, constants));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedStringLike(String reLike) {
            String regex = StringLikeRexTruffleNode.quotemeta(reLike);
            int starCount = (int) regex.chars().filter(ch -> ch == '%').count();
            if(!regex.contains("_") && starCount > 0) {
                DynamicObject buffer = state.buffer;
                int offset = state.offset;
                // startsWith
                if(starCount == 1 && regex.endsWith("%")) {
                    String start = regex.substring(0, regex.length() - 1);
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringStartsWithNode(
                            int32Array, int8Array, buffer, offset, start));
                }
                // endsWith
                if(starCount == 1 && regex.startsWith("%")) {
                    String end = regex.substring(1);
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringEndsWithNode(
                            int32Array, int8Array, buffer, offset, end));
                }
                // contains
                if(starCount == 2 && regex.startsWith("%") && regex.endsWith("%")) {
                    String in = regex.substring(1, regex.length() - 1);
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringContainsNode(
                            int32Array, int8Array, buffer, offset, in));
                }
                // multi-contains
                if(regex.startsWith("%") && regex.endsWith("%")) {
                    // TODO
                    String[] patterns = regex.substring(1, regex.length() - 1).split("%");
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringMultiContainsNode(
                            int32Array, int8Array, buffer, offset, patterns));
                }
            }
            return Optional.empty();
        }

    }
    static abstract class CharAccessorNode extends IntegerRexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        private final TypedArray.TypedIntArray int32Array;
        CharAccessorNode(DynamicObject buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
            this.int32Array = (TypedArray.TypedIntArray)
                    TypedArrayFactory.Int32Array.createArrayType(state.isDirect, false);
        }

        @Specialization
        Integer execute(int index) {
            return int32Array.getInt(state.buffer, state.getPosition(index), null);
        }

        @Specialization(guards = "nullObj == null")
        Integer execute(Object nullObj) {
            return null;
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedEquals(Object constant) {
            char constantChar;
            if(constant instanceof Character) constantChar = (char) constant;
            else if(constant instanceof String && ((String)constant).length() == 1) constantChar = ((String)constant).charAt(0);
            else if(constant instanceof Integer && ((int) constant) <= Character.MAX_VALUE) constantChar = (char) constant;
            else {
                return Optional.empty();
            }
            BooleanRexTruffleNode rexTruffleNode = new BooleanRexTruffleNode() {
                @CompilerDirectives.CompilationFinal
                private final char compare = constantChar;

                @Override
                public boolean runBoolean(VirtualFrame frame, Object input) {
                    int index = (int) input;
                    int value = int32Array.getInt(state.buffer, state.getPosition(index), null);
                    return value == compare;
                }

                @Override
                public String explain() {
                    return "AfterBurnerOptimizedCharEq(" + constant + ")";
                }
            };
            return Optional.of(rexTruffleNode);
        }
    }
}
