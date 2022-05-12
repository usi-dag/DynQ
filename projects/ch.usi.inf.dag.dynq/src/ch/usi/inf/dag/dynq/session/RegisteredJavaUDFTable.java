package ch.usi.inf.dag.dynq.session;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

public class RegisteredJavaUDFTable {

  private final HashMap<String, Supplier<Object>> udfTable = new HashMap<>();

  public void registerUDF(String name, Supplier<Object> func) {
    udfTable.put(name, func);
  }

  public Set<String> names() {
    return udfTable.keySet();
  }

}
