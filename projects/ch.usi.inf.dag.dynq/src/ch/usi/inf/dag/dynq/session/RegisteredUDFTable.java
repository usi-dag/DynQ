package ch.usi.inf.dag.dynq.session;

import com.oracle.truffle.api.CompilerDirectives;

import java.util.HashMap;
import java.util.Set;

public class RegisteredUDFTable {

  private final HashMap<String, Object> udfTable = new HashMap<>();

  public void registerUDF(String name, Object func) {
    udfTable.put(name, func);
  }

  @CompilerDirectives.TruffleBoundary
  public Object getUDF(String name) {
    return udfTable.get(name);
  }

  public Set<String> names() {
    return udfTable.keySet();
  }

}
