package ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing;

import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;

public final class ArrayBoxHashNode extends Node {

    @Children
    FastHashNode[] hashNodes;

    public ArrayBoxHashNode(int len) {
        this.hashNodes = new FastHashNode[len];
        for (int i = 0; i < len; i++) {
            this.hashNodes[i] = FastHashNodeGen.create();
        }
    }

    @ExplodeLoop
    public int hash(Object[] data) {
        int result = 1;
        for (int i = 0; i < hashNodes.length; i++) {
            Object element = data[i];
            result = 31 * result + hashNodes[i].execute(element);
        }
        return result;
    }

}
