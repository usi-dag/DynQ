package ch.usi.inf.dag.dynq.structures;

import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.CompilerDirectives;

import java.util.HashMap;

public final class MarkOnGetMap extends HashMap<Object, FinalMarkedArrayList> {

    public MarkOnGetMap(int initialCapacity) {
        super(initialCapacity);
    }

    public MarkOnGetMap() {
    }

    private static final long serialVersionUID = -4335260480915003507L;

    public void insert(Object key, Object row) {
        getOrCompute(key).add(row);
    }

    public FinalMarkedArrayList getAndMark(Object key) {
        FinalMarkedArrayList value = TruffleBoundaryUtils.hashMapGet(this, key);
        if(value != null) {
            value.marked = true;
            return value;
        }
        return null;
    }

    @CompilerDirectives.TruffleBoundary
    private FinalMarkedArrayList getOrCompute(Object key) {
        return computeIfAbsent(key, x -> new FinalMarkedArrayList());
    }

}