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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlanQ21 {

    public static final JavaTypeFactory typeFactory = new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);

    static final RexBuilder rexBuilder = new RexBuilder(typeFactory);
    static RelBuilder relBuilder;

    static final String
        NATION = "nation",
        LINEITEM = "lineitem",
        ORDERS = "orders",
        SUPPLIER = "supplier",
        S_NAME = "s_name";


    static final Map<String, List<String>> requiredFields = new ImmutableMap.Builder<String, List<String>>()
            .put("lineitem", ImmutableList.of("l_receiptdate","l_commitdate", "l_suppkey", "l_quantity", "l_orderkey"))
            .put("nation", ImmutableList.of("n_nationkey", "n_name"))
            .put("supplier", ImmutableList.of("s_name", "s_suppkey", "s_nationkey"))
            .put("orders", ImmutableList.of("o_orderstatus", "o_orderkey"))
            .build();


    static SchemaPlus defaultSchema;

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
                .defaultSchema(defaultSchema)
                .build();
        relBuilder = RelBuilder.create(config);


        // nat_sup = Join(nation, supplier)
        RelNode scanNation = relBuilder.scan(NATION).build();
        RexNode rexFilterNation = relBuilder.call(
                SqlStdOperatorTable.EQUALS,
                getFieldInputRef(scanNation, "n_name"),
                rexBuilder.makeLiteral("SAUDI ARABIA"));

        RelNode filterNation = relBuilder.push(scanNation).filter(rexFilterNation).build();
        RelNode scanSupplier = relBuilder.scan(SUPPLIER).build();
        RelNode joinNationSupplier = makeInnerJoin(filterNation, scanSupplier, "n_nationkey", "s_nationkey");

        // nat_sup_L1 = Join(nat_supp, lineitem1)
        RelNode scanLineitem1 = relBuilder.scan(LINEITEM).build();
        RexNode rexFilterLineitem1 = relBuilder.call(
                SqlStdOperatorTable.GREATER_THAN,
                getFieldInputRef(scanLineitem1, "l_receiptdate"),
                getFieldInputRef(scanLineitem1, "l_commitdate"));
        RelNode filterLineitem1 = relBuilder.push(scanLineitem1).filter(rexFilterLineitem1).build();
        RelNode joinNatSuppL1 = makeInnerJoin(joinNationSupplier, filterLineitem1, "s_suppkey", "l_suppkey");

        // nat_sup_L1_ord = Join(nat_sup_L1, orders)
        RelNode scanOrders = relBuilder.scan(ORDERS).build();
        RexNode rexFilterOrders = relBuilder.call(
                SqlStdOperatorTable.EQUALS,
                getFieldInputRef(scanOrders, "o_orderstatus"),
                rexBuilder.makeLiteral("F"));
        RelNode filterOrders = relBuilder.push(scanOrders).filter(rexFilterOrders).build();
        RelNode joinNatSuppL1Orders = makeInnerJoin(joinNatSuppL1, filterOrders, "l_orderkey", "o_orderkey");

        // nat_sup_L1_ord_L2 = AntiJoin(nat_sup_L1_ord, lineitem2)
        RelNode filterLineitem2 = filterLineitem1;
        RelNode joinNatSuppL1OrdersL2 =  relBuilder
                .push(joinNatSuppL1Orders)
                .push(filterLineitem2)
                .antiJoin(
                        makeEqualsRex(getFieldInputRef(joinNatSuppL1Orders, "l_orderkey"), getFieldInputRefJoinRightSide(filterLineitem2, "l_orderkey", joinNatSuppL1Orders)),
                        makeNotEqualsRex(getFieldInputRef(joinNatSuppL1Orders, "l_suppkey"), getFieldInputRefJoinRightSide(filterLineitem2, "l_suppkey", joinNatSuppL1Orders))
                )
                .build();

        // nat_sup_L1_ord_L2_L3 = SemiJoin(nat_sup_L1_ord_L2, lineitem3)
        RelNode scanLineitem3 = scanLineitem1;
        RelNode joinNatSuppL1OrdersL2L3 =  relBuilder
                .push(joinNatSuppL1OrdersL2)
                .push(scanLineitem3)
                .semiJoin(
                        makeEqualsRex(getFieldInputRef(joinNatSuppL1OrdersL2, "l_orderkey"), getFieldInputRefJoinRightSide(scanLineitem3, "l_orderkey", joinNatSuppL1OrdersL2)),
                        makeNotEqualsRex(getFieldInputRef(joinNatSuppL1OrdersL2, "l_suppkey"), getFieldInputRefJoinRightSide(scanLineitem3, "l_suppkey", joinNatSuppL1OrdersL2))
                )
                .build();

        RelNode joined = joinNatSuppL1OrdersL2L3;

        // GroupBy(joined)
        RelNode grouped = relBuilder
                .push(joined)
                .aggregate(
                        relBuilder.groupKey(S_NAME),
                        relBuilder.count().as("numwait"))
                .build();

        // Sort(grouped)
        RelNode sorted = relBuilder
                .push(grouped)
                .sort(-2, 0)
                .limit(0,100)
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
