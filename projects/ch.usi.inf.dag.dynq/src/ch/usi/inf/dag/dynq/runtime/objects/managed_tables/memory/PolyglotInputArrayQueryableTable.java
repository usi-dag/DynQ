package ch.usi.inf.dag.dynq.runtime.objects.managed_tables.memory;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.QueryableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTableQueryable;


@SuppressWarnings("unchecked")
public class PolyglotInputArrayQueryableTable extends PolyglotInputArrayTable
        implements QueryableTable, TranslatableTable {
  // note: used only by Calcite engine

  final Function<? super Object, ?>[] fieldConverters;

  public PolyglotInputArrayQueryableTable(Object input, Map<String, RelProtoDataType> schemaMap) {
    super(input, schemaMap);

    this.fieldConverters = fillConverters(schemaMap, getFieldTypesList());
  }

  @Override
  public Queryable<Object[]> asQueryable(QueryProvider queryProvider, SchemaPlus schemaPlus, String tableName) {
    return new AbstractTableQueryable<Object[]>(queryProvider, schemaPlus, this, tableName) {
      public Enumerator<Object[]> enumerator() {
        return new Enumerator<Object[]>() {
          int currentIdx = -1;

          @Override
          public Object[] current() {
            try {
              return value(INTERPOP_UNCACHED.readArrayElement(getInput(), currentIdx));
            } catch (UnsupportedMessageException | InvalidArrayIndexException | UnknownIdentifierException e) {
              // TODO should iterate to next element
              CompilerDirectives.transferToInterpreter();
              throw new RuntimeException(e);
            }
          }

          @Override
          public boolean moveNext() {
            return ++currentIdx < getLength();
          }

          @Override
          public void reset() {
            currentIdx = -1;
          }

          @Override
          public void close() {}
        };
      }

      @ExplodeLoop
      private Object[] value(Object current) throws UnknownIdentifierException, UnsupportedMessageException {
        Object[] result = new Object[getNumFields()];
        for (int i = 0; i < result.length; i++) {
          String fieldName = getNameAtIndex(i);
          Object member = INTERPOP_UNCACHED.readMember(current, fieldName);
          try {
            result[i] = fieldConverters[i].apply(member);
          } catch (RuntimeException e) {
            System.out.println("Exception with field converter");
            System.out.println("Table: " + PolyglotInputArrayQueryableTable.this);
            System.out.println("Field at index: " + i);
            System.out.println("which is: " + PolyglotInputArrayQueryableTable.super.getNameAtIndex(i));
            throw e;
          }
        }
        return result;
      }
    };
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Expression getExpression(SchemaPlus schema, String tableName, Class clazz) {
    return Schemas.tableExpression(schema, getElementType(), tableName, clazz);
  }

  @Override
  public RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
    return new LogicalTableScan(
            context.getCluster(),
            context.getCluster().traitSet(),
            context.getTableHints(),
            relOptTable);
  }



  static Object asDate(Object o) {
    try {
      LocalDate d = INTERPOP_UNCACHED.asDate(o);
      return Date.valueOf(d);
    }
    catch (Exception e) { throw new RuntimeException(e); }
  }
  static Object asInt(Object o) {
    try { return INTERPOP_UNCACHED.asInt(o); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
  static Object asDouble(Object o) {
    try { return INTERPOP_UNCACHED.asDouble(o); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
  static Object asLong(Object o) {
    try { return INTERPOP_UNCACHED.asLong(o); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
  static Object asString(Object o) {
    try { return INTERPOP_UNCACHED.asString(o); }
    catch (Exception e) { throw new RuntimeException(e); }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static Function<? super Object, ?>[] fillConverters(Map<String, RelProtoDataType> schemaMap, List<RelProtoDataType> fieldTypesList) {
    JavaTypeFactoryImpl factory = new JavaTypeFactoryImpl();
    Function<? super Object, ?>[] fieldConverters = new Function[schemaMap.size()];
    for (int i = 0; i < fieldConverters.length; i++) {
      switch (fieldTypesList.get(i).apply(factory).getSqlTypeName()) {
        case DATE:
          fieldConverters[i] = PolyglotInputArrayQueryableTable::asDate;
          break;
        case INTEGER:
          fieldConverters[i] = PolyglotInputArrayQueryableTable::asInt;
          break;
        case BIGINT:
          fieldConverters[i] = PolyglotInputArrayQueryableTable::asLong;
          break;
        case CHAR:
        case VARCHAR:
          fieldConverters[i] = PolyglotInputArrayQueryableTable::asString;
          break;
        case DOUBLE:
          fieldConverters[i] = PolyglotInputArrayQueryableTable::asDouble;
          break;
        default:
          throw new RuntimeException("Unexpected type for polyglot conversion: " + fieldTypesList.get(i).apply(factory).getSqlTypeName());
      }
    }
    return fieldConverters;
  }


}
