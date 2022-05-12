package ch.usi.inf.dag.dynq.runtime.objects.managed_tables.memory;

import com.google.common.collect.ImmutableList;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.schema.impl.AbstractTable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PolyglotInputArrayTable extends AbstractTable {

  static final InteropLibrary INTERPOP_UNCACHED = InteropLibrary.getFactory().getUncached();
  private final Object input;
  private final long length;
  private final int numFields;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private final String[] fieldNames;
  private final List<String> fieldNamesList;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private final RelProtoDataType[] fieldTypes;
  private final List<RelProtoDataType> fieldTypesList;

  public PolyglotInputArrayTable(Object input, Map<String, RelProtoDataType> schemaMap) {
    this.input = input;
    this.numFields = schemaMap.size();
    fieldNames = new String[numFields];
    fieldTypes = new RelProtoDataType[numFields];
    int i = 0;
    for(Map.Entry<String, RelProtoDataType> entry : schemaMap.entrySet()) {
      fieldNames[i] = entry.getKey();
      fieldTypes[i] = entry.getValue();
      i++;
    }
    fieldNamesList = Arrays.asList(fieldNames);
    fieldTypesList = Arrays.asList(fieldTypes);
    try {
      this.length = INTERPOP_UNCACHED.getArraySize(input);
    } catch (UnsupportedMessageException e) {
      CompilerDirectives.transferToInterpreter();
      throw new RuntimeException(e);
    }
  }

  public Object getInput() {
    return input;
  }

  public long getLength() {
    return length;
  }

  public Statistic getStatistic() {
    return Statistics.of((double)length, ImmutableList.of());
  }

  public int getNumFields() {
    return numFields;
  }

  public List<RelProtoDataType> getFieldTypesList() {
    return fieldTypesList;
  }

  public String getNameAtIndex(int index) {
    return fieldNames[index];
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    List<RelDataType> fieldTypesConverted = fieldTypesList.stream().map(p -> p.apply(typeFactory)).collect(Collectors.toList());
    return typeFactory.createStructType(fieldTypesConverted, fieldNamesList);
  }

  public Type getElementType() {
    return Object[].class;
  }

}
