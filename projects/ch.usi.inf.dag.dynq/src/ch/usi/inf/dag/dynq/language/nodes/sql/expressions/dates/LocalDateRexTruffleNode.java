package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates;


import java.time.LocalDate;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class LocalDateRexTruffleNode extends RexTruffleNode {

    @Override
    public abstract LocalDate executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;

}
