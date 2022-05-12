package ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins;

import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public abstract class BinaryJoinCondition extends Node implements Explainable {

    public abstract boolean execute(VirtualFrame frame, Object left, Object right) throws InteropException, FrameSlotTypeException;

}
