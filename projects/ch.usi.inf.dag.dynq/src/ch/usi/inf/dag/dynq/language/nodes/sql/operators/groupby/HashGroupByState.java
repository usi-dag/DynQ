package ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby;

import java.util.HashMap;

public final class HashGroupByState extends HashMap<Object, Object[]> {
    private static final long serialVersionUID = -5894081955888371417L;

    public HashGroupByState(int initialCapacity) {
        super(initialCapacity);
    }

    public HashGroupByState() {
    }
}
