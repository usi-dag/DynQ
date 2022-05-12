package ch.usi.inf.dag.dynq.language.nodes.sql.volcano;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.finalizers.AbstractFillListDataCentricConsumer;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.finalizers.DataCentricFinalizerUtils;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.runtime.objects.resultsets.PolyglotArrayProxyResultSet;
import ch.usi.inf.dag.dynq.runtime.objects.resultsets.ProxyRow;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.object.DynamicObject;

import java.util.List;

import static ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.finalizers.DataCentricFinalizerUtils.getListToPolyglotFinalizerByMaterializerClass;

public final class VolcanoIteratorPolyglotFinalizerNode extends VolcanoIteratorNode {

    @Child
    private VolcanoIteratorNode child;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final String[] columnNames;

    public VolcanoIteratorPolyglotFinalizerNode(VolcanoIteratorNode child, String[] columnNames) {
        this.child = child;
        this.columnNames = columnNames;
    }



    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        if(consumer != null) {
            throw new RuntimeException("VolcanoIteratorPolyglotFinalizerNode does not accept consumers");
        }
        AbstractFillListDataCentricConsumer fillListConsumerNode = DataCentricFinalizerUtils.getFillListFinalizer();

        if(columnNames.length == 1 && "**".equals(columnNames[0])) { // project dynamic star
            return new TruffleLinqExecutableFinalizerDynamicObjectArrayNode(child.acceptConsumer(fillListConsumerNode), child.getMaterializerClass());
        }

        return new TruffleLinqExecutableFinalizerNode(child.acceptConsumer(fillListConsumerNode), columnNames);

    }

    static final class TruffleLinqExecutableFinalizerNode extends TruffleLinqExecutableNode {
        @Child TruffleLinqExecutableNode linqExecutableNode;

        @CompilerDirectives.CompilationFinal(dimensions = 1)
        final String[] columnNames;

        public TruffleLinqExecutableFinalizerNode(TruffleLinqExecutableNode linqExecutableNode, String[] columnNames) {
            this.linqExecutableNode = linqExecutableNode;
            this.columnNames = columnNames;
        }

        @SuppressWarnings("unchecked") // TODO
        public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            Object result = linqExecutableNode.execute(frame);
            if(result instanceof Object[][]) {
                return PolyglotArrayProxyResultSet.fromArray((Object[][]) result, columnNames);
            } else if(result instanceof List) {
                return PolyglotArrayProxyResultSet.fromList((List<Object[]>) result, columnNames);
            } else if(result instanceof DynamicObject) {
                return result;
            } if(result instanceof Object[]) {
                ProxyRow.ProxyRowBuilder proxyBuilder = ProxyRow.builder(columnNames);
                return proxyBuilder.build((Object[]) result);
            }
            throw new RuntimeException("Unexpected type for materializer child: " + result.getClass());
        }
    }

    public static final class TruffleLinqExecutableFinalizerDynamicObjectArrayNode extends TruffleLinqExecutableNode {
        @Child
        TruffleLinqExecutableNode linqExecutableNode;
        @Child
        DataCentricFinalizerUtils.FinalizerExecutorNode finalizerExecutorNode;

        @CompilerDirectives.CompilationFinal
        final Class<?> materializerClass;

        public TruffleLinqExecutableFinalizerDynamicObjectArrayNode(TruffleLinqExecutableNode linqExecutableNode,
                                                                    Class<?> materializerClass) {
            this.linqExecutableNode = linqExecutableNode;
            this.materializerClass = materializerClass;
            this.finalizerExecutorNode = getListToPolyglotFinalizerByMaterializerClass(materializerClass);
        }

        public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            return finalizerExecutorNode.execute(linqExecutableNode.execute(frame));
        }

    }
}
