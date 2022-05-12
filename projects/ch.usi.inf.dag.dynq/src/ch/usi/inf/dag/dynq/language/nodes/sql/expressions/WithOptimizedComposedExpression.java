package ch.usi.inf.dag.dynq.language.nodes.sql.expressions;


import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;


public interface WithOptimizedComposedExpression {
    default Optional<RexTruffleNode> getOptimizedEquals(Object constant) {
        return Optional.empty();
    }
    default Optional<RexTruffleNode> getOptimizedNotEquals(Object constant) {
        return Optional.empty();
    }

    // Note: range is left-open right-closed, i.e., from <= x < to
    default Optional<RexTruffleNode> getOptimizedInDateRange(LocalDate from, LocalDate to) {
        return Optional.empty();
    }

    default Optional<RexTruffleNode> getOptimizedSubstring(int from, int len) {
        return Optional.empty();
    }
    default Optional<RexTruffleNode> getOptimizedInConstantStringSet(Set<String> elements) {
        return Optional.empty();
    }
    default Optional<RexTruffleNode> getOptimizedStringLike(String matcher) {
        return Optional.empty();
    }
}
