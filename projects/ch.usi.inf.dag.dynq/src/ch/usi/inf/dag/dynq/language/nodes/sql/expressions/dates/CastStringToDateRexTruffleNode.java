package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates;


import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;

import java.time.LocalDate;


public abstract class CastStringToDateRexTruffleNode extends LocalDateRexTruffleNode {


    @Specialization
    LocalDate pass(LocalDate input) {
        return input;
    }

    @Specialization
    LocalDate castString(String input) {
        return castStringImpl(input);
    }

    @CompilerDirectives.TruffleBoundary
    private LocalDate castStringImpl(String input) {
        return LocalDate.parse(input);
    }

}
