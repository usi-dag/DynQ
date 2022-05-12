package ch.usi.inf.dag.dynq.structures;

import java.util.Iterator;


public final class AppendableLinkedList implements Iterable<Object> {

    private AppendableLinkedListNode first;
    private AppendableLinkedListNode last;

    public void append(Object element) {
        AppendableLinkedListNode newLastNode = new AppendableLinkedListNode(element);
        AppendableLinkedListNode l = last;
        if(l != null) {
            l.next = newLastNode;
            last = newLastNode;
        } else {
            first = last = newLastNode;
        }
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {
            AppendableLinkedListNode current = first;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Object next() {
                AppendableLinkedListNode c = current;
                Object result = c.element;
                current = c.next;
                return result;
            }
        };
    }

    static final class AppendableLinkedListNode {
        final Object element;
        AppendableLinkedListNode next;

        AppendableLinkedListNode(Object element) {
            this.element = element;
        }
    }

}
