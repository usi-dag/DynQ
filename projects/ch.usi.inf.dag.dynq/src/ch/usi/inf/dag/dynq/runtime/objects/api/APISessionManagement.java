package ch.usi.inf.dag.dynq.runtime.objects.api;

import ch.usi.inf.dag.dynq.runtime.objects.data.DynQNullValue;
import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.Tpch;
import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.memory.PolyglotInputArrayQueryableTable;
import ch.usi.inf.dag.dynq.session.RegisteredJavaUDFTable;
import ch.usi.inf.dag.dynq.session.RegisteredTables;
import ch.usi.inf.dag.dynq.session.RegisteredUDFTable;
import ch.usi.inf.dag.dynq.runtime.utils.InteropUtils;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.memory.DynamicPolyglotInputArrayTable;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.schema.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;


@ExportLibrary(InteropLibrary.class)
public class APISessionManagement extends AbstractAPITruffleObject {


  private static final String REGISTER_UDF = "registerUDF";
  private static final String REGISTER_TABLE = "registerTable";

  private final RegisteredUDFTable registeredUDFTable;
  private final RegisteredJavaUDFTable registeredJavaUDFTable;
  private final RegisteredTables registeredTables;
  private final CalciteConnection calciteConnection;

  private final boolean debugEnabled = "true".equals(System.getenv("DYNQ_DEBUG"));


  private static String[] expandExtraMembers(String... extraMembers) {
    String[] members = new String[extraMembers.length + 2];
    members[0] = REGISTER_UDF;
    members[1] = REGISTER_TABLE;
    System.arraycopy(extraMembers, 0, members, 2, extraMembers.length);
    return members;
  }


  public APISessionManagement(String... extraMembers) {
    super(expandExtraMembers(extraMembers));
    registeredUDFTable = new RegisteredUDFTable();
    registeredJavaUDFTable = new RegisteredJavaUDFTable();
    registeredTables = new RegisteredTables();

    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    info.setProperty("timeZone", "GMT"); // if not, Calcite will rewrite dates
    // TODO fix Class Loader issues
    ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(CalciteConnection.class.getClassLoader());
      Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
      calciteConnection = connection.unwrap(CalciteConnection.class);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(oldCl);
    }
  }

  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  public RegisteredUDFTable getRegisteredUDFTable() {
    return registeredUDFTable;
  }
  public RegisteredJavaUDFTable getRegisteredJavaUDFTable() {
    return registeredJavaUDFTable;
  }

  public RegisteredTables getRegisteredTables() {
    return registeredTables;
  }

  public CalciteConnection getConnection() {
    return calciteConnection;
  }


  @Override
  @ExportMessage
  public Object readMember(String name) throws UnknownIdentifierException {
    if(REGISTER_TABLE.equals(name)) {
      return new RegisterTableMember();
    }
    throw UnknownIdentifierException.create(name);
  }

  @Override
  @ExportMessage
  public boolean isMemberInvocable(String member) {
    return REGISTER_UDF.equals(member);
  }

  @Override
  @ExportMessage
  public Object invokeMember(String member, Object... arguments) throws UnknownIdentifierException, UnsupportedMessageException, ArityException, UnsupportedTypeException {
    if(REGISTER_UDF.equals(member)) {
      InteropUtils.checkArgumentLength(arguments, 2);
      String name = InteropUtils.expectString(arguments[0], "expected function name");
      Object func = InteropUtils.expectExecutable(arguments[1], "expected executable function as UDF");
      registeredUDFTable.registerUDF(name, func);
      return func;
    } else if(REGISTER_TABLE.equals(member)) {
      checkRegisterTable(arguments);
      return DynQNullValue.INSTANCE;
    } else {
      throw UnknownIdentifierException.create(member);
    }
  }

  protected void checkRegisterTable(Object... arguments) throws ArityException, UnsupportedTypeException {
    // Expect name, table (array), schema, options?
    InteropUtils.checkArgumentMinMaxLength(arguments, 3, 4);
    String name = InteropUtils.expectString(arguments[0], "expected function name");
    Object array = arguments[1];
    Object schema = arguments[2];
    Object options = arguments.length == 4 ? arguments[3] : null;
    try {
      registerTable(name, array, schema, options);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void registerTable(String name, Object input, Object type, Object options) throws Exception {
    Table table;
    if(InteropUtils.INTEROP.isString(type)) {
      String tableType = InteropUtils.expectString(type);
      if("dynamic".equals(tableType)) {
        table = getDynamicTable(name, input);
      } else if(tableType.startsWith("schema")) {
        table = getSchemaTable(name, input, type);
      } else {
        throw new RuntimeException("Unknown table type: " + tableType);
      }
    } else {
      throw new RuntimeException("Unknown table type: " + type);
    }

    SchemaPlus rootSchema = calciteConnection.getRootSchema();
    rootSchema.add(name, table);
    registeredTables.registerTable(name, table);
  }

  protected Table getDynamicTable(String name, Object input) {
    return new DynamicPolyglotInputArrayTable(input);
  }

  protected Table getSchemaTable(String name, Object input, Object schema) {
    // TODO parse schema -- currently only TPC-H (hardcoded) schema
    return new PolyglotInputArrayQueryableTable(input, Tpch.TpchTableEnum.valueOf(name.toUpperCase()).getSchemaMap());
  }

  @ExportLibrary(InteropLibrary.class)
  class RegisterTableMember extends AbstractAPITruffleObject  {
    @ExportMessage
    public boolean isExecutable() {
      return true;
    }

    @ExportMessage
    public Object execute(Object... arguments) {
      try {
        checkRegisterTable(arguments);
        return "Registered";
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }


  public List<RelOptRule> getExtraPlannerRules() {
    return Collections.emptyList();
  }
}


