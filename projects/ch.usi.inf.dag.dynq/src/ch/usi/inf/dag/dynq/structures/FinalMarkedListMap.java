package ch.usi.inf.dag.dynq.structures;

import com.oracle.truffle.api.CompilerDirectives;

import java.util.HashMap;


public final class FinalMarkedListMap extends HashMap<Object, FinalMarkedArrayList> {

    public FinalMarkedListMap(int initialCapacity) {
        super(initialCapacity);
    }

    public FinalMarkedListMap() {
    }

    private static final long serialVersionUID = -3658155729356790556L;

    public void insert(Object key, Object row) {
        getOrCompute(key).add(row);
    }

    @CompilerDirectives.TruffleBoundary
    private FinalMarkedArrayList getOrCompute(Object key) {
        return computeIfAbsent(key, x -> new FinalMarkedArrayList());
    }

}
