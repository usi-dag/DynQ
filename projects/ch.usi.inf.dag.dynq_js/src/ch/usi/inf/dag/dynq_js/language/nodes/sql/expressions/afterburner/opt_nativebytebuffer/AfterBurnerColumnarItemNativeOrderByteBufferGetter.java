package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.WithOptimizedComposedExpression;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.StringLikeRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles.DoubleRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers.IntegerRexTruffleNode;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.AfterBurnerDB;
import ch.usi.inf.dag.dynq_js.runtime.types.AfterBurnerDateWrapper;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.builtins.JSArrayBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;


public abstract class AfterBurnerColumnarItemNativeOrderByteBufferGetter extends RexTruffleNode implements WithOptimizedComposedExpression {

    public static RexTruffleNode createAccessor(DynamicObject buffer, AfterBurnerDB.Column column) {
        int offset = (int) column.getOffset();
        if(!JSArrayBuffer.isJSDirectArrayBuffer(buffer)) {
            throw new IllegalArgumentException("AfterBurnerColumnarItemNativeOrderByteBufferGetter requires direct buffer");
        }
        ByteBuffer bb = JSArrayBuffer.getDirectByteBuffer(buffer);
        if(!bb.order().equals(ByteOrder.nativeOrder())) {
            throw new IllegalArgumentException("AfterBurnerColumnarItemNativeOrderByteBufferGetter requires direct buffer with native order");
        }

        switch (column.getDataType()) {
            case INT:
                return new IntAccessorNode(bb, offset);
            case DOUBLE:
                return new DoubleAccessorNode(bb, offset);
            case STRING:
                return new StringAccessorNode(bb, offset);
            case DATE:
                return new DateAccessorNode(bb, offset);
            case CHAR:
                return new CharAccessorNode(bb, offset);
        }
        throw new IllegalArgumentException("Unknown AfterBurner column type: " + column.getDataType());
    }

    static final class IntAccessorNode extends IntegerRexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        IntAccessorNode(ByteBuffer buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
        }

        @Override
        public Integer executeWith(VirtualFrame frame, Object input) {
            if(input != null) {
                return state.readInt((int) input);
            }
            return null;
        }
    }

    static final class DoubleAccessorNode extends DoubleRexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        DoubleAccessorNode(ByteBuffer buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
        }

        @Override
        public Double executeWith(VirtualFrame frame, Object input) {
            if(input != null) {
                return state.readDouble((int) input);
            }
            return null;
        }
    }

    static final class DateAccessorNode extends RexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        DateAccessorNode(ByteBuffer buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
        }

        @Override
        public Object executeWith(VirtualFrame frame, Object input) {
            if(input != null) {
                return new AfterBurnerDateWrapper(state.readInt((int) input));
            }
            return null;
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedInDateRange(LocalDate from, LocalDate to) {
            return Optional.of(new AfterBurnerDateInRangeNode(
                    state, from.toEpochDay(), to.toEpochDay()));
        }
    }

    static final class StringAccessorNode extends RexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;

        StringAccessorNode(ByteBuffer buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
        }

        @Override
        public Object executeWith(VirtualFrame frame, Object input) {
            if(input != null) {
                int pointer = state.readInt((int)input);
                return new AfterBurnerStringWrapperNativeByteBufferUnsafe(state, pointer);
            }
            return null;
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedEquals(Object constant) {
            if(!(constant instanceof CharSequence)) {
                return Optional.empty();
            }
            String str = ((CharSequence) constant).toString();
            return Optional.of(new AfterBurnerStringEqualsNode(state, str));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedNotEquals(Object constant) {
            if(!(constant instanceof CharSequence)) {
                return Optional.empty();
            }
            String str = ((CharSequence) constant).toString();
            return Optional.of(new AfterBurnerStringNotEqualsNode(state, str));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedSubstring(int from, int len) {
            return Optional.of(new AfterBurnerSubStringNode(state, from, len));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedInConstantStringSet(Set<String> constants) {
            return Optional.of(new AfterBurnerStringInConstantStringSetNode(state, constants));
        }

        @Override
        public Optional<RexTruffleNode> getOptimizedStringLike(String reLike) {
            String regex = StringLikeRexTruffleNode.quotemeta(reLike);
            int starCount = (int) regex.chars().filter(ch -> ch == '%').count();
            if(!regex.contains("_") && starCount > 0) {
                // startsWith
                if(starCount == 1 && regex.endsWith("%")) {
                    String start = regex.substring(0, regex.length() - 1);
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringStartsWithNode(
                            state, start));
                }
                // endsWith
                if(starCount == 1 && regex.startsWith("%")) {
                    String end = regex.substring(1);
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringEndsWithNode(
                            state, end));
                }
                // contains
                if(starCount == 2 && regex.startsWith("%") && regex.endsWith("%")) {
                    String in = regex.substring(1, regex.length() - 1);
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringContainsNode(
                            state, in));
                }
                // multi-contains
                if(regex.startsWith("%") && regex.endsWith("%")) {
                    // TODO
                    String[] patterns = regex.substring(1, regex.length() - 1).split("%");
                    return Optional.of(new AfterBurnerStringLikeNode.AfterBurnerStringMultiContainsNode(
                            state, patterns));
                }
            }
            return Optional.empty();
        }

    }
    static final class CharAccessorNode extends IntegerRexTruffleNode {
        private final AfterBurnerColumnarAccessorNodeState state;
        CharAccessorNode(ByteBuffer buffer, int offset) {
            state = new AfterBurnerColumnarAccessorNodeState(buffer, offset);
        }

        @Override
        public Integer executeWith(VirtualFrame frame, Object input) {
            if(input != null) {
                return state.readInt((int) input);
            }
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
                    return compare == state.readInt((int) input);
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
