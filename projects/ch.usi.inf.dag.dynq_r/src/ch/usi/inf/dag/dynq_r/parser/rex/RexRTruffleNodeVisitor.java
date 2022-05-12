package ch.usi.inf.dag.dynq_r.parser.rex;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.dates.ExtractFromDateRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DivRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.EqualsRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.MulRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.NotEqualsRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.SubRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.rex.RexTruffleNodeVisitor;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.dates.LocalDateRWrapperConstRexTruffleNode;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.untyped.GreaterThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.untyped.GreaterThanRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.untyped.LessThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.untyped.LessThanRexTruffleNodeGen;
import com.google.common.collect.ImmutableMap;
import org.apache.calcite.avatica.util.TimeUnitRange;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexProgram;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import static ch.usi.inf.dag.dynq.parser.rex.RexConversionUtils.asLocalDate;


public class RexRTruffleNodeVisitor extends RexTruffleNodeVisitor {
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

    protected RexRTruffleNodeVisitor(APISessionManagement session, RelNodeVolcanoWrapper relNode, RexProgram rexProgram) {
        super(session, relNode, rexProgram);
    }

    protected RexRTruffleNodeVisitor(APISessionManagement session, RelNodeVolcanoWrapper relNode) {
        super(session, relNode);
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
    protected RexTruffleNode resolveConstant(RexLiteral rexLiteral) {
        if(rexLiteral.getTypeName() == SqlTypeName.DATE) {
            Calendar calendar = rexLiteral.getValueAs(Calendar.class);
            return new LocalDateRWrapperConstRexTruffleNode(asLocalDate(calendar));
        }
        return super.resolveConstant(rexLiteral);
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
        return super.visitCall(rexCall);
    }
}
