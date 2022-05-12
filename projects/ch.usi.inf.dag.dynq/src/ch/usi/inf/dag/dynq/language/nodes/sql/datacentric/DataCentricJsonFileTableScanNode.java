package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public abstract class DataCentricJsonFileTableScanNode extends TruffleLinqExecutableNode {

  final File input;
  BufferedReader reader = null;

  @Child
  DataCentricConsumerNode consumerNode;

  public DataCentricJsonFileTableScanNode(File input, DataCentricConsumerNode consumerNode) {
    this.input = input;
    this.consumerNode = consumerNode;
  }

  @Specialization
  Object executeScan(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
    consumerNode.init(frame);
    open();
    String line;
    try {
      while((line = readLine()) != null) {
        consumerNode.execute(frame, line);
      }
    } catch (EndOfComputation ignored) {}
    close();
    Object result = consumerNode.getFinalizedState(frame);
    consumerNode.free(frame);
    return result;
  }

  @CompilerDirectives.TruffleBoundary
  private void open() {
    try {
      reader = new BufferedReader(new FileReader(input));
    } catch (FileNotFoundException e) {
      CompilerDirectives.transferToInterpreter();
      throw new RuntimeException(e);
    }
  }


  @CompilerDirectives.TruffleBoundary
  private void close() {
    try {
      reader.close();
    } catch (IOException e) {
      CompilerDirectives.transferToInterpreter();
      throw new RuntimeException(e);
    }
  }

  @CompilerDirectives.TruffleBoundary
  private String readLine() {
    try {
      return reader.readLine();
    } catch (IOException e) {
      CompilerDirectives.transferToInterpreter();
      throw new RuntimeException(e);
    }
  }

}
