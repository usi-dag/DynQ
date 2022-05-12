package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;


import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public abstract class TruffleLinqExecutableNode extends Node implements Explainable {

    public abstract Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException;

}
