package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import com.oracle.truffle.api.frame.VirtualFrame;


public final class AggregateCountNode extends AggregateFunctionNode {

    @Override
    public Counter getInitialState() {
        return new Counter();
    }

    @Override
    public Counter execute(VirtualFrame frame, Object state, Object input) {
        Counter counter = (Counter) state;
        counter.value++;
        return counter;
    }

    @Override
    public boolean needsFinalization() {
        return true;
    }

    @Override
    public Long finalize(VirtualFrame frame, Object state) {
        return ((Counter)state).value;
    }

    static final class Counter {
        long value = 0;
    }

}
