
package ch.usi.inf.dag.dynq.structures.truffle_pq;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.RexComparatorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public class PqPollNode extends Node implements Explainable {

    @Child
    private RexComparatorNode comparatorNode;

    public PqPollNode(RexComparatorNode comparatorNode) {
        this.comparatorNode = comparatorNode;
    }

    public Object poll(VirtualFrame frame, PriorityQueue<?> queue) throws InteropException, FrameSlotTypeException {
        if (queue.size == 0)
            return null;
        int s = --queue.size;
        queue.modCount++;
        Object result = queue.queue[0];
        Object x = queue.queue[s];
        queue.queue[s] = null;
        if (s != 0)
            siftDownUsingComparator(frame, queue, 0, x);
        return result;
    }

    private void siftDownUsingComparator(VirtualFrame frame, PriorityQueue<?> queue, int k, Object x) throws InteropException, FrameSlotTypeException {
        int half = queue.size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue.queue[child];
            int right = child + 1;
            if (right < queue.size && comparatorNode.compare(frame, c, queue.queue[right]) > 0)
                c = queue.queue[child = right];
            if (comparatorNode.compare(frame, x, c) <= 0)
                break;
            queue.queue[k] = c;
            k = child;
        }
        queue.queue[k] = x;
    }

}
