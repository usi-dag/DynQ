package ch.usi.inf.dag.dynq.parser;

import ch.usi.inf.dag.dynq.language.LanguageSpecificExtension;
import ch.usi.inf.dag.dynq.language.TruffleLinqException;
import ch.usi.inf.dag.dynq.language.nodes.TruffleLinqRootNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.MultiKeysRexComparatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.RexComparatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.SingleKeyRexComparatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.GreaterThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.GreaterThanRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.LessThanEqualRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.LessThanRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.NotEqualsRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationMultipleAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationScalarAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateAvgNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateCountNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateCountWithExprNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateCountWithExprWithFilterNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateCountWithFilterNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateFunctionNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateMaxNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateMinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateSingleValueNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateSumNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateSumWithFilterNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby.HashGroupByOperatorMultiKeysBackedArrayNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby.HashGroupByOperatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby.HashGroupByOperatorSingleKeyNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.AllBinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.AnyBinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryEquiJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryRexContainerJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.OnlyLeftBinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.OnlyRightBinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.ReversedBinaryRexContainerJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.predicates.PredicateNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.predicates.PredicateRowExpressionNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.projections.ArrayProjectNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.projections.DynamicObjectProjectNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.projections.ProjectNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorLimitNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorPolyglotFinalizerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorPredicateNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorProjectionNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorRootNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.aggregations.VolcanoIteratorAggregateNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.aggregations.VolcanoIteratorHashGroupByFastHashNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.aggregations.VolcanoIteratorScalarAggregateNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin.VolcanoIteratorGroupInnerJoinSingleKeyNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin.VolcanoIteratorGroupLeftJoinSingleKeyNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin.VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftAggNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin.VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftScalarAggNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin.VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeRightAggNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerAntiHashJoinMultiKeysNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerAntiHashJoinSingleKeyNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerAntiHashJoinSingleKeyWithNonEquiJoinConditionNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerHashJoinMultiKeysNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerHashJoinMultiKeysWithNonEquiJoinConditionNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerHashJoinWithNonEquiJoinConditionNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerSemiHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorInnerSemiHashJoinWithNonEquiJoinConditionNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorLeftHashJoinMultiKeysNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorLeftHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins.VolcanoIteratorLeftHashJoinWithNonEquiJoinConditionNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.sorts.VolcanoIteratorLimitedSortNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.sorts.VolcanoIteratorSortNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.table_scans.VolcanoIteratorPolyglotTableScanNode;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.AggregateVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.FakeRemovedProjectVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.FilterVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.JoinVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.LimitVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.PolyglotTableScanVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.ProjectVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.SortVolcanoWrapper;
import ch.usi.inf.dag.dynq.parser.rex.RexTruffleNodeVisitor;
import ch.usi.inf.dag.dynq.parser.rex.RexTruffleNodeVisitorFactory;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.memory.PolyglotInputArrayTable;
import org.apache.calcite.adapter.enumerable.EnumerableLimit;
import org.apache.calcite.adapters.enumerable.MyRemovableEnumerableProject;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelFieldCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelVisitor;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinInfo;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.ImmutableBitSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.graalvm.collections.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class CalciteParser {

  static public boolean MERGE_AGG_PROJ = "true".equals(System.getenv().getOrDefault("DYNQ_MERGE_AGG_PROJ", "false"));
  static public boolean USE_GROUPJOIN = "true".equals(System.getenv().getOrDefault("DYNQ_USE_GROUPJOIN", "false"));
  static public boolean USE_REVERSED_GROUPJOIN = "true".equals(System.getenv().getOrDefault("DYNQ_USE_REVERSED_GROUPJOIN", "false"));
  static public boolean USE_REVERSED_SCALAR_GROUPJOIN = "true".equals(System.getenv().getOrDefault("DYNQ_USE_REVERSED_SCALAR_GROUPJOIN", "false"));
  static public boolean USE_SCALAR_AGGREGATION = "true".equals(System.getenv("DYNQ_USE_SCALAR_AGGREGATION"));
  static public boolean REUSE_AGGREGATIONS = "true".equals(System.getenv("DYNQ_REUSE_AGGREGATIONS"));
  static public boolean REMOVE_SIMPLE_PROJECTIONS = "true".equals(System.getenv("DYNQ_REMOVE_SIMPLE_PROJECTIONS"));

  private final APISessionManagement session;
  private final LanguageSpecificExtension languageSpecificExtension;

  public CalciteParser(APISessionManagement session, LanguageSpecificExtension extension) {
    this.session = session;
    languageSpecificExtension = extension;
  }

  public TruffleLinqRootNode parseSource(String sql) throws SqlParseException {
    return convertToTruffle(convertToRelNode(sql));
  }


  public RelNode convertToRelNode(String sql) throws SqlParseException {
    if(session.isDebugEnabled()) {
      System.out.println("Parsing SQL: " + sql);
    }
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(CalciteConnection.class.getClassLoader());
    SqlToCalciteRel converter = SqlToCalciteRel.factory(session);
    RelNode rel = converter.convert(sql);
    session.getRegisteredTables().resetDynamicTables();
    Thread.currentThread().setContextClassLoader(oldClassLoader);
    if(session.isDebugEnabled()) {
      findEqualsSubTrees(rel);
    }
    return rel;
  }


  private void findEqualsSubTrees(RelNode relNode) {
    HashMap<String, Integer> visited = new HashMap<>();
    RelVisitor visitor = new RelVisitor() {
      @Override
      public void visit(RelNode node, int ordinal, RelNode parent) {
        if(node instanceof TableScan) return;
        String nodeStr = RelOptUtil.toString(node);
        int nVisit = visited.getOrDefault(nodeStr, 0) + 1;
        visited.put(nodeStr, nVisit);
        if(nVisit > 1) {
          System.out.println("FIND MATCH");
          System.out.println(RelOptUtil.toString(node));
        } else {
          super.visit(node, ordinal, parent);
        }
      }
    };
    visitor.visit(relNode, 0, null);
  }

  public TruffleLinqRootNode convertToTruffle(RelNode relNode) {
    if(REMOVE_SIMPLE_PROJECTIONS) {
      // Get rid of removable projections
      new SetRemovableProjectionsVisitor().go(relNode);
    }

    RelToTruffleVisitor visitor = new RelToTruffleVisitor(session, languageSpecificExtension);
    RelNodeVolcanoWrapper pair = visitor.visit(relNode);
    String[] columnNames = relNode.getRowType().getFieldNames().toArray(new String[0]);
    VolcanoIteratorPolyglotFinalizerNode asPolyglot = new VolcanoIteratorPolyglotFinalizerNode(pair.getVolcanoIteratorNode(), columnNames);
    visitor.root.setChild(asPolyglot);
    TruffleLinqRootNode pushed = visitor.root.push();

    if(session.isDebugEnabled()) {
      System.out.println("Parsed to AST:");
      System.out.println(visitor.root.explain());
      System.out.println("Pushed Down as AST:");
      System.out.println(pushed.explain());
    }
    return pushed;
  }

  static class SetRemovableProjectionsVisitor extends RelVisitor {
    boolean seenTupleMaterializer = false;

    @Override
    public void visit(RelNode node, int ordinal, @Nullable RelNode parent) {
      if(node instanceof MyRemovableEnumerableProject && seenTupleMaterializer) {
        ((MyRemovableEnumerableProject) node).setRemovable();
      }
      if(node instanceof Project || node instanceof Aggregate) {
        seenTupleMaterializer = true;
      }
      super.visit(node, ordinal, parent);
    }
  }

  static class RelToTruffleVisitor { // TODO visitor? use as above
    final VolcanoIteratorRootNode root;
    final APISessionManagement session;
    final RexTruffleNodeVisitorFactory rexTruffleNodeVisitorFactory;
    final TableScanVisitor tableScanVisitor;
    final Map<String, Pair<RelNode, RelNodeVolcanoWrapper>> cachedVisitedRelNodes = new HashMap<>();


    RelToTruffleVisitor(APISessionManagement session, LanguageSpecificExtension languageSpecificExtension) {
      this.session = session;
      this.root = new VolcanoIteratorRootNode(languageSpecificExtension.getCurrentLanguage());
      this.rexTruffleNodeVisitorFactory = languageSpecificExtension.getRexTruffleNodeVisitorFactory();
      this.tableScanVisitor = languageSpecificExtension.getTableScanVisitor();
    }

    private RelNodeVolcanoWrapper visit(RelNode relNode) {
      // dispatcher
      // Table Scan
      if (relNode instanceof TableScan) {
        return visit((TableScan) relNode);
      }

      // Project
      if (relNode instanceof Project) {
        return visit((Project) relNode);
      }

      // Filter
      if (relNode instanceof Filter) {
        return visit((Filter) relNode);
      }

      // Aggregate
      if (relNode instanceof Aggregate) {
        if(!REUSE_AGGREGATIONS) {
          return visit((Aggregate) relNode);
        } else {
          Aggregate aggregate = (Aggregate) relNode;
          String relNodeStr = RelOptUtil.toString(relNode);
          if(cachedVisitedRelNodes.containsKey(relNodeStr)) {
            Pair<RelNode, RelNodeVolcanoWrapper> pair = cachedVisitedRelNodes.get(relNodeStr);
            if(session.isDebugEnabled()) {
              System.out.println("FIND REUSABLE RELNODE: " + relNode + " -- " + pair.getRight().getVolcanoIteratorNode().getClass().getName());
            }
            return pair.getRight();
          } else {
            RelNodeVolcanoWrapper relNodeVolcanoWrapper = visit(aggregate);
            cachedVisitedRelNodes.put(relNodeStr, Pair.create(aggregate, relNodeVolcanoWrapper));
            return relNodeVolcanoWrapper;
          }
        }
      }

      // Join
      if (relNode instanceof Join) {
        return visit((Join) relNode);
      }

      // Sort
      if (relNode instanceof Sort) {
        return visit((Sort) relNode);
      }

      // EnumerableLimit
      if (relNode instanceof EnumerableLimit) {
        return visit((EnumerableLimit) relNode);
      }

      throw new TruffleLinqException("Unknown relation expression type: " + relNode.getClass());
    }

    private RelNodeVolcanoWrapper visit(TableScan tableScan) {
      String tableName = tableScan.getTable().getQualifiedName().get(0);
      Table table = session.getRegisteredTables().getTable(tableName);
      RelNodeVolcanoWrapper fromExtensions = tableScanVisitor.visit(tableScan, session);
      if(fromExtensions != null) {
        return fromExtensions;
      }
      if(table instanceof PolyglotInputArrayTable) {
        PolyglotInputArrayTable polyglotTable = (PolyglotInputArrayTable) table;
        VolcanoIteratorNode volcano = VolcanoIteratorPolyglotTableScanNode.create(polyglotTable.getInput(), (int) polyglotTable.getLength());
        return new PolyglotTableScanVolcanoWrapper(volcano, tableScan);
      } else {
        throw new RuntimeException("Not a managed table: " + table + " of class: " + table.getClass());
      }
    }

    private RelNodeVolcanoWrapper visit(Project project) {
      RelNodeVolcanoWrapper childVisit = visit(project.getInput());
      if(project instanceof MyRemovableEnumerableProject && ((MyRemovableEnumerableProject) project).isRemovable()) {
        return new FakeRemovedProjectVolcanoWrapper(project, childVisit);
      }

      VolcanoIteratorProjectionNode volcanoIteratorProjectionNode =
              VolcanoIteratorProjectionNode.create(null, childVisit.getVolcanoIteratorNode());
      RelNodeVolcanoWrapper relNodeVolcanoWrapper = new ProjectVolcanoWrapper(project, volcanoIteratorProjectionNode, childVisit);
      RexTruffleNodeVisitor visitor = rexTruffleNodeVisitorFactory.create(session, relNodeVolcanoWrapper);

      List<RexNode> projections = project.getProjects();
      if(projections.size() == 1 && projections.get(0).getType().getSqlTypeName() == SqlTypeName.DYNAMIC_STAR) {
        // is a dynamic star project: do not use arrays
        // TODO: check that this project is the top one
        RexTruffleNode truffleNode = projections.get(0).accept(visitor);
        ProjectNode projectNode = new DynamicObjectProjectNode(truffleNode);
        volcanoIteratorProjectionNode.setProject(projectNode);
        return relNodeVolcanoWrapper;
      }

      RexTruffleNode[] projectionNodes = new RexTruffleNode[projections.size()];
      for (int i = 0; i < projections.size(); i++) {
        projectionNodes[i] = projections.get(i).accept(visitor);
      }
      ArrayRexTruffleNode arrayRexTruffleNode = ArrayRexTruffleNode.create(projectionNodes);
      ArrayProjectNode arrayProjectNode = ArrayProjectNode.create(arrayRexTruffleNode);
      volcanoIteratorProjectionNode.setProject(arrayProjectNode);
      return relNodeVolcanoWrapper;
    }

    private RelNodeVolcanoWrapper visit(Filter filter) {
      RelNodeVolcanoWrapper childVisit = visit(filter.getInput());
      VolcanoIteratorPredicateNode volcanoIteratorPredicateNode =
              VolcanoIteratorPredicateNode.create(childVisit.getVolcanoIteratorNode());
      RelNodeVolcanoWrapper relNodeVolcanoWrapper = new FilterVolcanoWrapper(filter, volcanoIteratorPredicateNode, childVisit);
      RexTruffleNodeVisitor visitor = rexTruffleNodeVisitorFactory.create(session, relNodeVolcanoWrapper);
      RexTruffleNode visitedPredicate = filter.getCondition().accept(visitor);
      PredicateNode predicateNode = PredicateRowExpressionNodeGen.create(visitedPredicate);

      volcanoIteratorPredicateNode.setPredicate(predicateNode);
      return relNodeVolcanoWrapper;
    }


    private RelNodeVolcanoWrapper visit(Aggregate aggregate) {
      if (aggregate.getGroupCount() == 0) {
        return visitAggregateWithoutGroups(aggregate);
      } else {
        return visitAggregateWithGroups(aggregate);
      }
    }

    private RelNodeVolcanoWrapper visitAggregateWithoutGroups(Aggregate aggregate) {
      if(aggregate.getInput() instanceof Project && MERGE_AGG_PROJ) {
        if(session.isDebugEnabled()) {
          System.out.printf("merge agg(%s) with proj(%s)\n", aggregate, aggregate.getInput());
        }
        return visitAggregateWithoutGroupsMergeProject(aggregate);
      }
      RelNodeVolcanoWrapper childVisit = visit(aggregate.getInput());

      // aggregations
      AggregateFunctionNode[] aggregateFunctionNodes = makeAggregators(aggregate.getAggCallList(), childVisit);
      if(aggregateFunctionNodes.length == 1 && USE_SCALAR_AGGREGATION) {
        // scalar aggregation
        AggregateFunctionNode aggregateFunctionNode = aggregateFunctionNodes[0];
        AggregationScalarAggregatorNode aggregatorNode = new AggregationScalarAggregatorNode(aggregateFunctionNode);
        VolcanoIteratorNode volcanoIteratorNode = new VolcanoIteratorScalarAggregateNode(aggregatorNode, childVisit.getVolcanoIteratorNode());
        return new AggregateVolcanoWrapper(aggregate, volcanoIteratorNode, childVisit);

      } else {
        AggregationMultipleAggregatorNode collectorNode = AggregationMultipleAggregatorNode.create(aggregateFunctionNodes);
        VolcanoIteratorNode volcanoIteratorNode = VolcanoIteratorAggregateNode.create(collectorNode, childVisit.getVolcanoIteratorNode());
        return new AggregateVolcanoWrapper(aggregate, volcanoIteratorNode, childVisit);
      }
    }

    private RelNodeVolcanoWrapper visitAggregateWithoutGroupsMergeProject(Aggregate aggregate) {
      Project childProject = (Project) aggregate.getInput();
      RelNodeVolcanoWrapper childVisit = visit(childProject.getInput());
      RexTruffleNodeVisitor visitor = makeGroupMergeProjectVisitor(childProject, childVisit);
      AggregateFunctionNode[] aggregateFunctionNodes =
              makeAggregatorsMergeProjections(aggregate.getAggCallList(), childProject.getProjects(), visitor);

      VolcanoIteratorNode volcanoChild = childVisit.getVolcanoIteratorNode();

      if(aggregateFunctionNodes.length == 1 && USE_SCALAR_AGGREGATION) {
        // scalar aggregation
        AggregateFunctionNode aggregateFunctionNode = aggregateFunctionNodes[0];
        AggregationScalarAggregatorNode aggregatorNode = new AggregationScalarAggregatorNode(aggregateFunctionNode);
        VolcanoIteratorNode volcanoIteratorNode = new VolcanoIteratorScalarAggregateNode(aggregatorNode, childVisit.getVolcanoIteratorNode());
        return new AggregateVolcanoWrapper(aggregate, volcanoIteratorNode, childVisit);
      } else {
        AggregationMultipleAggregatorNode collectorNode = AggregationMultipleAggregatorNode.create(aggregateFunctionNodes);
        VolcanoIteratorNode volcanoIteratorNode = VolcanoIteratorAggregateNode.create(collectorNode, volcanoChild);
        return new AggregateVolcanoWrapper(aggregate, volcanoIteratorNode, childVisit);
      }
    }

    private RelNodeVolcanoWrapper visitAggregateWithGroups(Aggregate aggregate) {
      if(aggregate.getInput() instanceof Project && MERGE_AGG_PROJ) {
        if(session.isDebugEnabled()) {
          System.out.printf("merge agg(%s) with proj(%s)\n", aggregate, aggregate.getInput());
        }
        return visitAggregateWithGroupsMergeProject(aggregate);
      }
      if(aggregate.getInput() instanceof Join && USE_GROUPJOIN) {
        if(session.isDebugEnabled()) {
          System.out.printf("try merging agg(%s) with join(%s)\n", aggregate, aggregate.getInput());
        }
        try {
          return visitGroupJoin(aggregate);
        } catch (RuntimeException e) {
          if(session.isDebugEnabled()) {
            System.out.printf("Cannot merge agg(%s) with join(%s)\n", aggregate, aggregate.getInput());
            System.out.println(e.getMessage());
          }
        }
      }

      RelNodeVolcanoWrapper childVisit = visit(aggregate.getInput());
      ArrayRexTruffleNode keyGetter = makeGroupKeyGetter(aggregate, childVisit::getOutputDataAccessor);
      AggregateFunctionNode[] aggregateFunctionNodes = makeAggregators(aggregate.getAggCallList(), childVisit);
      AggregationMultipleAggregatorNode aggregation =
              AggregationMultipleAggregatorNode.create(aggregateFunctionNodes, keyGetter.size());
      return makeGroupByWrapper(aggregation, keyGetter, childVisit, aggregate);
    }


    private RelNodeVolcanoWrapper visitAggregateWithGroupsMergeProject(Aggregate aggregate) {
      Project childProject = (Project) aggregate.getInput();
      RelNodeVolcanoWrapper childVisit = visit(childProject.getInput());
      List<RexNode> projections = childProject.getProjects();
      RexTruffleNodeVisitor visitor = makeGroupMergeProjectVisitor(childProject, childVisit);
      AggregateFunctionNode[] aggregateFunctionNodes =
              makeAggregatorsMergeProjections(aggregate.getAggCallList(), projections, visitor);
      ArrayRexTruffleNode keyGetter = makeGroupKeyGetter(aggregate, col -> projections.get(col).accept(visitor));
      AggregationMultipleAggregatorNode aggregation =
              AggregationMultipleAggregatorNode.create(aggregateFunctionNodes, keyGetter.size());
      return makeGroupByWrapper(aggregation, keyGetter, childVisit, aggregate);
    }

    private RexTruffleNodeVisitor makeGroupMergeProjectVisitor(Project childProject, RelNodeVolcanoWrapper childVisit) {
      VolcanoIteratorNode volcanoChild = childVisit.getVolcanoIteratorNode();
      VolcanoIteratorProjectionNode volcano = VolcanoIteratorProjectionNode.create(null, volcanoChild);
      RelNodeVolcanoWrapper relNodeVolcanoWrapper = new ProjectVolcanoWrapper(childProject, volcano, childVisit);
      return rexTruffleNodeVisitorFactory.create(session, relNodeVolcanoWrapper);
    }


    private ArrayRexTruffleNode makeGroupKeyGetter(Aggregate aggregate,
                                                   Function<Integer, RexTruffleNode> singleKeyGetter) {
      // groups key TODO now assuming single group
      ImmutableBitSet groupKeyBitSet = aggregate.groupSets.get(0);
      Set<Integer> groupByCols = groupKeyBitSet.asSet();
      RexTruffleNode[] keyGetterChildren = new RexTruffleNode[groupByCols.size()];
      int i = 0;
      for (int col : groupByCols) {
        keyGetterChildren[i++] = singleKeyGetter.apply(col);
      }
      return ArrayRexTruffleNode.create(keyGetterChildren);
    }

    private RelNodeVolcanoWrapper makeGroupByWrapper(AggregationMultipleAggregatorNode aggregation,
                                                     ArrayRexTruffleNode keyGetter,
                                                     RelNodeVolcanoWrapper childVisit,
                                                     Aggregate aggregate) {
      RexTruffleNode singleKey = null;
      if(keyGetter.size() == 1) {
        singleKey = keyGetter.getRexChildren()[0];
      }

      if(singleKey != null) {
        HashGroupByOperatorNode hashGroupByOperator = new HashGroupByOperatorSingleKeyNode(aggregation, singleKey);
        VolcanoIteratorHashGroupByFastHashNode groupBy = VolcanoIteratorHashGroupByFastHashNode.create(hashGroupByOperator, childVisit.getVolcanoIteratorNode());
        return new AggregateVolcanoWrapper(aggregate, groupBy, childVisit);
      } else {
        HashGroupByOperatorNode hashGroupByOperator = new HashGroupByOperatorMultiKeysBackedArrayNode(aggregation, keyGetter);
        VolcanoIteratorHashGroupByFastHashNode groupBy = VolcanoIteratorHashGroupByFastHashNode.create(hashGroupByOperator, childVisit.getVolcanoIteratorNode());
        return new AggregateVolcanoWrapper(aggregate, groupBy, childVisit);
      }
    }



    public AggregateFunctionNode resolveAggregateFunction(AggregateCall aggregateCall, RexTruffleNode inputGetter) {
      switch (aggregateCall.getAggregation().kind) {
        case SUM:
        case SUM0: // TODO SUM0 should be implemented like a CASE (i.e., if sum=0 return null else sum)
          return new AggregateSumNode(inputGetter);
        case COUNT:
          return inputGetter == null ? new AggregateCountNode() : new AggregateCountWithExprNode(inputGetter);
        case MIN:
          return new AggregateMinNode(inputGetter);
        case MAX:
          return new AggregateMaxNode(inputGetter);
        case AVG:
          return new AggregateAvgNode(inputGetter);
        case SINGLE_VALUE:
          return new AggregateSingleValueNode(inputGetter);

      }
      throw new RuntimeException("Unexpected aggregate call " + aggregateCall);
    }

    public AggregateFunctionNode resolveAggregateFunction(AggregateCall aggregateCall,
                                                          RexTruffleNode inputGetter,
                                                          RexTruffleNode filterNode) {
      if(filterNode == null) {
        return resolveAggregateFunction(aggregateCall, inputGetter);
      }
      switch (aggregateCall.getAggregation().kind) {
        case SUM:
        case SUM0: // TODO SUM0 should be implemented like a CASE (i.e., if sum=0 return null else sum)
          return new AggregateSumWithFilterNode(filterNode, inputGetter);
        case COUNT:
          return inputGetter == null
                  ? new AggregateCountWithFilterNode(filterNode)
                  : new AggregateCountWithExprWithFilterNode(filterNode, inputGetter);
      }
      throw new RuntimeException("Unexpected aggregate call with filter " + aggregateCall);
    }

    public AggregateFunctionNode resolveAggregateFunctionByChild(AggregateCall aggregateCall,
                                                                 RelNodeVolcanoWrapper child) {
      int nArgForCall = aggregateCall.getArgList().size();
      if(aggregateCall.filterArg < 0) {
        switch (nArgForCall) {
          case 0:
            return resolveAggregateFunction(aggregateCall, null);
          case 1:
            int inputRef = aggregateCall.getArgList().get(0);
            RexTruffleNode dataAccessor = child.getOutputDataAccessor(inputRef);
            return resolveAggregateFunction(aggregateCall, dataAccessor);
          default:
            throw new RuntimeException("Unexpected number of arguments for aggregate call, currently supported only 0 or 1, got: " + nArgForCall);
        }
      } else {
        RexTruffleNode filterNode = child.getOutputDataAccessor(aggregateCall.filterArg);
        switch (nArgForCall) {
          case 0:
            return resolveAggregateFunction(aggregateCall, null, filterNode);
          case 1:
            int inputRef = aggregateCall.getArgList().get(0);
            RexTruffleNode dataAccessor = child.getOutputDataAccessor(inputRef);
            return resolveAggregateFunction(aggregateCall, dataAccessor, filterNode);
          default:
            throw new RuntimeException("Unexpected number of arguments for aggregate call, currently supported only 0 or 1, got: " + nArgForCall);
        }
      }
    }


    private AggregateFunctionNode[] makeAggregators(List<AggregateCall> aggregateCallList, RelNodeVolcanoWrapper childVisit) {
      AggregateFunctionNode[] aggregateFunctionNodes = new AggregateFunctionNode[aggregateCallList.size()];
      for (int j = 0; j < aggregateCallList.size(); j++) {
        // TODO now assuming the call has zero or one input (i.e., an input ref of the row returned by child)
        AggregateCall aggregateCall = aggregateCallList.get(j);
        aggregateFunctionNodes[j] = resolveAggregateFunctionByChild(aggregateCall, childVisit);
      }
      return aggregateFunctionNodes;
    }

    private AggregateFunctionNode[] makeAggregatorsMergeProjections(List<AggregateCall> aggregateCallList,
                                                                    List<RexNode> projections,
                                                                    RexTruffleNodeVisitor visitor) {
      AggregateFunctionNode[] aggregateFunctionNodes = new AggregateFunctionNode[aggregateCallList.size()];
      for (int j = 0; j < aggregateCallList.size(); j++) {
        // TODO now assuming the call has zero or one input (i.e., an input ref of the row returned by child)
        AggregateCall aggregateCall = aggregateCallList.get(j);
        RexTruffleNode filterNode = aggregateCall.filterArg < 0 ? null : projections.get(aggregateCall.filterArg).accept(visitor);
        int nArgForCall = aggregateCall.getArgList().size();
        switch (nArgForCall) {
          case 0:
            aggregateFunctionNodes[j] = resolveAggregateFunction(aggregateCall, null, filterNode);
            break;
          case 1:
            int inputRef = aggregateCall.getArgList().get(0);
            RexTruffleNode dataAccessor = projections.get(inputRef).accept(visitor);
            aggregateFunctionNodes[j] = resolveAggregateFunction(aggregateCall, dataAccessor, filterNode);
            break;
          default:
            throw new RuntimeException("Unexpected number of arguments for aggregate call, currently supported only 0 or 1, got: " + nArgForCall);
        }
      }
      return aggregateFunctionNodes;
    }

    private RelNodeVolcanoWrapper visitGroupJoin(Aggregate aggregate) {
      Join join = (Join) aggregate.getInput();
      JoinInfo joinInfo = join.analyzeCondition();
      int nKeys = joinInfo.leftKeys.size();
      if(nKeys == 0 || nKeys != joinInfo.rightKeys.size()) {
        throw new RuntimeException("cannot make group join with no keys or different left/right keys");
      }

      ImmutableBitSet groupKeyBitSet = aggregate.groupSets.get(0);
      Set<Integer> groupByCols = groupKeyBitSet.asSet();

      // TODO currently only for single key
      if(nKeys > 1 || groupByCols.size() > 1) {
        throw new RuntimeException("cannot make group join with multiple keys");
      }

      // TODO currently only equijoin
      if(!joinInfo.isEqui()) {
        throw new RuntimeException("cannot make group join with non equijoin");
      }

      int leftKey = joinInfo.leftKeys.get(0);
      int rightKey = joinInfo.rightKeys.get(0);
      int groupKey = groupByCols.iterator().next();
      int rightKeyForGroupMatch = rightKey + join.getLeft().getRowType().getFieldCount();

      if(!(groupKey == leftKey || groupKey == rightKeyForGroupMatch)) {
        throw new RuntimeException("group join requires group by having same key as left or right join inputs");
      }

      RelNodeVolcanoWrapper leftChild = visit(join.getLeft());
      RelNodeVolcanoWrapper rightChild = visit(join.getRight());
      RexTruffleNode leftKeyGetter = leftChild.getOutputDataAccessor(leftKey);
      RexTruffleNode rightKeyGetter = rightChild.getOutputDataAccessor(rightKey);

      RelNodeVolcanoWrapper joinVisit = visitHashJoin(join);
      AggregateFunctionNode[] aggregateFunctionNodes = makeAggregators(aggregate.getAggCallList(), joinVisit);
      AggregationMultipleAggregatorNode aggregation =
              AggregationMultipleAggregatorNode.create(aggregateFunctionNodes, 1);

      if(join.getJoinType() == JoinRelType.INNER) {
        VolcanoIteratorGroupInnerJoinSingleKeyNode groupJoin = new VolcanoIteratorGroupInnerJoinSingleKeyNode(
                leftChild.getVolcanoIteratorNode(),
                rightChild.getVolcanoIteratorNode(),
                leftKeyGetter, rightKeyGetter, aggregation);
        return new AggregateVolcanoWrapper(aggregate, groupJoin, joinVisit);
      } else if (join.getJoinType() == JoinRelType.LEFT) {
        VolcanoIteratorGroupLeftJoinSingleKeyNode groupJoin = new VolcanoIteratorGroupLeftJoinSingleKeyNode(
                leftChild.getVolcanoIteratorNode(),
                rightChild.getVolcanoIteratorNode(),
                leftKeyGetter, rightKeyGetter, aggregation);
        return new AggregateVolcanoWrapper(aggregate, groupJoin, joinVisit);
      }
      throw new RuntimeException("unsupported group join -- got: " + join.getJoinType());
    }

    private RelNodeVolcanoWrapper visit(Join join) {
      try {
        return visitHashJoin(join);
      } catch (Exception e) {
        if(session.isDebugEnabled()) {
          System.out.println("Cannot use HashJoin for " + join + " -- got: " + e);
        }
      }
      RelNodeVolcanoWrapper leftChild = visit(join.getLeft());
      RelNodeVolcanoWrapper rightChild = visit(join.getRight());

      boolean produceOneRowOnLeft =
              join.getLeft() instanceof Aggregate && ((Aggregate) join.getLeft()).getGroupSet().isEmpty();
      VolcanoIteratorJoinNode volcanoIteratorJoinNode = VolcanoIteratorJoinNode.create(
              leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(), produceOneRowOnLeft);

      JoinVolcanoWrapper joinVolcanoWrapper = new JoinVolcanoWrapper(join, volcanoIteratorJoinNode, leftChild, rightChild);

      if(join.getCondition().isAlwaysTrue()) {
        return joinVolcanoWrapper;
      }
      if (join.getCondition() instanceof RexCall) {
        RexCall condition = (RexCall) join.getCondition();
        BinaryJoinCondition simpleCondition = fitsSimpleJoinCondition(joinVolcanoWrapper, condition);
        if(simpleCondition != null) {
          volcanoIteratorJoinNode.setJoinCondition(simpleCondition);
          return joinVolcanoWrapper;
        }
      }
      throw new RuntimeException("Unknown join condition: " + join.getCondition());
    }


    private RelNodeVolcanoWrapper visitHashJoin(Join join) {
      if(session.isDebugEnabled()) {
        System.out.println("try hash join");
      }
      RelNodeVolcanoWrapper leftChild = visit(join.getLeft());
      RelNodeVolcanoWrapper rightChild = visit(join.getRight());
      JoinVolcanoWrapper joinVolcanoWrapper = new JoinVolcanoWrapper(join, null, leftChild, rightChild);
      JoinInfo info = joinVolcanoWrapper.getJoinInfo();
      BinaryJoinCondition nonEquiCondition = getNonEquiJoinCondition(joinVolcanoWrapper);
      // TODO if left (right?) child is scalar aggregation, optimize
      if(info.leftKeys.size() != info.rightKeys.size()) {
        throw new RuntimeException("Different number of keys from left and right: " + join);
      }
      if(info.leftKeys.size() == 0) {
        throw new RuntimeException("Hash Join inefficient with no keys");
      }

      // This should be a valid HashJoin
      int nKeys = info.leftKeys.size();

      if(USE_REVERSED_GROUPJOIN) {
        try {
          return visitHashJoinMergeAggregation(join);
        } catch (Exception e) {
          if(session.isDebugEnabled()) {
            System.out.println("Cannot merge aggregation -- got: " + e);
          }
        }
      }

      // Single key joins

      if(nKeys == 1) { // Single key can be optimized (i.e., we do not need an array for keys)
        if(session.isDebugEnabled()) {
          System.out.println("Valid HashJoin with single key!" + join);
        }

        RexTruffleNode leftKeyGetter = leftChild.getOutputDataAccessor(info.leftKeys.get(0));
        RexTruffleNode rightKeyGetter = rightChild.getOutputDataAccessor(info.rightKeys.get(0));
        // INNER Join
        if(join.getJoinType() == JoinRelType.INNER) {
          VolcanoIteratorNode joinNode;
          if(nonEquiCondition == null) { // w/o non-equi condition
            joinNode = VolcanoIteratorInnerHashJoinNode.create(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter);
          } else { // with non-equi condition
            joinNode = VolcanoIteratorInnerHashJoinWithNonEquiJoinConditionNode.create(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter, nonEquiCondition);
          }
          joinVolcanoWrapper.setVolcanoIteratorNode(joinNode);
          return joinVolcanoWrapper;
        }
        // LEFT Join
        else if(join.getJoinType() == JoinRelType.LEFT) {
          VolcanoIteratorNode joinNode;
          if(nonEquiCondition == null) { // w/o non-equi condition
            joinNode = VolcanoIteratorLeftHashJoinNode.create(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter);
          } else { // with non-equi condition
            joinNode = VolcanoIteratorLeftHashJoinWithNonEquiJoinConditionNode.create(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter, nonEquiCondition);
          }
          joinVolcanoWrapper.setVolcanoIteratorNode(joinNode);
          return joinVolcanoWrapper;
        } else if(join.isSemiJoin()) {
          VolcanoIteratorNode joinNode;
          if(nonEquiCondition == null) { // w/o non-equi condition
            joinNode = new VolcanoIteratorInnerSemiHashJoinNode(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter);
          } else { // with non-equi condition
            joinNode = new VolcanoIteratorInnerSemiHashJoinWithNonEquiJoinConditionNode(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter, nonEquiCondition);
          }
          joinVolcanoWrapper.setVolcanoIteratorNode(joinNode);
          return joinVolcanoWrapper;
        } else if(join.getJoinType() == JoinRelType.ANTI) {
          VolcanoIteratorNode joinNode;
          if(nonEquiCondition == null) { // w/o non-equi condition
            joinNode = new VolcanoIteratorInnerAntiHashJoinSingleKeyNode(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter);
          } else { // with non-equi condition
            joinNode = new VolcanoIteratorInnerAntiHashJoinSingleKeyWithNonEquiJoinConditionNode(
                    leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                    leftKeyGetter, rightKeyGetter, nonEquiCondition);
          }
          joinVolcanoWrapper.setVolcanoIteratorNode(joinNode);
          return joinVolcanoWrapper;
        } else {
          throw new RuntimeException("Not yet implemented join type: " + join.getJoinType());
        }
      }

      if(session.isDebugEnabled()) {
        System.out.println("Valid HashJoin with multiple keys!" + join);
      }

      // Multiple keys joins

      RexTruffleNode[] leftKeyGetters = new RexTruffleNode[nKeys];
      RexTruffleNode[] rightKeyGetters = new RexTruffleNode[nKeys];

      for (int i = 0; i < nKeys; i++) {
        leftKeyGetters[i] = leftChild.getOutputDataAccessor(info.leftKeys.get(i));
        rightKeyGetters[i] = rightChild.getOutputDataAccessor(info.rightKeys.get(i));
      }
      ArrayRexTruffleNode leftKeyGetter = ArrayRexTruffleNode.create(leftKeyGetters);
      ArrayRexTruffleNode rightKeyGetter = ArrayRexTruffleNode.create(rightKeyGetters);

      if(join.getJoinType() == JoinRelType.INNER) {
        VolcanoIteratorNode joinNode;
        if(nonEquiCondition == null) { // w/o non-equi condition
          joinNode = VolcanoIteratorInnerHashJoinMultiKeysNode.create(
                  leftChild.getVolcanoIteratorNode(),
                  rightChild.getVolcanoIteratorNode(),
                  leftKeyGetter,
                  rightKeyGetter);
        } else { // with non-equi condition
          joinNode = VolcanoIteratorInnerHashJoinMultiKeysWithNonEquiJoinConditionNode.create(
                  leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                  leftKeyGetter, rightKeyGetter, nonEquiCondition);
        }
        joinVolcanoWrapper.setVolcanoIteratorNode(joinNode);
        return joinVolcanoWrapper;
      } else if(join.getJoinType() == JoinRelType.LEFT) {
        VolcanoIteratorNode joinNode;
        if(nonEquiCondition == null) {
          joinNode = VolcanoIteratorLeftHashJoinMultiKeysNode.create(
                  leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                  leftKeyGetter, rightKeyGetter);
        } else {
          throw new RuntimeException(
                  "Not yet implemented join type with non-equi condition (multikeys): " + join.getJoinType());
        }
        joinVolcanoWrapper.setVolcanoIteratorNode(joinNode);
        return joinVolcanoWrapper;
      } // TODO semi join multikeys
      else if(join.getJoinType() == JoinRelType.ANTI) {
        if(nonEquiCondition == null) { // w/o non-equi condition
          VolcanoIteratorNode joinNode = new VolcanoIteratorInnerAntiHashJoinMultiKeysNode(
                  leftChild.getVolcanoIteratorNode(), rightChild.getVolcanoIteratorNode(),
                  leftKeyGetter, rightKeyGetter);
          joinVolcanoWrapper.setVolcanoIteratorNode(joinNode);
          return joinVolcanoWrapper;
        } else { // with non-equi condition
          throw new RuntimeException(
                  "Not yet implemented join type with non-equi condition (multikeys): " + join.getJoinType());
        }
      }
      else {
        throw new RuntimeException("Not yet implemented join type: " + join.getJoinType());
      }

    }

    private RelNodeVolcanoWrapper visitHashJoinMergeAggregation(Join join) {
      if(!(join.getLeft() instanceof Aggregate || join.getRight() instanceof Aggregate)) {
        throw new RuntimeException("visitHashJoinMergeAggregation: no agg to merge");
      }
      Aggregate aggLeft = null, aggRight = null;
      if(join.getLeft() instanceof Aggregate) aggLeft = (Aggregate) join.getLeft();
      if(join.getRight() instanceof Aggregate) aggRight = (Aggregate) join.getRight();

      JoinInfo joinInfo = join.analyzeCondition();

      // TODO think about aggregate merge project -- it may disturb this step

      if(aggLeft != null) {

        ImmutableBitSet groupKeyBitSet = aggLeft.groupSets.get(0);
        Set<Integer> groupByCols = groupKeyBitSet.asSet();
        int nJoinKeys = joinInfo.leftKeys.size();

        if(USE_REVERSED_SCALAR_GROUPJOIN && nJoinKeys == 1 && groupByCols.size() == 0) {
          RelNodeVolcanoWrapper visitedAggregate = visit(aggLeft);
          AggregateVolcanoWrapper aggregateVolcanoWrapper = (AggregateVolcanoWrapper) visitedAggregate;
          if (aggregateVolcanoWrapper.getVolcanoIteratorNode() instanceof VolcanoIteratorScalarAggregateNode) {
            AggregateFunctionNode[] aggregateFunctionNodes = makeAggregators(aggLeft.getAggCallList(), visit(aggLeft.getInput()));
            AggregateFunctionNode aggregateFunctionNode = aggregateFunctionNodes[0];
            AggregationScalarAggregatorNode aggregatorNode = new AggregationScalarAggregatorNode(aggregateFunctionNode);

            boolean hasMergedProject = aggLeft.getInput() instanceof Project &&
                    !(visitedAggregate.getInput(0) instanceof Project);
            RelNodeVolcanoWrapper leftChild = hasMergedProject
                    ? visit(aggLeft.getInput().getInput(0))
                    : visit(aggLeft.getInput());

            RelNodeVolcanoWrapper rightChild = visit(join.getRight());
            int rightKey = joinInfo.rightKeys.get(0);
            RexTruffleNode rightKeyGetter = rightChild.getOutputDataAccessor(rightKey);

            if (join.getJoinType() == JoinRelType.INNER) {
              BinaryJoinCondition nonEquiCondition = getNonEquiJoinCondition(new JoinVolcanoWrapper(join, null, visitedAggregate, rightChild));
              VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftScalarAggNode groupJoin = new VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftScalarAggNode(
                      leftChild.getVolcanoIteratorNode(),
                      rightChild.getVolcanoIteratorNode(),
                      rightKeyGetter, nonEquiCondition, aggregatorNode);
              return new JoinVolcanoWrapper(join, groupJoin, visitedAggregate, rightChild);
            }else {
              throw new RuntimeException("visitHashJoinMergeAggregation: cannot make reversed group join (merge left) for " + join.getJoinType());
            }
          }
        }

        // TODO currently only for single key - make it works also for multiple keys
        if(nJoinKeys != 1 || groupByCols.size() != 1) {
          if(session.isDebugEnabled()) {
            System.out.println("visitHashJoinMergeAggregation: (left) cannot make group join with multiple keys");
          }
        } else {
          int leftKey = joinInfo.leftKeys.get(0);
          int rightKey = joinInfo.rightKeys.get(0);
          int groupKey = groupByCols.iterator().next();

          if(leftKey != 0) {
            if(session.isDebugEnabled()) {
              System.out.println("visitHashJoinMergeAggregation: (left) group join (reversed) requires group by having same key as left or right join inputs");
            }
          } else {

            RelNodeVolcanoWrapper visitedAggregate = visit(aggLeft);
            boolean hasMergedProject = aggLeft.getInput() instanceof Project &&
                    !(visitedAggregate.getInput(0) instanceof Project);

            RelNodeVolcanoWrapper leftChild = hasMergedProject
                    ? visit(aggLeft.getInput().getInput(0))
                    : visit(aggLeft.getInput());

            RelNodeVolcanoWrapper rightChild = visit(join.getRight());
            RexTruffleNode leftKeyGetter = leftChild.getOutputDataAccessor(groupKey);
            RexTruffleNode rightKeyGetter = rightChild.getOutputDataAccessor(rightKey);

            AggregateFunctionNode[] aggregateFunctionNodes = makeAggregators(aggLeft.getAggCallList(), visit(aggLeft.getInput()));
            AggregationMultipleAggregatorNode aggregation =
                    AggregationMultipleAggregatorNode.create(aggregateFunctionNodes, 1);

            if (join.getJoinType() == JoinRelType.INNER) {
              BinaryJoinCondition nonEquiCondition = getNonEquiJoinCondition(new JoinVolcanoWrapper(join, null, visitedAggregate, rightChild));
              VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftAggNode groupJoin = new VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftAggNode(
                      leftChild.getVolcanoIteratorNode(),
                      rightChild.getVolcanoIteratorNode(),
                      leftKeyGetter, rightKeyGetter, nonEquiCondition, aggregation);
              return new JoinVolcanoWrapper(join, groupJoin, visitedAggregate, rightChild);
            } else {
              throw new RuntimeException("visitHashJoinMergeAggregation: cannot make reversed group join (merge left) for " + join.getJoinType());
            }
          }
        }
      }
      if(aggRight != null){
        RelNodeVolcanoWrapper visitedAggregate = visit(aggRight);
        int nJoinKeys = joinInfo.rightKeys.size();

        ImmutableBitSet groupKeyBitSet = aggRight.groupSets.get(0);
        Set<Integer> groupByCols = groupKeyBitSet.asSet();

        // TODO currently only for single key - make it works also for multiple keys

        if(nJoinKeys != 1 || groupByCols.size() != 1) {
          throw new RuntimeException("visitHashJoinMergeAggregation: (right) cannot make group join with multiple keys");
        }

        int leftKey = joinInfo.leftKeys.get(0);
        int rightKey = joinInfo.rightKeys.get(0);
        int groupKey = groupByCols.iterator().next();

        if(rightKey != 0) {
          throw new RuntimeException("visitHashJoinMergeAggregation: group join requires (reversed) group by having same key as left or right join inputs");
        }

        boolean hasMergedProject = aggRight.getInput() instanceof Project &&
                !(visitedAggregate.getInput(0) instanceof Project);

        RelNodeVolcanoWrapper leftChild = visit(join.getLeft());
        RelNodeVolcanoWrapper rightChild = hasMergedProject
                ? visit(aggRight.getInput().getInput(0))
                : visit(aggRight.getInput());
        RexTruffleNode leftKeyGetter = leftChild.getOutputDataAccessor(leftKey);
        RexTruffleNode rightKeyGetter = rightChild.getOutputDataAccessor(groupKey);


        AggregateFunctionNode[] aggregateFunctionNodes = makeAggregators(aggRight.getAggCallList(), visit(aggRight.getInput()));
        AggregationMultipleAggregatorNode aggregation =
                AggregationMultipleAggregatorNode.create(aggregateFunctionNodes, 1);
        if (join.getJoinType() == JoinRelType.INNER) {
          BinaryJoinCondition nonEquiCondition = getNonEquiJoinCondition(new JoinVolcanoWrapper(join, null, leftChild, visitedAggregate));
          VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeRightAggNode groupJoin = new VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeRightAggNode(
                  leftChild.getVolcanoIteratorNode(),
                  rightChild.getVolcanoIteratorNode(),
                  leftKeyGetter, rightKeyGetter, nonEquiCondition, aggregation);
          return new JoinVolcanoWrapper(join, groupJoin, leftChild, visit(aggRight));
        } else {
          throw new RuntimeException("visitHashJoinMergeAggregation: cannot make reversed group join (merge right) for " + join.getJoinType());
        }
      }


      throw new RuntimeException("visitHashJoinMergeAggregation: cannot make reversed group join");
    }

    private BinaryJoinCondition getNonEquiJoinCondition(JoinVolcanoWrapper joinVolcanoWrapper) {
      JoinInfo joinInfo = joinVolcanoWrapper.getJoinInfo();
      if(joinInfo.nonEquiConditions.size() > 0) {
        // TODO revise this builder (maybe join.getCluster().getTypeFactory())
        RexBuilder builder = new RexBuilder(new JavaTypeFactoryImpl());
        RexNode nonEquiConditionNode = RexUtil.composeConjunction(builder, joinInfo.nonEquiConditions);
        if(nonEquiConditionNode instanceof RexCall) {
          return fitsSimpleJoinCondition(joinVolcanoWrapper, (RexCall) nonEquiConditionNode);
        }
      }
      return null;
    }

    private BinaryJoinCondition fitsSimpleJoinCondition(JoinVolcanoWrapper join, RexCall condition) {

      // Common visitors
      RelNodeVolcanoWrapper leftWrapper = join.getLeftWrapper();
      RelNodeVolcanoWrapper rightWrapper = join.getRightWrapper();
      Pair<RexTruffleNodeVisitor, RexTruffleNodeVisitor> pairVisitor = rexTruffleNodeVisitorFactory.createPair(session, leftWrapper, rightWrapper);
      RexTruffleNodeVisitor leftVisitor = pairVisitor.getLeft();
      RexTruffleNodeVisitor rightVisitor = pairVisitor.getRight();
      // Due to recursion, the condition could be resolved only on one side
      if(join.canBeResolvedOnLeftSide(condition)) {
        RexTruffleNode leftIthCondition = condition.accept(leftVisitor);
        return new OnlyLeftBinaryJoinCondition(leftIthCondition);
      } else if(join.canBeResolvedOnRightSide(condition)) {
        RexTruffleNode rightIthCondition = condition.accept(rightVisitor);
        return new OnlyRightBinaryJoinCondition(rightIthCondition);
      }

      if(condition.op.kind == SqlKind.AND || condition.op.kind == SqlKind.OR) {
        BinaryJoinCondition[] conditions = new BinaryJoinCondition[condition.operands.size()];
        for (int i = 0; i < condition.operands.size(); i++) {
          RexNode ith = condition.operands.get(i);
          if(!(ith instanceof RexCall)) {
            return null;
          }
          conditions[i] = fitsSimpleJoinCondition(join, (RexCall) ith);
          if(conditions[i] == null) {
            return null;
          }
        }
        return condition.op.kind == SqlKind.AND
                ? new AllBinaryJoinCondition(conditions)
                : new AnyBinaryJoinCondition(conditions);
      } else if (condition.operands.size() == 2) {
        RexNode fstOp = condition.operands.get(0);
        RexNode sndOp = condition.operands.get(1);
        if(join.canBeResolvedOnLeftSide(fstOp) && join.canBeResolvedOnRightSide(sndOp)) {
          RexTruffleNode left = fstOp.accept(leftVisitor);
          RexTruffleNode right = sndOp.accept(rightVisitor);
          return resolveSimpleCondition(left, right, condition);
        } else if(join.canBeResolvedOnLeftSide(sndOp) && join.canBeResolvedOnRightSide(fstOp)) {
          RexTruffleNode left = sndOp.accept(leftVisitor);
          RexTruffleNode right = fstOp.accept(rightVisitor);
          return resolveReversedSimpleCondition(left, right, condition);
        }
      }

      return null;
    }

    private BinaryJoinCondition resolveSimpleCondition(RexTruffleNode left, RexTruffleNode right, RexCall condition) {
      if(condition.op.kind == SqlKind.EQUALS) {
        return new BinaryEquiJoinCondition(left, right);
      } else if (condition.op.kind == SqlKind.NOT_EQUALS) {
        return new BinaryRexContainerJoinCondition(left, right, NotEqualsRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.LESS_THAN) {
        return new BinaryRexContainerJoinCondition(left, right, LessThanRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.LESS_THAN_OR_EQUAL) {
        return new BinaryRexContainerJoinCondition(left, right, LessThanEqualRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.GREATER_THAN) {
        return new BinaryRexContainerJoinCondition(left, right, GreaterThanRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.GREATER_THAN_OR_EQUAL) {
        return new BinaryRexContainerJoinCondition(left, right, GreaterThanEqualRexTruffleNodeGen.create());
      }
      return null;
    }

    private BinaryJoinCondition resolveReversedSimpleCondition(RexTruffleNode left, RexTruffleNode right, RexCall condition) {
      if(condition.op.kind == SqlKind.EQUALS) {
        return new BinaryEquiJoinCondition(left, right);
      } else if (condition.op.kind == SqlKind.NOT_EQUALS) {
        return new ReversedBinaryRexContainerJoinCondition(left, right, NotEqualsRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.LESS_THAN) {
        return new ReversedBinaryRexContainerJoinCondition(left, right, LessThanRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.LESS_THAN_OR_EQUAL) {
        return new ReversedBinaryRexContainerJoinCondition(left, right, LessThanEqualRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.GREATER_THAN) {
        return new ReversedBinaryRexContainerJoinCondition(left, right, GreaterThanRexTruffleNodeGen.create());
      } else if (condition.op.kind == SqlKind.GREATER_THAN_OR_EQUAL) {
        return new ReversedBinaryRexContainerJoinCondition(left, right, GreaterThanEqualRexTruffleNodeGen.create());
      }
      return null;
    }

    private RelNodeVolcanoWrapper visit(Sort sort) {
      RelNodeVolcanoWrapper childVisit = visit(sort.getInput());
      List<RexNode> children = sort.getSortExps();
      RexTruffleNode[] keys = new RexTruffleNode[children.size()];
      SortVolcanoWrapper sortVolcanoWrapper = new SortVolcanoWrapper(sort, null, childVisit);
      boolean[] reversed = new boolean[children.size()];

      RexTruffleNodeVisitor visitor = rexTruffleNodeVisitorFactory.create(session, sortVolcanoWrapper);

      // TODO: double check -- getSortExps and getCollation().getFieldCollations() may return different lists
      for (int i = 0; i < children.size(); i++) {
        keys[i] = children.get(i).accept(visitor);
        reversed[i] = sort.getCollation().getFieldCollations().get(i).direction == RelFieldCollation.Direction.DESCENDING;
      }

      VolcanoIteratorSortNode sortNode;
      RexComparatorNode comparatorNode = new MultiKeysRexComparatorNode(keys, reversed);
      if(keys.length == 1) {
        comparatorNode = new SingleKeyRexComparatorNode(keys[0], reversed[0]);
      }

      if(sort.fetch == null) {
        sortNode = VolcanoIteratorSortNode.create(childVisit.getVolcanoIteratorNode(), comparatorNode);
      } else {
        int limit = RexLiteral.intValue(sort.fetch);
        sortNode = VolcanoIteratorLimitedSortNode.create(childVisit.getVolcanoIteratorNode(), comparatorNode, limit);
      }
      sortVolcanoWrapper.setVolcanoIteratorNode(sortNode);
      return sortVolcanoWrapper;
    }

    private RelNodeVolcanoWrapper visit(EnumerableLimit limit) {
      if(limit.offset != null) {
        throw new RuntimeException("Currently Limit is supported only without offset");
      }
      RelNodeVolcanoWrapper childVisit = visit(limit.getInput());
      if(childVisit instanceof SortVolcanoWrapper) {
        SortVolcanoWrapper child = (SortVolcanoWrapper) childVisit;
        return child.withLimit(RexLiteral.intValue(limit.fetch));
      }
      VolcanoIteratorLimitNode limitNode =
              new VolcanoIteratorLimitNode(childVisit.getVolcanoIteratorNode(), RexLiteral.intValue(limit.fetch));
      return new LimitVolcanoWrapper(limit, limitNode, childVisit);
    }

  }

}