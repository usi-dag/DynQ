package ch.usi.inf.dag.dynq.language.nodes.utils;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import com.oracle.truffle.api.nodes.Node;

public interface Explainable {

    default String explain() {
        String simpleName = getClass().getSimpleName();
        return (!simpleName.isEmpty()) ? simpleName : getClass().getName();
    }

    static String explain(Node node) {
        StringBuilder sb = new StringBuilder();
        sb.append("Root: ").append(node.getClass().getSimpleName()).append("\n");
        for(Node child : node.getChildren()) {
            sb.append(explain(child, 0)).append("\n");
        }
        return sb.toString();
    }

    static String explain(Node node, int level) {
        StringBuilder sb = new StringBuilder();
        if(node instanceof Explainable) {
            if(node instanceof VolcanoIteratorNode) sb.append("(V) ");
            else if(node instanceof TruffleLinqExecutableNode) sb.append("(E) ");
            else if(node instanceof RexTruffleNode) sb.append("(R) ");
            else if(node instanceof DataCentricConsumerNode) sb.append("(C) ");
            else sb.append("(?)");
            Explainable explainable = (Explainable) node;
            String explanation = explainable.explain();
            if(explanation != null && !explanation.isEmpty()) {
                StringBuilder prefix = new StringBuilder();
                for (int i = 0; i < level; i++) prefix.append("\t");
                for(String line : explanation.split("\n")) {
                    if(!line.isEmpty())
                        sb.append(prefix).append(line);
                    sb.append('\n');
                }
            } else {
                sb.append('\n');
            }
        }
        for(Node child : node.getChildren()) {
            sb.append(explain(child, level + 1));
        }
        return sb.toString();
    }
}
