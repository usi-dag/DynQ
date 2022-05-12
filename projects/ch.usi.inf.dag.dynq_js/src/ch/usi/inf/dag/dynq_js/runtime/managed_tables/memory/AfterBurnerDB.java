package ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory;

import org.apache.calcite.sql.type.SqlTypeName;

public class AfterBurnerDB {

    public enum AfterBurnerDataType {
        INT(SqlTypeName.INTEGER),
        DOUBLE(SqlTypeName.DOUBLE),
        STRING(SqlTypeName.VARCHAR),
        DATE(SqlTypeName.DATE),
        CHAR(SqlTypeName.CHAR);

        private final SqlTypeName sqlTypeName;
        AfterBurnerDataType(SqlTypeName sqlTypeName) {
            this.sqlTypeName = sqlTypeName;
        }

        public SqlTypeName getSqlTypeName() {
            return sqlTypeName;
        }
    }

    public static class Column {
        private final String name;
        private final long offset;
        private final AfterBurnerDataType dataType;

        public Column(String name, long offset, AfterBurnerDataType dataType) {
            this.name = name;
            this.offset = offset;
            this.dataType = dataType;
        }

        public String getName() {
            return name;
        }

        public long getOffset() {
            return offset;
        }

        public AfterBurnerDataType getDataType() {
            return dataType;
        }
    }

    public static class Table {
        private final String name;
        private final long nElements;
        private final Column[] columns;

        public Table(String name, long nElements, Column[] columns) {
            this.name = name;
            this.nElements = nElements;
            this.columns = columns;
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
    }

}
