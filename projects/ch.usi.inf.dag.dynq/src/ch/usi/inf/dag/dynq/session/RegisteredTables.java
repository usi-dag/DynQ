package ch.usi.inf.dag.dynq.session;

import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.ResettableDynamicRecordTable;
import com.oracle.truffle.api.CompilerDirectives;
import org.apache.calcite.schema.Table;

import java.util.HashMap;

public class RegisteredTables {

  private final HashMap<String, Table> tables = new HashMap<>();

  public void registerTable(String name, Table table) {
    tables.put(name, table);
  }

  @CompilerDirectives.TruffleBoundary
  public Table getTable(String name) {
    return tables.get(name);
  }

  public void resetDynamicTables() {
    tables.values().forEach(t -> {
      if(t instanceof ResettableDynamicRecordTable) {
        ((ResettableDynamicRecordTable) t).resetDataType();
      }
    });
  }

}

