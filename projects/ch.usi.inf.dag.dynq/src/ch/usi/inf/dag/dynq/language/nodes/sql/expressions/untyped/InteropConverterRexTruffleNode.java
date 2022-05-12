package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;

import java.time.LocalDate;


abstract class InteropConverterRexTruffleNode extends RexTruffleNode {

    // Primitives

    @Specialization
    public boolean getPrimitiveBool(boolean result) {
        return result;
    }

    @Specialization
    public int getPrimitiveInt(int result) {
        return result;
    }

    @Specialization
    public long getPrimitiveLong(long result) {
        return result;
    }

    @Specialization
    public double getPrimitiveDouble(double result) {
        return result;
    }


    // Date
    @Specialization(guards = "interop.isDate(value)")
    public LocalDate getLocalDate(Object value,
                                  @CachedLibrary(limit = "1") InteropLibrary interop) throws InteropException {
        return interop.asDate(value);
    }

    // String
    @Specialization(guards = "interop.isString(value)")
    public String getString(Object value,
                            @CachedLibrary(limit = "1") InteropLibrary interop) throws InteropException {
        return interop.asString(value);
    }

    // Boolean
    @Specialization(guards = "interop.isBoolean(value)")
    public boolean getBool(Object value,
                           @CachedLibrary(limit = "1") InteropLibrary interop) throws InteropException {
        return interop.asBoolean(value);
    }

    // Int
    @Specialization(guards = "interop.fitsInInt(value)")
    public int getInt(Object value,
                      @CachedLibrary(limit = "1") InteropLibrary interop) throws InteropException {
        return interop.asInt(value);
    }


    // Long
    @Specialization(guards = "interop.fitsInLong(value)")
    public long getLong(Object value,
                        @CachedLibrary(limit = "1") InteropLibrary interop) throws InteropException {
        return interop.asLong(value);
    }

    // Double
    @Specialization(guards = "interop.fitsInDouble(value)")
    public double getDouble(Object value,
                            @CachedLibrary(limit = "1") InteropLibrary interop) throws InteropException {
        return interop.asDouble(value);
    }


    @Specialization
    public DynamicObject passIt(DynamicObject value) {
        return value;
    }


    @Override
    public String explain() {
        return "";
    }
}
