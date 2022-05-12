package ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory;

import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.sql.type.BasicSqlType;

public class BasicRDataFrameType extends BasicSqlType {

    private static final long serialVersionUID = -142299752984709256L;


    public BasicRDataFrameType(RelDataTypeSystem typeSystem, RDataFrameTable.RDataFrameTableDataType dataType) {
        super(typeSystem, dataType.getSqlTypeName());
    }

}
