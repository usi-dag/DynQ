package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.dates;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers.IntegerRexTruffleNode;
import ch.usi.inf.dag.dynq_js.runtime.types.AfterBurnerDateWrapper;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;
import ch.usi.inf.dag.dynq_js.runtime.types.JSDateWrapper;
import ch.usi.inf.dag.dynq_js.runtime.types.LocalDateWrapper;

import java.time.LocalDate;


public abstract class ExtractFromDateRexTruffleNode extends IntegerRexTruffleNode {

    @Child
    RexTruffleNode dateGetter;

    ExtractFromDateRexTruffleNode(RexTruffleNode dateGetter) {
        this.dateGetter = dateGetter;
    }

    @Override
    public LocalDate runDate(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return dateGetter.runDate(frame, input);
    }

    // Year Extractor
    public static final class Year extends ExtractFromDateRexTruffleNode {
        @Child
        YearExtractorNode extractor;

        public Year(RexTruffleNode dateGetter) {
            super(dateGetter);
            extractor = ExtractFromDateRexTruffleNodeFactory.YearExtractorNodeGen.create();
        }

        @Override
        public Integer executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
            return extractor.execute(dateGetter.executeWith(frame, input));
        }
    }

    public static abstract class YearExtractorNode extends Node {

        abstract int execute(Object input);

        @Specialization
        public int executeLocalDate(LocalDate date) {
            return date.getYear();
        }

        @Specialization
        public int executeLocalDateWrapper(LocalDateWrapper date) {
            return date.getDate().getYear();
        }

        @Specialization
        public int executeJSDateWrapper(JSDateWrapper date) {
            // TODO rewrite this using only calculation, i.e., no LocalDate (maybe check GraalJS lib)
            return date.asLocalDate().getYear();
        }

        @Specialization
        public int executeAfterBurnerDate(AfterBurnerDateWrapper date) {
            // TODO rewrite this using only calculation, i.e., no LocalDate (maybe check AfterBurner lib)
            return date.asLocalDate().getYear();
        }
    }

    // Month Extractor
    public static final class Month extends ExtractFromDateRexTruffleNode {
        @Child
        MonthExtractorNode extractor;

        public Month(RexTruffleNode dateGetter) {
            super(dateGetter);
            extractor = ExtractFromDateRexTruffleNodeFactory.MonthExtractorNodeGen.create();
        }

        @Override
        public Integer executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
            return extractor.execute(dateGetter.executeWith(frame, input));
        }
    }

    public static abstract class MonthExtractorNode extends Node {

        abstract int execute(Object input);

        @Specialization
        public int executeLocalDate(LocalDate date) {
            return date.getMonthValue();
        }

        @Specialization
        public int executeLocalDateWrapper(LocalDateWrapper date) {
            return date.getDate().getMonthValue();
        }

        @Specialization
        public int executeJSDateWrapper(JSDateWrapper date) {
            // TODO rewrite this using only calculation, i.e., no LocalDate (maybe check GraalJS lib)
            return date.asLocalDate().getMonthValue();
        }

        @Specialization
        public int executeAfterBurnerDate(AfterBurnerDateWrapper date) {
            // TODO rewrite this using only calculation, i.e., no LocalDate (maybe check AfterBurner lib)
            return date.asLocalDate().getMonthValue();
        }
    }

}
