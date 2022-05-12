package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


public interface SingleValueObjectComparatorNode {
    int execute(Object fst, Object snd);
}
