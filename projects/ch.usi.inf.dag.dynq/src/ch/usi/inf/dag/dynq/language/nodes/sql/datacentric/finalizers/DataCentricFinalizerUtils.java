package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.finalizers;

import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import ch.usi.inf.dag.dynq.runtime.objects.resultsets.PolyglotArrayProxyResultSet;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

import java.util.ArrayList;
import java.util.List;

public class DataCentricFinalizerUtils {

    public static AbstractFillListDataCentricConsumer getFillListFinalizer() {
        return new FillListDataCentricConsumerNode();
    }

    public static FinalizerExecutorNode getListToPolyglotFinalizerByMaterializerClass(Class<?> materializerClass) {
        if (materializerClass == null || materializerClass.isAssignableFrom(List.class)) {
            return getListToPolyglotFinalizer();
        } else if (materializerClass.isAssignableFrom(DynamicObject.class)) {
            return new FinalizerDynamicObjectExecutorNode();
        } else if (materializerClass.isAssignableFrom(Object[].class)) {
            return new FinalizerArrayExecutorNode();
        } else {
            throw new RuntimeException("Cannot create a polyglot query finalizer for class " + materializerClass);
        }
    }

    public static FinalizerExecutorNode getListToPolyglotFinalizer() {
        return new FinalizerArrayListExecutorNode();
    }

    public static abstract class FinalizerExecutorNode extends Node implements Explainable {
        public abstract Object execute(Object result);
    }

    static final class FinalizerArrayListExecutorNode extends FinalizerExecutorNode {
        @Override
        public Object execute(Object result) {
            return PolyglotArrayProxyResultSet.fromDynamicObjectArrayList((ArrayList) result);
        }
    }
    static final class FinalizerArrayExecutorNode extends FinalizerExecutorNode {
        @Override
        public Object execute(Object result) {
            return PolyglotArrayProxyResultSet.fromDynamicObjectArray((Object[]) result);
        }
    }
    static final class FinalizerDynamicObjectExecutorNode extends FinalizerExecutorNode {
        @Override
        public Object execute(Object result) {
            return result;
        }
    }
}

