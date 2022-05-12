package ch.usi.inf.dag.dynq.parser.rex;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.CaseRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanConstRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.IsNotNullRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.IsNullRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.IsTrueRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.NotBooleanRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.StringLikeRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates.CastStringToDateRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates.CastStringToDateRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates.ExtractFromDateRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates.LocalDateConstRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions.StringStartsUnsensitiveWithRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions.StringStartsWithRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions.UDFCallerRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles.DoubleConstRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers.IntegerConstRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.CastToFloatRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.CastToFloatRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.CastToStringRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ConstRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.StringConstRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.SubStringRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.UpperCaseStringRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.InConstantIntSetNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.InConstantStringSetNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_cmp.ConstantStringEqNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_cmp.ConstantStringNEqNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_range_predicates.DoubleInLeftOpenRangeNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_range_predicates.DoubleInLeftOpenRightOpenRangeNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_range_predicates.LocalDateInLeftOpenRangeNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AllRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AnyRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.BinaryContainerRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.BinaryRexForContainerTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DivRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.EqualsRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.GreaterThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.GreaterThanRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.InteropArrayItemGetterRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.InteropReaderRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.LessThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.LessThanRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.MulRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.NotEqualsRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.RexDynamicParameterTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.SubRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.UntypedRexTruffleNode;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import org.apache.calcite.avatica.util.TimeUnitRange;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexCorrelVariable;
import org.apache.calcite.rex.RexDynamicParam;
import org.apache.calcite.rex.RexFieldAccess;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexLocalRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexOver;
import org.apache.calcite.rex.RexPatternFieldRef;
import org.apache.calcite.rex.RexProgram;
import org.apache.calcite.rex.RexRangeRef;
import org.apache.calcite.rex.RexSubQuery;
import org.apache.calcite.rex.RexTableInputRef;
import org.apache.calcite.rex.RexVisitor;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;



public class RexTruffleNodeVisitor implements RexVisitor<RexTruffleNode> {

    private static final boolean MERGE_RANGES = "true".equals(System.getenv("DYNQ_MERGE_RANGES"));
    private static final boolean MERGE_OR_INTO_IN = "true".equals(System.getenv("DYNQ_MERGE_OR_INTO_IN"));

    private final APISessionManagement session;
    private final RexProgram rexProgram;
    private final RelNodeVolcanoWrapper relNode;


    protected Map<SqlKind, BinaryOperator<RexTruffleNode>> BINARY_OPERATORS;

    protected Map<SqlKind, Function<RexTruffleNode[], RexTruffleNode>> NARY_OPERATORS;

    // TODO improve initialization
    {
        BINARY_OPERATORS = new HashMap<>();
        BINARY_OPERATORS.put(SqlKind.PLUS, wrapBinOp(AddRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.TIMES, wrapBinOp(MulRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.MINUS, wrapBinOp(SubRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.DIVIDE, wrapBinOp(DivRexTruffleNodeGen::create));

        BINARY_OPERATORS.put(SqlKind.EQUALS, wrapBinOp(EqualsRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.NOT_EQUALS, wrapBinOp(NotEqualsRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.LESS_THAN, wrapBinOp(LessThanRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.LESS_THAN_OR_EQUAL, wrapBinOp(LessThanEqualRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.GREATER_THAN, wrapBinOp(GreaterThanRexTruffleNodeGen::create));
        BINARY_OPERATORS.put(SqlKind.GREATER_THAN_OR_EQUAL, wrapBinOp(GreaterThanEqualRexTruffleNodeGen::create));

        NARY_OPERATORS = new HashMap<>();
        NARY_OPERATORS.put(SqlKind.AND, AllRexTruffleNode::new);
        NARY_OPERATORS.put(SqlKind.OR, AnyRexTruffleNode::new);
    }

    protected RexTruffleNodeVisitor(APISessionManagement session, RelNodeVolcanoWrapper relNode, RexProgram rexProgram) {
        this.session = session;
        this.relNode = relNode;
        this.rexProgram = rexProgram;
    }

    protected RexTruffleNodeVisitor(APISessionManagement session, RelNodeVolcanoWrapper relNode) {
        this(session, relNode, null);
    }

    public APISessionManagement getSession() {
        return session;
    }

    protected static BinaryOperator<RexTruffleNode> wrapBinOp(Supplier<BinaryRexForContainerTruffleNode> operation) {
        return (left, right) -> new BinaryContainerRexTruffleNode(left, right, operation.get());
    }

    protected static RexTruffleNode foldLeftBinOp(List<RexTruffleNode> nodes, BinaryOperator<RexTruffleNode> operator) {
        if(nodes.size() < 2) {
            throw new AssertionError("Cannot fold a binary operator on less than two nodes");
        }
        return nodes.stream().reduce(operator).get();
    }


    protected RexTruffleNode resolveSqlBinaryOp(List<? extends RexTruffleNode> children, RexCall rexCall) {
        assert rexCall.op instanceof SqlBinaryOperator;

        SqlKind kind = rexCall.getKind();
        // First check if we have a binary operator to fold for this SqlKind
        if(BINARY_OPERATORS.containsKey(kind)) {
            return foldLeftBinOp(new LinkedList<>(children), BINARY_OPERATORS.get(kind));
        }

        // Check if it is an OR that comes from an IN operator
        if(kind == SqlKind.OR) {

        }

        // If not, check if we have a n-ary operator, i.e., a function
        if(NARY_OPERATORS.containsKey(kind)) {
            return NARY_OPERATORS.get(kind).apply(children.toArray(new RexTruffleNode[0]));
        }

        // Nothing else to try, just report the missing SqlKind
        throw new RuntimeException("Unexpected type " + rexCall.getKind());
    }


    protected RexTruffleNode resolveConstant(RexLiteral rexLiteral) {
        switch (rexLiteral.getTypeName()) {
            case FLOAT:
            case DOUBLE:
            case DECIMAL:
                double aDouble = Double.parseDouble(rexLiteral.getValue().toString());
                return new DoubleConstRexTruffleNode(aDouble);
            case DATE:
                Calendar calendar = rexLiteral.getValueAs(Calendar.class);
                return new LocalDateConstRexTruffleNode(RexConversionUtils.asLocalDate(calendar));
            case INTERVAL_YEAR:
            case INTEGER:
                int aInt = Integer.parseInt(rexLiteral.getValue().toString());
                return new IntegerConstRexTruffleNode(aInt);
            case CHAR:
            case VARCHAR:
                String aString = rexLiteral.getValueAs(String.class);
                return new StringConstRexTruffleNode(aString);
            case NULL:
                return ConstRexTruffleNode.create(null);
            case BOOLEAN:
                boolean value = rexLiteral.getValueAs(Boolean.class);
                return new BooleanConstRexTruffleNode(value);

        }
        throw new RuntimeException("Unexpected field type for constant: " + rexLiteral.getTypeName());
    }

    @Override
    public RexTruffleNode visitInputRef(RexInputRef rexInputRef) {
        int index = rexInputRef.getIndex();
        return relNode.getInputDataAccessor(index);
    }

    @Override
    public RexTruffleNode visitLiteral(RexLiteral rexLiteral) {
        return resolveConstant(rexLiteral);
    }

    @Override
    public RexTruffleNode visitCall(RexCall rexCall) {
        return _visitCall(rexCall);
    }


    protected RexTruffleNode _visitCall(RexCall rexCall) {
        if(rexCall.getKind() == SqlKind.CAST) {
            RexNode operand = rexCall.getOperands().get(0);
            RexTruffleNode accessor = operand.accept(this);
            if(rexCall.getType().getSqlTypeName() == SqlTypeName.DATE) {
                CastStringToDateRexTruffleNode castStringToDateRexTruffleNode = CastStringToDateRexTruffleNodeGen.create();
                return accessor.andThen(castStringToDateRexTruffleNode);
            } else if(rexCall.getType().getSqlTypeName() == SqlTypeName.VARCHAR) {
                CastToStringRexTruffleNode toString = new CastToStringRexTruffleNode();
                return accessor.andThen(toString);
            } else if(rexCall.getType().getSqlTypeName() == SqlTypeName.FLOAT) {
                CastToFloatRexTruffleNode toFloat = CastToFloatRexTruffleNodeGen.create();
                return accessor.andThen(toFloat);
            }
            return accessor;
        }
        if(rexCall.getKind() == SqlKind.CASE) {
            RexTruffleNode condition = rexCall.getOperands().get(0).accept(this);
            RexTruffleNode ifTrue = rexCall.getOperands().get(1).accept(this);
            RexTruffleNode ifFalse = rexCall.getOperands().get(2).accept(this);
            return CaseRexTruffleNode.create(condition, ifTrue, ifFalse);
        }
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
        if(rexCall.getKind() == SqlKind.LIKE) {
            // Expecting two operands: a string expression (i.e.. what to be tested) and a string with % (i.e., a regex-like)
            if(rexCall.getOperands().size() != 2) {
                throw new RuntimeException("Expecting exactly two operands for EXTRACT operation");
            }
            RexNode fst = rexCall.getOperands().get(0);
            // TODO allow only STRING or ANY
//            if(!SqlTypeName.STRING_TYPES.contains(fst.getType().getSqlTypeName())) {
//                throw new RuntimeException("Expecting first operand for LIKE operation to be an expression of type String");
//            }
            RexNode snd = deref(rexCall.getOperands().get(1));
            if(!(snd instanceof RexLiteral)) {
                throw new RuntimeException("Expecting second operand for LIKE operation to be a RexLiteral");
            }
            RexLiteral sndLiteral = (RexLiteral) snd;
            try {
                RexTruffleNode stringGetter = fst.accept(this);
                String regExLike = sndLiteral.getValueAs(String.class);
                return stringGetter.getOptimizedStringLike(regExLike).orElseGet(() ->
                        StringLikeRexTruffleNode.create(stringGetter, regExLike));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if(rexCall.getKind() == SqlKind.NOT) {
            if(rexCall.getOperands().size() != 1) {
                throw new RuntimeException("Expecting exactly one operand for NOT operation");
            }
            RexNode fst = rexCall.getOperands().get(0);
            return new NotBooleanRexTruffleNode(fst.accept(this));
        }

        if(rexCall.getKind() == SqlKind.IS_NOT_NULL) {
            if(rexCall.getOperands().size() != 1) {
                throw new RuntimeException("Expecting exactly one operand for IS NOT NULL operation");
            }
            RexNode fst = rexCall.getOperands().get(0);
            return new IsNotNullRexTruffleNode(fst.accept(this));
        }
        if(rexCall.getKind() == SqlKind.IS_NULL) {
            if(rexCall.getOperands().size() != 1) {
                throw new RuntimeException("Expecting exactly one operand for IS NOT NULL operation");
            }
            RexNode fst = rexCall.getOperands().get(0);
            return new IsNullRexTruffleNode(fst.accept(this));
        }
        if(rexCall.op.getName().equals("SUBSTRING")) {
            if(rexCall.getOperands().size() != 3) {
                throw new RuntimeException("Expecting exactly 3 operands for SUBSTRING operation");
            }
            RexTruffleNode stringGetter = rexCall.getOperands().get(0).accept(this);
            RexNode snd = deref(rexCall.getOperands().get(1));
            RexNode trd = deref(rexCall.getOperands().get(2));
            if(snd instanceof RexLiteral && trd instanceof RexLiteral) {
                int from = ((RexLiteral) snd).getValueAs(Integer.class) - 1;
                int len = ((RexLiteral) trd).getValueAs(Integer.class);
                Optional<RexTruffleNode> optimizedSubString = stringGetter.getOptimizedSubstring(from, len);
                return optimizedSubString.orElseGet(() ->
                        SubStringRexTruffleNodeGen.create(from, len, stringGetter));
            }
            throw new RuntimeException("Expecting both second and third operands for SUBSTRING to be literals (missing impl)");
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

        if(rexCall.getKind() == SqlKind.ITEM) {
            if(rexCall.getOperands().size() != 2) {
                throw new RuntimeException("Expecting exactly 2 operands for ITEM operation");
            }
            RexNode fst = rexCall.getOperands().get(0);
            RexNode snd = deref(rexCall.getOperands().get(1));
            if(snd instanceof RexLiteral) {
                RexLiteral literal = (RexLiteral) snd;
                // TODO here we assume Dynamic Objects - generalize it
                SqlTypeName typeName = literal.getType().getSqlTypeName();
                if (SqlTypeName.STRING_TYPES.contains(typeName)) {
                    String member = literal.getValueAs(String.class);
                    return fst.accept(this).andThen(InteropReaderRexTruffleNode.create(member));
                }
                if (SqlTypeName.INT_TYPES.contains(typeName)) {
                    int index = literal.getValueAs(Integer.class);
                    return fst.accept(this).andThen(InteropArrayItemGetterRexTruffleNode.create(index));
                }
            }
        }

        if(rexCall.getKind() == SqlKind.IS_TRUE) {
            RexNode op = rexCall.getOperands().get(0);
            return new IsTrueRexTruffleNode(op.accept(this));
        }


        if(rexCall.op instanceof SqlBinaryOperator) {
            return visitSqlBinaryOperator(rexCall);
        }

        if(rexCall.op instanceof SqlFunction) {
            SqlFunction function = (SqlFunction) rexCall.op;
            if(function.getFunctionType().isUserDefined()) {
                Object udf = session.getRegisteredUDFTable().getUDF(function.getName());
                if(udf == null) {
                    throw new RuntimeException("Unknown UDF: " + function.getName());
                }
                // TODO: currently we are converting values from interop
                //  but likely this call should use original dynamic objects without converting them
                RexTruffleNode[] operands = rexCall.operands.stream().map(op -> op.accept(this)).toArray(RexTruffleNode[]::new);
                return UDFCallerRexTruffleNodeGen.create(udf, operands);
            }
        }

        throw new RuntimeException("Unknown RexCall: " + rexCall);
    }

    protected RexTruffleNode visitSqlBinaryOperator(RexCall rexCall) {
        List<RexNode> ops = rexCall.operands.stream().map(this::deref).collect(Collectors.toList());

        // few optimized binary operators
        boolean optimizeStrEq = "true".equals(System.getenv("DYNQ_OPTIMIZE_EQ"));
        if(optimizeStrEq && (rexCall.getKind() == SqlKind.EQUALS || rexCall.getKind() == SqlKind.NOT_EQUALS) && twoOperandsOneConstant(ops)) {
            int constantIndex = getFirstConstantIndex(ops);
            RexLiteral constant = (RexLiteral) ops.get(constantIndex);
            if(constant.getTypeName() == SqlTypeName.CHAR || constant.getTypeName() == SqlTypeName.VARCHAR) {
                String constantString = constant.getValueAs(String.class);
                int otherIndex = constantIndex == 0 ? 1 : 0;
                RexTruffleNode rexTruffleNode = ops.get(otherIndex).accept(this);
                if(rexCall.getKind() == SqlKind.EQUALS) {
                    return rexTruffleNode.getOptimizedEquals(constantString)
                            .orElseGet(() -> new ConstantStringEqNode(constantString, rexTruffleNode));
                } else {
                    return rexTruffleNode.getOptimizedNotEquals(constantString)
                            .orElseGet(() -> new ConstantStringNEqNode(constantString, rexTruffleNode));
                }
            }
        }


        if(MERGE_RANGES && rexCall.isA(SqlKind.AND)) {
            RexTruffleNode merged = RangeMerger.maybeMergeRange(rexCall, this);
            if(merged != null) {
                return merged;
            }
        }

        if(MERGE_OR_INTO_IN && rexCall.isA(SqlKind.OR)) {
            if(ops.stream().allMatch(rexNode -> rexNode.isA(SqlKind.EQUALS))) {
                Set<RexNode> leftRexNodes = new HashSet<>();
                Set<RexLiteral> rightRexNodes = new HashSet<>();
                Set<SqlTypeName> rightTypes = new HashSet<>();
                ops.stream().map(r -> (RexCall) r).forEach(rexEqCall -> {
                    leftRexNodes.add(rexEqCall.operands.get(0));
                    RexNode rightSide = rexEqCall.operands.get(1);
                    if(rightSide instanceof RexLiteral) {
                        RexLiteral rightLiteral = (RexLiteral) rightSide;
                        rightRexNodes.add(rightLiteral);
                        rightTypes.add(rightLiteral.getType().getSqlTypeName());
                    }
                });
                if(leftRexNodes.size() == 1 && rightTypes.size() == 1 && rightRexNodes.size() == rexCall.operands.size()) {
                    RexNode left = leftRexNodes.stream().findFirst().get();
                    SqlTypeName type = rightTypes.stream().findFirst().get();
                    if(SqlTypeName.STRING_TYPES.contains(type)) {
                        Set<String> elements = rightRexNodes.stream()
                                .map(r -> r.getValueAs(String.class))
                                .collect(Collectors.toSet());
                        RexTruffleNode leftRex = left.accept(this);
                        return leftRex.getOptimizedInConstantStringSet(elements).orElseGet(
                                () -> InConstantStringSetNode.create(left.accept(this), elements));                    }
                    // TODO fix (calcite): type is DECIMAL instead of INTEGER
                    else if(type == SqlTypeName.INTEGER || SqlTypeName.INT_TYPES.contains(type)) {
                        Set<Integer> elements = rightRexNodes.stream()
                                .map(r -> r.getValueAs(Integer.class))
                                .collect(Collectors.toSet());
                        return InConstantIntSetNode.create(left.accept(this), elements);
                    }
                    // TODO other types
                }
            }

        }

        List<RexTruffleNode> children = ops.stream().map(op -> op.accept(this)).collect(Collectors.toList());
        return resolveSqlBinaryOp(children, rexCall);
    }

    @Override
    public RexTruffleNode visitOver(RexOver rexOver) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public RexTruffleNode visitCorrelVariable(RexCorrelVariable rexCorrelVariable) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public RexTruffleNode visitDynamicParam(RexDynamicParam rexDynamicParam) {
        return new RexDynamicParameterTruffleNode(rexDynamicParam.getIndex());
    }

    @Override
    public UntypedRexTruffleNode visitRangeRef(RexRangeRef rexRangeRef) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public UntypedRexTruffleNode visitFieldAccess(RexFieldAccess rexFieldAccess) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public UntypedRexTruffleNode visitSubQuery(RexSubQuery rexSubQuery) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public UntypedRexTruffleNode visitTableInputRef(RexTableInputRef rexTableInputRef) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public UntypedRexTruffleNode visitPatternFieldRef(RexPatternFieldRef rexPatternFieldRef) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public RexTruffleNode visitLocalRef(RexLocalRef rexLocalRef) {
        if(rexProgram == null) {
            throw new RuntimeException("Cannot visit a LocalRef without a RexProgram");
        }
        return ithExpr(rexLocalRef.getIndex()).accept(this);
    }

    private RexNode ithExpr(int i) {
        if(rexProgram == null) {
            throw new RuntimeException("Cannot visit a missing RexProgram");
        }
        return rexProgram.getExprList().get(i);
    }

    protected RexNode deref(RexNode rexNode) {
        if(rexNode instanceof  RexLocalRef && rexProgram != null) {
            return ithExpr(((RexLocalRef) rexNode).getIndex());
        }
        return rexNode;
    }

    protected boolean twoOperandsOneConstant(List<RexNode> operands) {
        if(operands.size() != 2) {
            return false;
        }
        return operands.get(0) instanceof RexLiteral || operands.get(1) instanceof RexLiteral;
    }

    protected Integer getFirstConstantIndex(List<RexNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            RexNode node = nodes.get(i);
            if(node instanceof RexLiteral) {
                return i;
            }
        }
        return null;
    }

    // TODO get range check from extension
    protected RexTruffleNode makeLeftOpenRightCloseDateRange(RexTruffleNode input,
                                                             LocalDate from,
                                                             LocalDate to) {
        return input.getOptimizedInDateRange(from, to).orElseGet(() ->
                input.andThen(LocalDateInLeftOpenRangeNodeGen.create(from, to)));
    }

    // TODO other optimized comparisons
    protected RexTruffleNode makeLeftOpenRightOpenDateRange(RexTruffleNode input,
                                                            LocalDate from,
                                                            LocalDate to) {
        return makeLeftOpenRightCloseDateRange(input, from, to.plusDays(1));
    }

    protected RexTruffleNode makeLeftOpenRightCloseDoubleRange(RexTruffleNode input,
                                                               double from,
                                                               double to) {
        return input.andThen(DoubleInLeftOpenRangeNodeGen.create(from, to));
    }

    protected RexTruffleNode makeLeftOpenRightOpenDoubleRange(RexTruffleNode input,
                                                              double from,
                                                              double to) {
        return input.andThen(DoubleInLeftOpenRightOpenRangeNodeGen.create(from, to));
    }

    @Override
    public String toString() {
        return super.toString() + "(" + relNode.toString() + ")";
    }
}

