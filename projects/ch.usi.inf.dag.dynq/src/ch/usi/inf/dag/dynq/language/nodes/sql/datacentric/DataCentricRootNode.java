package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;


import ch.usi.inf.dag.dynq.language.TruffleLinqLanguage;
import ch.usi.inf.dag.dynq.language.nodes.TruffleLinqRootNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class DataCentricRootNode extends TruffleLinqRootNode {

    @Child
    TruffleLinqExecutableNode executableNode;

    public DataCentricRootNode(TruffleLinqLanguage truffleLinqLanguage, TruffleLinqExecutableNode executableNode) {
        super(truffleLinqLanguage);
        this.executableNode = executableNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            return executableNode.execute(frame);
        } catch (InteropException | FrameSlotTypeException e) {
            CompilerDirectives.transferToInterpreter();
            e.printStackTrace();
            return "FAILED: " + e.getMessage();
        }
    }
}
