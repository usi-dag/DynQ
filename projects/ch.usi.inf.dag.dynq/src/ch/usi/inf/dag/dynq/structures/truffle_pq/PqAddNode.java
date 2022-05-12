
package ch.usi.inf.dag.dynq.structures.truffle_pq;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.RexComparatorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public class PqAddNode extends Node implements Explainable {

    @Child
    private RexComparatorNode comparatorNode;

    public PqAddNode(RexComparatorNode comparatorNode) {
        this.comparatorNode = comparatorNode;
    }

    public void add(VirtualFrame frame, PriorityQueue<?> queue, Object element) throws InteropException, FrameSlotTypeException {
        queue.modCount++;
        int i = queue.size;
        if (i >= queue.queue.length)
            queue.grow(i + 1);
        queue.size = i + 1;
        if (i == 0)
            queue.queue[0] = element;
        else
            siftUpUsingComparator(frame, queue, i, element);
    }

    private void siftUpUsingComparator(VirtualFrame frame, PriorityQueue<?> queue, int k, Object x) throws InteropException, FrameSlotTypeException {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue.queue[parent];
            if (comparatorNode.compare(frame, x, e) >= 0)
                break;
            queue.queue[k] = e;
            k = parent;
        }
        queue.queue[k] = x;
    }

}
