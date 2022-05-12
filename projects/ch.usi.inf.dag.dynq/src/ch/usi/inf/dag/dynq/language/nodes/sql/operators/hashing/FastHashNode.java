package ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;


public abstract class FastHashNode extends Node {
        public abstract int execute(Object o);

        @Specialization
        int executeChar(char c) {
            return c;
        }

        @Specialization
        int executeInt(int i) {
            return i;
        }

        @Specialization(guards = "o.length() == 1")
        int executeChar(String o) {
            return o.charAt(0);
        }

        @Specialization(guards = "o != null")
        int executeGeneric(Object o) {
            return objHash(o);
        }

        @Specialization(guards = "o == null")
        int executeNull(Object o) {
            return 0;
        }

        @CompilerDirectives.TruffleBoundary(allowInlining = true)
        int objHash(Object o) {
            return o.hashCode();
        }

    }