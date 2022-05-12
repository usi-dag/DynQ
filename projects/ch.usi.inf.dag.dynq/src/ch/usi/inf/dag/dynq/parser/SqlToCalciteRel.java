package ch.usi.inf.dag.dynq.parser;


import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq.session.CustomSqlFunctions;
import org.apache.calcite.CalcitePlannerUtils;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptLattice;
import org.apache.calcite.plan.RelOptMaterialization;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptRules;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.RelVisitor;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.metadata.DefaultRelMetadataProvider;
import org.apache.calcite.rel.rules.AntiJoinRule;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.rules.JoinPushThroughJoinRule;
import org.apache.calcite.rel.rules.MyAggregateCaseToFilterRule;
import org.apache.calcite.rel.rules.MyEnumerableAggregateRule;
import org.apache.calcite.rel.rules.MyRemovableEnumerableProjectRule;
import org.apache.calcite.rel.rules.SwapNestedJoinRightAggregation;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexExecutorImpl;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexShuttle;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.runtime.CalciteContextException;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlCallBinding;
import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.BasicSqlType;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlOperandCountRanges;
import org.apache.calcite.sql.type.SqlOperandTypeInference;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorImpl;
import org.apache.calcite.sql.validate.SqlValidatorScope;
import org.apache.calcite.sql2rel.RelDecorrelator;
import org.apache.calcite.sql2rel.RelFieldTrimmer;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Program;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.apache.calcite.tools.Programs.sequence;


public final class SqlToCalciteRel {

    static final boolean USE_AVG_NODE = "true".equals(System.getenv("DYNQ_USE_AVG_NODE"));

    private final APISessionManagement session;
    private final CalciteConnection connection;
    private final SchemaPlus defaultSchema;

    private final boolean USE_SEMI_JOIN,
            USE_ANTI_JOIN,
            USE_HEURISTIC_JOIN_ORDER,
            USE_BUSHY,
            REORDER_JOINS
                    ;
    private final VolcanoPlanner planner;

    SqlToCalciteRel(APISessionManagement session) {
        this.session = session;
        this.connection = session.getConnection();
        this.defaultSchema = SqlToCalciteRel.this.connection.getRootSchema();

        String reorderJoins = System.getenv("DYNQ_REORDER_JOINS");
        REORDER_JOINS = "true".equals(System.getProperty("DYNQ_REORDER_JOINS", reorderJoins != null ? reorderJoins : "true"));
        USE_SEMI_JOIN = "true".equals(System.getenv("DYNQ_USE_SEMI_JOIN"));
        USE_ANTI_JOIN = "true".equals(System.getenv("DYNQ_USE_ANTI_JOIN"));
        USE_HEURISTIC_JOIN_ORDER = REORDER_JOINS &&
                "true".equals(System.getProperty("DYNQ_USE_HEURISTIC_JOIN_ORDER", System.getenv("DYNQ_USE_HEURISTIC_JOIN_ORDER")));
        USE_BUSHY = "true".equals(System.getProperty("DYNQ_USE_BUSHY", System.getenv("DYNQ_USE_BUSHY")));
        planner = getPlanner();
    }

    public static RelNode expandSearch(RelNode relNode) {
        RexBuilder builder = new RexBuilder(new JavaTypeFactoryImpl());
        return relNode.accept(new RexShuttle() {
            @Override
            public RexNode visitCall(RexCall call) {
                if(call.getOperator() == SqlStdOperatorTable.SEARCH) {
                    return RexUtil.expandSearch(builder, null, call);
                } else if(call.getOperator() == SqlStdOperatorTable.AND) {
                    List<RexNode> children = new LinkedList<>();
                    for(RexNode child : call.operands) {
                        child = child.accept(this);
                        if(child instanceof RexCall && ((RexCall) child).getOperator() == SqlStdOperatorTable.AND) {
                            for(RexNode granChild : ((RexCall) child).getOperands()) {
                                children.add(granChild.accept(this));
                            }
                        } else {
                            children.add(child);
                        }
                    }
                    return builder.makeCall(SqlStdOperatorTable.AND, children);
                } else {
                    return super.visitCall(call);
                }
            }
        });
    }

    static SqlToCalciteRel factory(APISessionManagement session) {
        return new SqlToCalciteRel(session);
    }

    public RelNode convert(String sql) throws SqlParseException {

        RelRoot relRoot = parseSql(sql, planner);

        List<RelOptMaterialization> materializationList = new ArrayList<>();
        List<RelOptLattice> latticeList = new ArrayList<>();
        RelTraitSet desiredTraits = getDesiredRootTraitSet(relRoot);

        Program program = getProgram();
        RelNode optimized = program.run(planner, relRoot.rel, desiredTraits, materializationList, latticeList);

        optimized.childrenAccept(new RelVisitor() {
            @Override
            public void visit(RelNode node, int ordinal, RelNode parent) {
                node = expandSearch(node);
                parent.replaceInput(ordinal, node);
                super.visit(node, ordinal, parent);
            }
        });
        optimized = expandSearch(optimized);

        if(session.isDebugEnabled()) {
            System.out.println("With Calcite Planner");
            printPlan(optimized);
        }

        return optimized;
    }


    public VolcanoPlanner getPlanner() {
        VolcanoPlanner planner = new VolcanoPlanner();
        RelOptUtil.registerDefaultRules(planner, true, false);
        planner.removeRule(EnumerableRules.ENUMERABLE_MERGE_JOIN_RULE);
        if(!(USE_SEMI_JOIN)) {
            planner.removeRule(CoreRules.JOIN_TO_SEMI_JOIN);
            planner.removeRule(CoreRules.PROJECT_TO_SEMI_JOIN);
        }
        if(USE_ANTI_JOIN) {
            planner.addRule(AntiJoinRule.INSTANCE);
        }
        if(USE_AVG_NODE) {
            planner.removeRule(EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
            planner.removeRule(CoreRules.AGGREGATE_REDUCE_FUNCTIONS);
            planner.addRule(MyEnumerableAggregateRule.INSTANCE);
        } else {
            planner.addRule(CoreRules.AGGREGATE_REDUCE_FUNCTIONS);
        }
        RelOptRules.CALC_RULES.forEach(planner::removeRule);

        planner.removeRule(CoreRules.SORT_PROJECT_TRANSPOSE);

        planner.removeRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        planner.addRule(MyRemovableEnumerableProjectRule.INSTANCE);

        if(REORDER_JOINS) {
            planner.addRule(CoreRules.JOIN_COMMUTE);
            // TODO enable this once implemented right join
            //  otherwise, change the rule such that is applies iff returned join is left
//    planner.addRule(CoreRules.JOIN_COMMUTE_OUTER);
            planner.addRule(JoinPushThroughJoinRule.LEFT);
            planner.addRule(JoinPushThroughJoinRule.RIGHT);
            planner.addRule(SwapNestedJoinRightAggregation.INSTANCE);
        } else {
            planner.removeRule(CoreRules.JOIN_COMMUTE);
            planner.removeRule(JoinPushThroughJoinRule.LEFT);
            planner.removeRule(JoinPushThroughJoinRule.RIGHT);
        }

//        planner.addRule(CoreRules.AGGREGATE_CASE_TO_FILTER);
        planner.addRule(MyAggregateCaseToFilterRule.INSTANCE);
        session.getExtraPlannerRules().forEach(planner::addRule);
        DataContext dataContext = connection.createPrepareContext().getDataContext();
        planner.setExecutor(new RexExecutorImpl(dataContext));

        return planner;
    }

    private Program getProgram() {
        Program myProgram;
        DefaultRelMetadataProvider metadataProvider = DefaultRelMetadataProvider.INSTANCE;
        Program subQuery = Programs.subQuery(metadataProvider);
        Program decorrelate = new DecorrelateProgram();
        Program trimFields = new TrimFieldsProgram();
        Program rulesProgram = Programs.ofRules(planner.getRules());
        myProgram = sequence(subQuery, decorrelate, trimFields, rulesProgram);
        Program program;
        if(USE_HEURISTIC_JOIN_ORDER) {
            if(session.isDebugEnabled()) {
                System.out.println("try heuristic join orders with bushy=" + USE_BUSHY);
            }
            Program first = Programs.heuristicJoinOrder(planner.getRules(), USE_BUSHY, 2);
            program = new MySequenceProgram(subQuery, decorrelate, trimFields, first, myProgram);
        } else {
            // TODO currently (myProgram, myProgram) replicated is needed to remove useless projections - fix
            program = new MySequenceProgram(subQuery, decorrelate, trimFields, myProgram, myProgram);
        }
        return program;
    }

    private RelRoot parseSql(String sql, VolcanoPlanner planner) throws SqlParseException {
        // SQL Parser
        SqlParser parser = SqlParser.create(sql, CalcitePlannerUtils.getSqlParserConfig());
        SqlNode parsed = parser.parseQuery();

        // Calcite Catalog reader
        SqlTypeFactoryImpl factory = getSqlTypeFactory();

        SchemaPlus defaultSchema = connection.getRootSchema();
        CalciteCatalogReader calciteCatalogReader = new CalciteCatalogReader(
                CalciteSchema.from(defaultSchema),
                CalciteSchema.from(defaultSchema).path(null),
                factory,
                new CalciteConnectionConfigImpl(new Properties()));


        // SQL Validator
        SqlValidator validator = getSqlValidator();
        SqlNode validated = validator.validate(parsed);


        // SqlToRelConverter -- SqlNode -> RelNode
        SqlToRelConverter.Config config = getSqlToRelConverterConfig();
        FrameworkConfig frameworkConfig = getFrameworkConfig();

        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);

        RexBuilder rexBuilder = new RexBuilder(factory);
        RelOptCluster cluster = RelOptCluster.create(planner, rexBuilder);
        RelOptTable.ViewExpander expander = (rowType, queryString, schemaPath, viewPath) -> null;
        SqlToRelConverter sqlToRelConverter = new SqlToRelConverter(
                expander, validator, calciteCatalogReader, cluster, frameworkConfig.getConvertletTable(), config);
        RelRoot root = sqlToRelConverter.convertQuery(validated, false, true);

        return root.withRel(sqlToRelConverter.decorrelate(validated, root.rel));
    }

    public SqlStdOperatorTable getSqlStdOperatorTableWithUDFs() {
        SqlStdOperatorTable sqlStdOperatorTable = SqlStdOperatorTable.instance();

        class MyExplicitOperandTypeInference implements SqlOperandTypeInference {
            @Override
            public void inferOperandTypes(SqlCallBinding sqlCallBinding, RelDataType relDataType, RelDataType[] relDataTypes) {}
        }

        for(String udfName : session.getRegisteredUDFTable().names()) {
            SqlFunction udf = new SqlFunction(udfName,
                    SqlKind.OTHER_FUNCTION,
                    ReturnTypes.LEAST_RESTRICTIVE,
                    new MyExplicitOperandTypeInference(),
                    OperandTypes.repeat(SqlOperandCountRanges.any()), // .ANY_ANY,
                    SqlFunctionCategory.USER_DEFINED_SPECIFIC_FUNCTION); // USER_DEFINED_FUNCTION
            sqlStdOperatorTable.register(udf);
        }

        for(String udfName : session.getRegisteredJavaUDFTable().names()) {
            SqlFunction udf = new SqlFunction(udfName,
                    SqlKind.OTHER_FUNCTION,
                    ReturnTypes.LEAST_RESTRICTIVE,
                    new MyExplicitOperandTypeInference(),
                    OperandTypes.repeat(SqlOperandCountRanges.any()), // .ANY_ANY,
                    SqlFunctionCategory.USER_DEFINED_SPECIFIC_FUNCTION); // USER_DEFINED_FUNCTION
            sqlStdOperatorTable.register(udf);
        }

        return sqlStdOperatorTable;
    }

    public SqlValidator getSqlValidator() {
        SqlTypeFactoryImpl factory = getSqlTypeFactory();
        CalciteCatalogReader catalogReader = getCalciteCatalogReader(factory);
        // SqlConformanceEnum: maybe not the best thing to do
        return new SqlValidatorImpl(getSqlStdOperatorTableWithUDFs(), catalogReader, factory, SqlValidator.Config.DEFAULT) {
            protected void validateWhereOrOn(SqlValidatorScope scope, SqlNode condition, String clause) {
                try {
                    super.validateWhereOrOn(scope, condition, clause);
                } catch (CalciteContextException e) {
                    boolean hide = false;
                    if(condition instanceof SqlBasicCall) {
                        SqlBasicCall call = (SqlBasicCall) condition;
                        if(session.getRegisteredUDFTable().getUDF(call.getOperator().getName()) != null) {
                            // This is a UDF, hide exception
                            hide = true;
                        } else if(CustomSqlFunctions.exists(call.getOperator().getName())) {
                            // This is a Custom SQL function, hide exception
                            hide = true;
                        }
                    }
                    if(!hide) {
                        throw e;
                    }
                }
            }

            @Override
            public RelDataType getValidatedNodeType(SqlNode node) {
                try {
                    return super.getValidatedNodeType(node);
                } catch (UnsupportedOperationException e) {
                    if(node instanceof SqlDynamicParam) {
//                        SqlDynamicParam param = (SqlDynamicParam) node;
                        return new BasicSqlType(factory.getTypeSystem(), SqlTypeName.ANY);
                    }
                    throw e;
                }
            }
        };
    }

    public FrameworkConfig getFrameworkConfig() {
        SqlStdOperatorTable sqlStdOperatorTable = SqlStdOperatorTable.instance();
        for(CustomSqlFunctions operator : CustomSqlFunctions.values()) {
            sqlStdOperatorTable.register(operator.function);
        }
        return Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(defaultSchema)
                .traitDefs(ConventionTraitDef.INSTANCE, RelDistributionTraitDef.INSTANCE)
                .operatorTable(sqlStdOperatorTable)
                .build();
    }

    public SqlToRelConverter.Config getSqlToRelConverterConfig() {
        return SqlToRelConverter.config()
                .withTrimUnusedFields(true)
                .withExpand(false);
    }

    public SqlTypeFactoryImpl getSqlTypeFactory() {
        return  new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    }


    public CalciteCatalogReader getCalciteCatalogReader(SqlTypeFactoryImpl factory) {
        return new CalciteCatalogReader(
                CalciteSchema.from(defaultSchema),
                CalciteSchema.from(defaultSchema).path(null),
                factory,
                new CalciteConnectionConfigImpl(new Properties()));
    }

    RelTraitSet getDesiredRootTraitSet(RelRoot root) {
        return root.rel.getTraitSet()
                .replace(EnumerableConvention.INSTANCE)
                .replace(root.collation)
                .simplify();
    }

    void printPlan(RelNode plan) {
        String str = RelOptUtil.toString(plan);
        System.out.println("plan>");
        System.out.println(str);
    }


    private static class TrimFieldsProgram implements Program {
        public RelNode run(RelOptPlanner planner, RelNode rel,
                           RelTraitSet requiredOutputTraits,
                           List<RelOptMaterialization> materializations,
                           List<RelOptLattice> lattices) {
            final RelBuilder relBuilder =
                    RelFactories.LOGICAL_BUILDER.create(rel.getCluster(), null);
            return new RelFieldTrimmer(null, relBuilder).trim(rel);
        }
    }

    private static class DecorrelateProgram implements Program {
        public RelNode run(RelOptPlanner planner, RelNode rel,
                           RelTraitSet requiredOutputTraits,
                           List<RelOptMaterialization> materializations,
                           List<RelOptLattice> lattices) {
            RelBuilder relBuilder = RelFactories.LOGICAL_BUILDER.create(rel.getCluster(), null);
            return RelDecorrelator.decorrelateQuery(rel, relBuilder);
        }
    }

    private class MySequenceProgram implements Program {
        private final List<Program> programs;

        MySequenceProgram(Program... programs) {
            this.programs = Arrays.asList(programs);
        }

        public RelNode run(RelOptPlanner planner, RelNode rel,
                           RelTraitSet requiredOutputTraits,
                           List<RelOptMaterialization> materializations,
                           List<RelOptLattice> lattices) {
            for (Program program : programs) {
                try {
                    rel = program.run(planner, rel, requiredOutputTraits, materializations, lattices);
                } catch (AssertionError e) {
                    if(session.isDebugEnabled()) {
                        System.out.println("got AssertionError with heuristic join orders! " + e);
                    }
                } catch (RuntimeException e) {
                    if(session.isDebugEnabled()) {
                        System.out.println("got RuntimeException with heuristic join orders! " + e);
                    }
                }
            }
            return rel;
        }
    }
}

