package ch.usi.inf.dag.dynq_js.parser.rex;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions.StringStartsUnsensitiveWithRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions.StringStartsWithRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ConstRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.UpperCaseStringRexTruffleNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.predicates.constant_range_predicates.LocalDateInLeftOpenRangeNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DivRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.MulRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.SubRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.rex.RexTruffleNodeVisitor;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.dates.ExtractFromDateRexTruffleNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.dates.LocalDateWrapperConstRexTruffleNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.functions.HaversineUDFNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.functions.HaversineUDFNodeGen;
import com.google.common.collect.ImmutableMap;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.apache.calcite.avatica.util.TimeUnitRange;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexProgram;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;

import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.EqualsRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.GreaterThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.GreaterThanRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.LessThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.LessThanRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.NotEqualsRexTruffleNodeGen;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import static ch.usi.inf.dag.dynq.parser.rex.RexConversionUtils.asLocalDate;


public class RexJSTruffleNodeVisitor extends RexTruffleNodeVisitor {
    Map<SqlKind, BinaryOperator<RexTruffleNode>> BINARY_OPERATORS = new ImmutableMap.Builder<SqlKind, BinaryOperator<RexTruffleNode>>()
            // Arithmetic
            .put(SqlKind.PLUS, wrapBinOp(AddRexTruffleNodeGen::create))
            .put(SqlKind.TIMES, wrapBinOp(MulRexTruffleNodeGen::create))
            .put(SqlKind.MINUS, wrapBinOp(SubRexTruffleNodeGen::create))
            .put(SqlKind.DIVIDE, wrapBinOp(DivRexTruffleNodeGen::create))

            // Relations
            .put(SqlKind.EQUALS, wrapBinOp(EqualsRexTruffleNodeGen::create))
            .put(SqlKind.NOT_EQUALS, wrapBinOp(NotEqualsRexTruffleNodeGen::create))
            .put(SqlKind.LESS_THAN, wrapBinOp(LessThanRexTruffleNodeGen::create))
            .put(SqlKind.LESS_THAN_OR_EQUAL, wrapBinOp(LessThanEqualRexTruffleNodeGen::create))
            .put(SqlKind.GREATER_THAN, wrapBinOp(GreaterThanRexTruffleNodeGen::create))
            .put(SqlKind.GREATER_THAN_OR_EQUAL, wrapBinOp(GreaterThanEqualRexTruffleNodeGen::create))

            // Logic -- skipped since AND and OR do not need folding, as they are designed as ALL and ANY with multiple children
            .build();

    protected RexJSTruffleNodeVisitor(APISessionManagement session, RelNodeVolcanoWrapper relNode, RexProgram rexProgram) {
        super(session, relNode, rexProgram);
    }

    protected RexJSTruffleNodeVisitor(APISessionManagement session, RelNodeVolcanoWrapper relNode) {
        super(session, relNode);
    }

    @Override
    protected RexTruffleNode resolveConstant(RexLiteral rexLiteral) {
        if(rexLiteral.getTypeName() == SqlTypeName.DATE) {
            Calendar calendar = rexLiteral.getValueAs(Calendar.class);
            return new LocalDateWrapperConstRexTruffleNode(asLocalDate(calendar));
        }
        if(rexLiteral.getType().getSqlTypeName() == SqlTypeName.FLOAT) {
            float val = rexLiteral.getValueAs(Float.class);
            return ConstRexTruffleNode.create(val);
        }
        return super.resolveConstant(rexLiteral);
    }

    @Override
    protected RexTruffleNode resolveSqlBinaryOp(List<? extends RexTruffleNode> children, RexCall rexCall) {
        SqlKind kind = rexCall.getKind();
        if(BINARY_OPERATORS.containsKey(kind)) {
            return foldLeftBinOp(new LinkedList<>(children), BINARY_OPERATORS.get(kind));
        }
        return super.resolveSqlBinaryOp(children, rexCall);
    }

    @Override
    public RexTruffleNode visitCall(RexCall rexCall) {
        if(rexCall.getKind() == SqlKind.EXTRACT) {
            // Expecting two operands: a flag (i.e.. what to be extracted) and an expression (i.e., where to extract)
            if(rexCall.getOperands().size() != 2) {
                throw new RuntimeException("Expecting exactly two operands for EXTRACT operation");
            }
            RexNode fst = deref(rexCall.getOperands().get(0));
            if(!(fst instanceof RexLiteral)) {
                throw new RuntimeException("Expecting first operand for EXTRACT operation to be a RexLiteral");
            }
            RexLiteral fstLiteral = (RexLiteral) fst;
            if(!(fstLiteral.getValue() instanceof TimeUnitRange)) {
                throw new RuntimeException("Expecting first operand for EXTRACT operation to be a TimeUnitRange");
            }
            RexNode snd = rexCall.getOperands().get(1);
            // TODO allow only DATE or ANY
//            if(!(snd.getType().getSqlTypeName() == SqlTypeName.DATE)) {
//                throw new RuntimeException("Expecting second operand for EXTRACT operation to be an expression of type Date");
//            }
            TimeUnitRange what = (TimeUnitRange) fstLiteral.getValue();
            RexTruffleNode from = snd.accept(this);
            switch (what) {
                case YEAR: return new ExtractFromDateRexTruffleNode.Year(from);
                case MONTH: return new ExtractFromDateRexTruffleNode.Month(from);
                default:
                    throw new RuntimeException("Unknown TimeUnitRange: " + what);
            }
        }

        if(rexCall.op.getName().equals("STARTS_WITH")) {
            if(rexCall.getOperands().size() != 2) {
                throw new RuntimeException("Expecting exactly 2 operands for STARTS_WITH operation");
            }
            RexTruffleNode stringGetter = rexCall.getOperands().get(0).accept(this);
            RexTruffleNode patternGetter = rexCall.getOperands().get(1).accept(this);
            return new StringStartsWithRexTruffleNode(stringGetter, patternGetter);
        }
        if(rexCall.op.getName().equals("STARTS_WITH_UNSENSITIVE")) {
            if(rexCall.getOperands().size() != 2) {
                throw new RuntimeException("Expecting exactly 2 operands for STARTS_WITH operation");
            }
            RexTruffleNode stringGetter = rexCall.getOperands().get(0).accept(this);
            RexTruffleNode patternGetter = rexCall.getOperands().get(1).accept(this);
            return new StringStartsUnsensitiveWithRexTruffleNode(stringGetter, patternGetter);
        }
        if(rexCall.op.getName().equals("UPPER")) {
            if(rexCall.getOperands().size() != 1) {
                throw new RuntimeException("Expecting exactly 1 operands for UPPER operation");
            }
            return new UpperCaseStringRexTruffleNode(rexCall.getOperands().get(0).accept(this));
        }
        // TODO create a mechanism for registering Java UDF and use that for registering this UDF
        if(rexCall.op.getName().equals("haversineJavaUDF")) {
            if(rexCall.getOperands().size() != 2) {
                throw new RuntimeException("Expecting exactly 1 operands for UPPER operation");
            }
            RexTruffleNode fstNode = rexCall.getOperands().get(0).accept(this);
            RexTruffleNode sndNode = rexCall.getOperands().get(1).accept(this);

            return new RexTruffleNode() {
                @Child HaversineUDFNode haversine = HaversineUDFNodeGen.create();
                @Child RexTruffleNode fstChildNode = fstNode;
                @Child RexTruffleNode sndChildNode = sndNode;

                @Override
                public Object executeWith(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
                    return haversine.execute(fstChildNode.executeWith(frame, row), sndChildNode.executeWith(frame, row));
                }
            };
        }

        return super.visitCall(rexCall);
    }

    @Override
    protected RexTruffleNode makeLeftOpenRightCloseDateRange(RexTruffleNode input,
                                                             LocalDate from,
                                                             LocalDate to) {
        return input.getOptimizedInDateRange(from, to).orElseGet(() ->
                input.andThen(LocalDateInLeftOpenRangeNodeGen.create(from, to)));
    }
}
