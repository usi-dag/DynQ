package ch.usi.inf.dag.dynq.structures;

import com.oracle.truffle.api.CompilerDirectives;

import java.util.HashMap;


public final class FinalListMap extends HashMap<Object, AppendableLinkedList> {

    public FinalListMap(int initialCapacity) {
        super(initialCapacity);
    }

    public FinalListMap() {
        super();
    }

    private static final long serialVersionUID = -9050151399844529283L;

    public void insert(Object key, Object row) {
        getOrCompute(key).append(row);
    }

    @CompilerDirectives.TruffleBoundary
    private AppendableLinkedList getOrCompute(Object key) {
        return computeIfAbsent(key, x -> new AppendableLinkedList());
    }

}
