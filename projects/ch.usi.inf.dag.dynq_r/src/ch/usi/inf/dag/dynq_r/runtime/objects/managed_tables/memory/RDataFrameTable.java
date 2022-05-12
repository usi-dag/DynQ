package ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory;

import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import org.apache.calcite.sql.type.SqlTypeName;

public class RDataFrameTable {

    public enum RDataFrameTableDataType {
        INT(SqlTypeName.INTEGER),
        DOUBLE(SqlTypeName.DOUBLE),
        STRING(SqlTypeName.VARCHAR),
        DATE(SqlTypeName.DATE),
        CHAR(SqlTypeName.CHAR);

        private final SqlTypeName sqlTypeName;
        RDataFrameTableDataType(SqlTypeName sqlTypeName) {
            this.sqlTypeName = sqlTypeName;
        }

        public SqlTypeName getSqlTypeName() {
            return sqlTypeName;
        }
    }

    public static class Column {
        private final String name;
        private final RDataFrameTableDataType dataType;
        private final RAbstractVector vector;

        public Column(String name, RAbstractVector vector, RDataFrameTableDataType dataType) {
            this.name = name;
            this.dataType = dataType;
            this.vector = vector;
        }

        public String getName() {
            return name;
        }

        public RAbstractVector getVector() {
            return vector;
        }

        public RDataFrameTableDataType getDataType() {
            return dataType;
        }
    }

    public static class Table {
        private final String name;
        private final long nElements;
        private final Column[] columns;
        private final RList rList;

        public Table(String name, long nElements, Column[] columns, RList rList) {
            this.name = name;
            this.nElements = nElements;
            this.columns = columns;
            this.rList = rList;
        }

        public String getName() {
            return name;
        }

        public long getnElements() {
            return nElements;
        }

        public Column[] getColumns() {
            return columns;
        }

        public RList getrList() {
            return rList;
        }
    }
}
