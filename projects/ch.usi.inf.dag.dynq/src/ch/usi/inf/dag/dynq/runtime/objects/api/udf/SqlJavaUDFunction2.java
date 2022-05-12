package ch.usi.inf.dag.dynq.runtime.objects.api.udf;

@FunctionalInterface
public interface SqlJavaUDFunction2 extends SqlJavaUDFunction {
  Object execute(Object fst, Object snd);
}
