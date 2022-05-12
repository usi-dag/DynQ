package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;

import java.time.LocalDate;



public abstract class LocalDateFromInteropRexTruffleNode extends LocalDateRexTruffleNode  {

    @CompilerDirectives.CompilationFinal
    private final String member;

    public LocalDateFromInteropRexTruffleNode(String member) {
        this.member = member;
    }

    @Specialization
    public LocalDate executeCached(Object input,
                                   @CachedLibrary(limit = "1") InteropLibrary interopRead,
                                   @CachedLibrary(limit = "1") InteropLibrary interopAsDate) throws InteropException {
        if(input == null) {
            return null;
        }
        return interopAsDate.asDate(interopRead.readMember(input, member));
    }


    @Override
    public String explain() {
        return super.explain() + "(" + member + ")" ;
    }

}
