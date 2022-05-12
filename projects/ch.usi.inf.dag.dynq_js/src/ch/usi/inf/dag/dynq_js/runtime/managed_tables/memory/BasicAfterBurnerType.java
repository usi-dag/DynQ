package ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory;

import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.sql.type.BasicSqlType;

public class BasicAfterBurnerType extends BasicSqlType {

    private static final long serialVersionUID = -1406688044462325332L;

    public BasicAfterBurnerType(RelDataTypeSystem typeSystem, AfterBurnerDB.AfterBurnerDataType dataType) {
        super(typeSystem, dataType.getSqlTypeName());
    }

}
