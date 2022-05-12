package ch.usi.inf.dag.dynq_r.parser.tpch_hardcoded_plans;

import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.calcite.CalcitePlannerUtils;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Program;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.util.DateString;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlanQ20 {

    public static final JavaTypeFactory typeFactory = new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);

    static final RexBuilder rexBuilder = new RexBuilder(typeFactory);
    static RelBuilder relBuilder;

    static final String
            NATION = "nation",
            PART = "part",
            PARTSUPP = "partsupp",
            LINEITEM = "lineitem",
            ORDERS = "orders",
            SUPPLIER = "supplier",
            S_NAME = "s_name";

    static SchemaPlus defaultSchema;

    static final Map<String, List<String>> requiredFields = new ImmutableMap.Builder<String, List<String>>()
            .put("part", ImmutableList.of("p_partkey", "p_name"))
            .put("partsupp", ImmutableList.of("ps_availqty", "ps_partkey", "ps_suppkey", "ps_partkey"))
            .put("lineitem", ImmutableList.of("l_partkey", "l_suppkey", "l_quantity", "l_shipdate"))
            .put("nation", ImmutableList.of("n_nationkey", "n_name"))
            .put("supplier", ImmutableList.of("s_name", "s_address", "s_suppkey", "s_nationkey"))
            .build();

    static RelNode plan(APISessionManagement session) {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CalciteConnection.class.getClassLoader());
        SchemaPlus dynqSchema = session.getConnection().getRootSchema();
        defaultSchema = Frameworks.createRootSchema(true);
        for(String tableName : dynqSchema.getTableNames()) {
            defaultSchema.add(tableName, dynqSchema.getTable(tableName));
        }
        for(Map.Entry<String, List<String>> entry : requiredFields.entrySet()) {
            for(String col : entry.getValue()) {
                defaultSchema.getTable(entry.getKey()).getRowType(typeFactory).getField(col, false, true);
            }
        }
        FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(CalcitePlannerUtils.getSqlParserConfig())
                .defaultSchema(dynqSchema)
                .build();
        relBuilder = RelBuilder.create(config);


        /*
        * ps_part as (
    select ps_availqty, ps_partkey, ps_suppkey
	from part, partsupp
	where p_partkey = ps_partkey
	and p_name like 'forest%'),
        * */

        RelNode scanPart = relBuilder.scan(PART).build();
        RexNode filterPartRex = relBuilder.call(
                SqlStdOperatorTable.LIKE,
                getFieldInputRef(scanPart, "p_name"),
                rexBuilder.makeLiteral("forest%"));

        RelNode filterPar = relBuilder.push(scanPart).filter(filterPartRex).build();
        RelNode scanPartSupp = relBuilder.scan(PARTSUPP).build();
        RelNode ps_part = makeInnerJoin(filterPar, scanPartSupp, "p_partkey", "ps_partkey");

        /*
        * group_lineitem as (
    select l_partkey, l_suppkey, sum(l_quantity) as sumq
    from lineitem
    where l_shipdate >= date '1994-01-01'
    and l_shipdate < date '1994-01-01' + interval '1' year
    group by l_partkey, l_suppkey
),
        * */

        LocalDate from = LocalDate.of(1994, 1, 1);
        LocalDate to = LocalDate.of(1995, 1, 1);
        RelNode scanLineitem = relBuilder.scan(LINEITEM).build();
        RexNode rexFilterLineitem = relBuilder.and(
                relBuilder.call(
                        SqlStdOperatorTable.GREATER_THAN_OR_EQUAL,
                        getFieldInputRef(scanLineitem, "l_shipdate"),
                        rexBuilder.makeDateLiteral(DateString.fromDaysSinceEpoch((int)from.toEpochDay()))),
                relBuilder.call(
                        SqlStdOperatorTable.LESS_THAN,
                        getFieldInputRef(scanLineitem, "l_shipdate"),
                        rexBuilder.makeDateLiteral(DateString.fromDaysSinceEpoch((int)to.toEpochDay()))));

        RelNode filter_lineitem = relBuilder
                .push(scanLineitem)
                .filter(rexFilterLineitem)
                .build();

        RelNode group_lineitem = relBuilder
                .push(filter_lineitem)
                .aggregate(relBuilder.groupKey(
                        getFieldInputRef(filter_lineitem, "l_partkey"),
                        getFieldInputRef(filter_lineitem, "l_suppkey")))
                .build();

        /*
        *
        * ps_line as (
    select *
    from ps_part, group_lineitem
    where ps_partkey = l_partkey
    and ps_suppkey = l_suppkey
    and ps_availqty > 0.5 * sumq
),
        * */

        RelNode ps_line = relBuilder
                .push(ps_part)
                .push(group_lineitem)
                .join(JoinRelType.INNER,
                        relBuilder.call(
                                SqlStdOperatorTable.AND,
                                makeEqualsRex(
                                        getFieldInputRef(ps_part, "ps_partkey"),
                                        getFieldInputRefJoinRightSide(group_lineitem, "l_partkey", ps_part)),
                                makeEqualsRex(
                                        getFieldInputRef(ps_part, "ps_suppkey"),
                                        getFieldInputRefJoinRightSide(group_lineitem, "l_suppkey", ps_part)),
                                relBuilder.call(
                                        SqlStdOperatorTable.GREATER_THAN,
                                        getFieldInputRef(ps_part, "ps_availqty"),
                                        relBuilder.call(
                                                SqlStdOperatorTable.MULTIPLY,
                                                relBuilder.literal(0.5),
                                                getFieldInputRef(ps_part, "ps_availqty"))
                                )))
                .build();

        // nat_sup = Join(nation, supplier)
        RelNode scanNation = relBuilder.scan(NATION).build();
        RexNode rexFilterNation = relBuilder.call(
                SqlStdOperatorTable.EQUALS,
                getFieldInputRef(scanNation, "n_name"),
                rexBuilder.makeLiteral("CANADA"));

        RelNode filterNation = relBuilder.push(scanNation).filter(rexFilterNation).build();
        RelNode scanSupplier = relBuilder.scan(SUPPLIER).build();
        RelNode sup_nat = makeInnerJoin(filterNation, scanSupplier, "n_nationkey", "s_nationkey");


        RelNode result = relBuilder
                .push(sup_nat)
                .push(ps_line)
                .join(JoinRelType.SEMI,
                        makeEqualsRex(
                                getFieldInputRef(sup_nat, "s_suppkey"),
                                getFieldInputRefJoinRightSide(ps_line, "ps_suppkey", sup_nat)))
                .build();

        //   EnumerableProject(s_name=[$2], s_address=[$3])
        RelNode proj = relBuilder
                .push(result)
                .project(
                        getFieldInputRef(result, "s_name"),
                        getFieldInputRef(result, "s_address"))
                .build();


        // Sort(grouped)
        RelNode sorted = relBuilder
                .push(proj)
                .sort(0)
                .build();

        List<RelOptRule> enumerableRules = new LinkedList<>(EnumerableRules.rules());
        enumerableRules.remove(EnumerableRules.ENUMERABLE_MERGE_JOIN_RULE);
        enumerableRules.add(CoreRules.PROJECT_REMOVE);
        Program enumerableConverter = Programs.ofRules(enumerableRules);
        RelNode converted = enumerableConverter.run(
                sorted.getCluster().getPlanner(),
                sorted,
                getDesiredRootTraitSet(sorted),
                Collections.emptyList(), Collections.emptyList());

        if(session.isDebugEnabled()) {
            System.out.println("Logical Plan");
            System.out.println(RelOptUtil.toString(sorted));
            System.out.println("Enumerable Plan");
            System.out.println(RelOptUtil.toString(converted));
        }

        Thread.currentThread().setContextClassLoader(oldCl);
        return converted;
    }

    static RelTraitSet getDesiredRootTraitSet(RelNode root) {
        return root.getTraitSet()
                .replace(EnumerableConvention.INSTANCE)
//                .replace(root.getConvention())
                .simplify();
    }

    static RexNode makeEqualsRex(RexInputRef left, RexInputRef right) {
        return relBuilder.call(SqlStdOperatorTable.EQUALS, left, right);
    }

    static RexNode makeNotEqualsRex(RexInputRef left, RexInputRef right) {
        return relBuilder.call(SqlStdOperatorTable.NOT_EQUALS, left, right);
    }

    static RelNode makeInnerJoin(RelNode left, RelNode right, String leftInputRef, String rightInputRef) {
        RelDataTypeField leftInputRefIdx = getField(left, leftInputRef);
        RelDataTypeField rightInputRefIdx = getField(right, rightInputRef);
        return makeInnerJoin(left, right, leftInputRefIdx, rightInputRefIdx);
    }

    static RelNode makeInnerJoin(RelNode left, RelNode right, RelDataTypeField leftInputRef, RelDataTypeField rightInputRef) {
        return makeJoin(JoinRelType.INNER, left, right, leftInputRef, rightInputRef);
    }

    static RelNode makeJoin(JoinRelType joinRelType, RelNode left, RelNode right, RelDataTypeField leftInputRef, RelDataTypeField rightInputRef) {
        return relBuilder
                .push(left)
                .push(right)
                .join(joinRelType, makeEqualsRex(
                        rexBuilder.makeInputRef(leftInputRef.getType(), leftInputRef.getIndex()),
                        rexBuilder.makeInputRef(rightInputRef.getType(), left.getRowType().getFieldCount() + rightInputRef.getIndex())))
                .build();
    }

    static RelDataTypeField getField(RelNode relNode, String fieldName) {
        RelDataTypeField field = relNode.getRowType().getField(fieldName, false, true);
        if(field != null) {
            return  field;
        }
        List<String> l = relNode.getTable().getQualifiedName();
        return defaultSchema.getTable(l.get(l.size() - 1)).getRowType(typeFactory).getField(fieldName, false, true);
    }

    static RexInputRef getFieldInputRef(RelNode relNode, String fieldName) {
        RelDataTypeField field = getField(relNode, fieldName);
        return rexBuilder.makeInputRef(field.getType(), field.getIndex());
    }

    static RexInputRef getFieldInputRefJoinRightSide(RelNode relNode, String fieldName, RelNode left) {
        return rexBuilder.makeInputRef(relNode.getRowType(), left.getRowType().getFieldCount() + getField(relNode, fieldName).getIndex());
    }
}
