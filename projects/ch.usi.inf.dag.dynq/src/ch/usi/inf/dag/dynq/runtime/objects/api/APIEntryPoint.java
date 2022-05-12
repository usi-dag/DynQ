package ch.usi.inf.dag.dynq.runtime.objects.api;

import ch.usi.inf.dag.dynq.language.LanguageSpecificExtension;
import ch.usi.inf.dag.dynq.language.nodes.TruffleLinqRootNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.RexDynamicParameterTruffleNode;
import ch.usi.inf.dag.dynq.parser.CalciteParser;
import ch.usi.inf.dag.dynq.runtime.objects.data.DynQStringValue;
import ch.usi.inf.dag.dynq.runtime.objects.resultsets.PolyglotArrayProxyResultSet;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.RootNode;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.RelNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


@ExportLibrary(InteropLibrary.class)
public class APIEntryPoint extends AbstractAPITruffleObject {

  static final String SESSION = "session";
  static final String SQL = "sql";
  static final String SQL_CALCITE = "sqlCalcite";
  static final String PREPARE = "prepare";
  static final String PREPARE_PARAMETRIC = "prepareParametric";
  // for benchmarks
  static final String PREPARE_PARSED = "parseQuery";
  static final String PREPARE_PARSED_AST = "parseQueryAst";
  static final String GC = "gc";
  final APISessionManagement session;
  private final LanguageSpecificExtension languageSpecificExtension;

  // TODO fix member-set and invocable members  (required for R)
  private final ExecSqlMember executor;
  private final CreatePreparedQuery queryPreparator;
  private final CreateParsedQuery queryParser;
  private final CreateRunGC runGcPreparator;
  private final CreateParsedAstQuery parsedAstQueryPreparator;

  public APIEntryPoint(LanguageSpecificExtension languageSpecificExtension) {
    super(SESSION, SQL, PREPARE, GC, PREPARE_PARSED, PREPARE_PARSED_AST);
    this.languageSpecificExtension = languageSpecificExtension;
    session = languageSpecificExtension.getApiSessionManagement();
    executor = new ExecSqlMember(this);
    queryPreparator = new CreatePreparedQuery(this);
    queryParser = new CreateParsedQuery(this);
    runGcPreparator = new CreateRunGC(session);
    parsedAstQueryPreparator = new CreateParsedAstQuery(this);
  }


  // Interop

  @Override
  @ExportMessage
  public Object readMember(String name) throws UnknownIdentifierException {
    if(SESSION.equals(name)) return session;
    if(SQL.equals(name)) return executor;
    if(PREPARE.equals(name)) return queryPreparator;
    if(PREPARE_PARSED.equals(name)) return queryParser;
    if(GC.equals(name)) return runGcPreparator;
    if(PREPARE_PARSED_AST.equals(name)) return parsedAstQueryPreparator;
    throw UnknownIdentifierException.create(name);
  }

  @ExportMessage
  static class InvokeMember {

    @Specialization(guards = "SQL.equals(memberName)")
    static public Object sqlCached(APIEntryPoint receiver, String memberName, Object[] arguments,
                                   @Cached("parse(arguments, receiver)") DirectCallNode call)  {
      return call.call();
    }

    @Specialization(guards = {"SQL.equals(memberName)"}, replaces = "sqlCached")
    static public Object sqlUncached(APIEntryPoint receiver, String memberName, Object[] arguments)  {
      System.out.println("Executing SQL Uncached");
      DirectCallNode call = parse(arguments, receiver);
      return call.call();
    }

    @Specialization(guards = {"SQL_CALCITE.equals(memberName)"})
    static public Object sqlCalcite(APIEntryPoint receiver, String memberName, Object[] arguments) {
      if(receiver.session.isDebugEnabled()) {
        System.out.println("Executing SQL with Calcite engine");
      }
      return execCalcite(receiver.session, (String) arguments[0]);
    }

    @Specialization(guards = "PREPARE.equals(memberName)")
    static public Object prepare(APIEntryPoint receiver, String memberName, Object[] arguments) {
      return new PreparedQuery(parse(arguments, receiver));
    }

    @Specialization(guards = "PREPARE_PARSED.equals(memberName)")
    static public Object prepareParsed(APIEntryPoint receiver, String memberName, Object[] arguments) {
      try {
        String sql = InteropLibrary.getFactory().getUncached().asString(arguments[0]);
        CalciteParser calciteParser = new CalciteParser(receiver.session, receiver.languageSpecificExtension);
        RelNode root = calciteParser.convertToRelNode(sql);
        return new ParsedQuery(root, calciteParser);
      } catch (Exception e) {
        return parseFailed(e, receiver.languageSpecificExtension);
      }
    }

    @Specialization(guards = "PREPARE_PARAMETRIC.equals(memberName)")
    static public Object prepareParametric(APIEntryPoint receiver, String memberName, Object[] arguments) {
      return createParametricQuery(arguments, receiver);
    }


    @Fallback
    static public Object sqlFallback(APIEntryPoint receiver, String memberName, Object[] arguments){
      System.out.println("not a member: " + memberName);
      return null;
    }

  }

  @CompilerDirectives.TruffleBoundary
  static private Object execCalcite(APISessionManagement session, String sql) {
    ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
    if(session.isDebugEnabled()) {
      explainCalcite(session.getConnection(), sql);
    }
    Thread.currentThread().setContextClassLoader(CalciteConnection.class.getClassLoader());
    try(Statement statement = session.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {
      return PolyglotArrayProxyResultSet.fromResultSet(resultSet);
    } catch (SQLException e) {
      CompilerDirectives.transferToInterpreter();
      e.printStackTrace();
      return "Calcite Query Failed" + e.getMessage();
    } finally {
      Thread.currentThread().setContextClassLoader(oldCl);
    }
  }

  static private void explainCalcite(CalciteConnection connection, String sql) {
    ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(CalciteConnection.class.getClassLoader());
    String q1 = "explain plan for " + sql;
    try(Statement statement = connection.createStatement();
        ResultSet r = statement.executeQuery(q1)) {
      System.out.println("Calcite plan>");
      while (r.next()) {
        System.out.println(r.getString(1));
      }
    } catch (SQLException e) {
      CompilerDirectives.transferToInterpreter();
      e.printStackTrace();
      System.out.println("Calcite Query Explain Failed" + e.getMessage());
    } finally {
      Thread.currentThread().setContextClassLoader(oldCl);
    }

  }
  public static Object createParametricQuery(Object[] arguments, APIEntryPoint receiver) {
    try {
      String sql = InteropLibrary.getFactory().getUncached().asString(arguments[0]);
      CalciteParser calciteParser = new CalciteParser(receiver.session, receiver.languageSpecificExtension);
      TruffleLinqRootNode root = calciteParser.parseSource(sql);
      return createParametricQuery(root);
    } catch (Exception e) {
      return parseFailed(e, receiver.languageSpecificExtension);
    }
  }

  public static Object createParametricQuery(TruffleLinqRootNode root) {
    List<RexDynamicParameterTruffleNode> unboundedParameters = NodeUtil.findAllNodeInstances(root, RexDynamicParameterTruffleNode.class);
    unboundedParameters.sort(Comparator.comparingInt(x -> x.index));
    CallTarget ct = Truffle.getRuntime().createCallTarget(root);
    DirectCallNode callNode = Truffle.getRuntime().createDirectCallNode(ct);
    return new ParametricPreparedQuery(callNode, unboundedParameters.toArray(new RexDynamicParameterTruffleNode[0]));
  }

  static DirectCallNode parse(Object[] arguments, APIEntryPoint receiver) {
    try {
      String sql = InteropLibrary.getFactory().getUncached().asString(arguments[0]);
      CalciteParser calciteParser = new CalciteParser(receiver.session, receiver.languageSpecificExtension);
      TruffleLinqRootNode root = calciteParser.parseSource(sql);
      CallTarget ct = Truffle.getRuntime().createCallTarget(root);
      return Truffle.getRuntime().createDirectCallNode(ct);
    } catch (Exception e) {
      return parseFailed(e, receiver.languageSpecificExtension);
    }
  }

  private static DirectCallNode parseFailed(Exception e, LanguageSpecificExtension languageSpecificExtension) {
    CompilerDirectives.transferToInterpreter();
    RootNode rootNode = new TruffleLinqRootNode(null) {
      @Override
      public Object execute(VirtualFrame frame) {
        return DynQStringValue.create("Parsing Failed " + e.getMessage());
      }
    };
    return Truffle.getRuntime().createDirectCallNode(Truffle.getRuntime().createCallTarget(rootNode));
  }

  @ExportLibrary(InteropLibrary.class)
  static class ExecSqlMember extends AbstractAPITruffleObject {

    static final HashMap<Object[], DirectCallNode> cache = new HashMap<>();

    @CompilerDirectives.TruffleBoundary
    static DirectCallNode get(Object[] arguments, APIEntryPoint apiEntryPoint) {
      return cache.computeIfAbsent(arguments, s -> parse(s, apiEntryPoint));
    }

    final APIEntryPoint apiEntryPoint;

    ExecSqlMember(APIEntryPoint entryPoint) {
      this.apiEntryPoint = entryPoint;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(ExecSqlMember receiver, Object[] arguments,
                                 @Cached(value = "get(arguments, receiver.apiEntryPoint)", uncached = "get(arguments, receiver.apiEntryPoint)") DirectCallNode callNode) {
      return callNode.call();
    }
  }


  @ExportLibrary(InteropLibrary.class)
  static class PreparedQuery extends AbstractAPITruffleObject {

    final DirectCallNode callNode;

    PreparedQuery(DirectCallNode callNode) {
      this.callNode = callNode;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(PreparedQuery receiver, Object[] arguments) {
      return receiver.callNode.call();
    }
  }

  @ExportLibrary(InteropLibrary.class)
  static class CreatePreparedQuery extends AbstractAPITruffleObject {

    final APIEntryPoint apiEntryPoint;

    CreatePreparedQuery(APIEntryPoint entryPoint) {
      this.apiEntryPoint = entryPoint;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(CreatePreparedQuery receiver, Object[] arguments) {
      return new PreparedQuery(parse(arguments, receiver.apiEntryPoint));
    }
  }

  @ExportLibrary(InteropLibrary.class)
  static class ParsedQuery extends AbstractAPITruffleObject {

    final RelNode root;
    final CalciteParser calciteParser;

    ParsedQuery(RelNode root, CalciteParser calciteParser) {
      this.root = root;
      this.calciteParser = calciteParser;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(ParsedQuery receiver, Object[] arguments) {
      RootNode rootNode = receiver.calciteParser.convertToTruffle(receiver.root);
      CallTarget ct = Truffle.getRuntime().createCallTarget(rootNode);
      return ct.call();
    }
  }

  @ExportLibrary(InteropLibrary.class)
  static class CreateParsedQuery extends AbstractAPITruffleObject {

    final APIEntryPoint apiEntryPoint;

    CreateParsedQuery(APIEntryPoint entryPoint) {
      this.apiEntryPoint = entryPoint;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(CreateParsedQuery receiver, Object[] arguments) {
      try {
        String sql = InteropLibrary.getFactory().getUncached().asString(arguments[0]);
        CalciteParser calciteParser = new CalciteParser(receiver.apiEntryPoint.session, receiver.apiEntryPoint.languageSpecificExtension);
        return new ParsedQuery(calciteParser.convertToRelNode(sql), calciteParser);
      } catch (Exception e) {
        return new PreparedQuery(parseFailed(e, receiver.apiEntryPoint.languageSpecificExtension));
      }
    }
  }

  @ExportLibrary(InteropLibrary.class)
  static class ParsedAstQuery extends AbstractAPITruffleObject {

    final TruffleLinqRootNode root;

    ParsedAstQuery(TruffleLinqRootNode root) {
      this.root = root;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(ParsedAstQuery receiver, Object[] arguments) {
      return Truffle.getRuntime().createCallTarget(receiver.root).call();
    }
  }

  @ExportLibrary(InteropLibrary.class)
  static class CreateParsedAstQuery extends AbstractAPITruffleObject {
    final APIEntryPoint apiEntryPoint;

    CreateParsedAstQuery(APIEntryPoint entryPoint) {
      this.apiEntryPoint = entryPoint;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(CreateParsedAstQuery receiver, Object[] arguments) {
      try {
        String sql = InteropLibrary.getFactory().getUncached().asString(arguments[0]);
        CalciteParser calciteParser = new CalciteParser(receiver.apiEntryPoint.session, receiver.apiEntryPoint.languageSpecificExtension);
        TruffleLinqRootNode root = calciteParser.parseSource(sql);
        return new ParsedAstQuery(root);
      } catch (Exception e) {
        return parseFailed(e, receiver.apiEntryPoint.languageSpecificExtension);
      }
    }
  }

  @ExportLibrary(InteropLibrary.class)
  static class CreateRunGC extends AbstractAPITruffleObject {

    final APISessionManagement session;

    CreateRunGC(APISessionManagement session) {
      this.session = session;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(CreateRunGC receiver, Object[] arguments) {
      return new RunGC(receiver.session);
    }
  }

  @ExportLibrary(InteropLibrary.class)
  static class RunGC extends AbstractAPITruffleObject {
    final APISessionManagement session;

    RunGC(APISessionManagement session) {
      this.session = session;
    }

    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public static Object execute(RunGC receiver, Object[] arguments) {
      if(receiver.session.isDebugEnabled()) {
        System.out.println("Try running GC");
        System.out.println("Free Memory before: " + Runtime.getRuntime().freeMemory());
        long start = System.currentTimeMillis();
        System.gc();
        System.out.println("GC Completed, took: " + (System.currentTimeMillis() - start));
        System.out.println("Free Memory after: " + Runtime.getRuntime().freeMemory());
      } else {
        System.gc();
      }
      return null;
    }
  }
}
