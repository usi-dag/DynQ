package ch.usi.inf.dag.dynq.runtime.objects.resultsets;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ExportLibrary(InteropLibrary.class)
public abstract class PolyglotArrayProxyResultSet implements TruffleObject {

  private final int numElements;

  PolyglotArrayProxyResultSet(int numElements) {
    this.numElements = numElements;
  }

  static public PolyglotArrayProxyResultSet fromList(List<Object[]> data, String[] memberNames) {
    ProxyRow.ProxyRowBuilder proxyBuilder = ProxyRow.builder(memberNames);
    return new PolyglotArrayProxyResultSet(data.size()) {
      @Override
      Object getItem(int index) {
        return proxyBuilder.build(data.get(index));
      }
    };
  }


  static public PolyglotArrayProxyResultSet fromArray(Object[][] data, String[] memberNames) {
    ProxyRow.ProxyRowBuilder proxyBuilder = ProxyRow.builder(memberNames);
    return new PolyglotArrayProxyResultSet(data.length) {
      @Override
      Object getItem(int index) {
        return proxyBuilder.build(data[index]);
      }
    };
  }


  static public PolyglotArrayProxyResultSet fromDynamicObjectArray(Object[] data) {
    return new PolyglotArrayProxyResultSet(data.length) {
      @Override
      Object getItem(int index) {
        return data[index];
      }
    };
  }

  static public PolyglotArrayProxyResultSet fromDynamicObjectArrayList(ArrayList<?> data) {
    return new PolyglotArrayProxyResultSet(data.size()) {
      @Override
      Object getItem(int index) {
        return data.get(index);
      }
    };
  }

  // TMP? actually used only by Calcite engine
  static public PolyglotArrayProxyResultSet fromResultSet(ResultSet resultSet) {
    try {
      ResultSetMetaData meta = resultSet.getMetaData();
      int nCols = meta.getColumnCount();
      String[] colNames = new String[nCols];
      for (int i = 0; i < nCols; i++) {
        colNames[i] = meta.getColumnLabel(i+1);
      }

      List<Object[]> rows = new LinkedList<>();

      while(resultSet.next()) {
        Object[] row = new Object[nCols];
        for (int i = 0; i < nCols; i++) {
          row[i] = resultSet.getObject(i+1);
        }
        rows.add(row);
      }

      return fromList(rows, colNames);

    } catch (SQLException e) {
      CompilerDirectives.transferToInterpreter();
      e.printStackTrace();
      return null;
    }
  }

  abstract Object getItem(int index);


  // Implementation of InteropLibrary

  @ExportMessage
  @SuppressWarnings("static-method")
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  public long getArraySize() {
    return numElements;
  }

  @ExportMessage
  boolean isArrayElementReadable(long index) {
    return true;
  }

  @ExportMessage
  boolean isArrayElementModifiable(long index) {
    return false;
  }

  @SuppressWarnings("static-method")
  @ExportMessage
  boolean isArrayElementInsertable(@SuppressWarnings("unused") long index) {
    return false;
  }

  @ExportMessage
  Object readArrayElement(long index) {
    return getItem((int) index);
  }

  @ExportMessage
  final void writeArrayElement(long index, Object arg2) throws UnsupportedMessageException {
    throw UnsupportedMessageException.create();
  }

}
