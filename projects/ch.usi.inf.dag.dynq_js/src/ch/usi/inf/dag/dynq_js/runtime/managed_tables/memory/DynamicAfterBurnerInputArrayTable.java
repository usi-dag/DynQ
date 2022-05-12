package ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory;


import com.google.common.collect.ImmutableList;
import com.oracle.truffle.api.object.DynamicObject;
import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.DynamicRecordType;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeFamily;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rel.type.RelDataTypeFieldImpl;
import org.apache.calcite.rel.type.RelDataTypePrecedenceList;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.sql.type.SqlTypeExplicitPrecedenceList;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.ImmutableBitSet;
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DynamicAfterBurnerInputArrayTable extends AfterBurnerInputArrayTable {
    private RelDataType dataType = null;

    public DynamicAfterBurnerInputArrayTable(DynamicObject jsArrayBuffer, AfterBurnerDB.Table table) {
        super(jsArrayBuffer, table);
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if(dataType == null) {
            dataType = new DynamicRecordTypeImplWithNaChecks(typeFactory);
        }
        return dataType;
    }

    @Override
    public Statistic getStatistic() {
        return Statistics.of(nElements, ImmutableList.of());
    }

    @Override
    public void resetDataType() {
        dataType = null;
    }

    @Override
    public RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
        return EnumerableTableScan.create(context.getCluster(), relOptTable);
    }




    private static class DynamicRecordTypeImplWithNaChecks extends DynamicRecordType {
        private final RelDataTypeHolderWithNaChecks holder;

        public DynamicRecordTypeImplWithNaChecks(RelDataTypeFactory typeFactory) {
            this.holder = new RelDataTypeHolderWithNaChecks(typeFactory);
            this.computeDigest();
        }

        public List<RelDataTypeField> getFieldList() {
            return this.holder.getFieldList();
        }

        public int getFieldCount() {
            return this.holder.getFieldCount();
        }

        public RelDataTypeField getField(String fieldName, boolean caseSensitive, boolean elideRecord) {
            Pair<RelDataTypeField, Boolean> pair = this.holder.getFieldOrInsert(fieldName, caseSensitive);
            if (pair.right) {
                this.computeDigest();
            }

            return pair.left;
        }

        public List<String> getFieldNames() {
            return this.holder.getFieldNames();
        }

        public SqlTypeName getSqlTypeName() {
            return SqlTypeName.ROW;
        }

        public RelDataTypePrecedenceList getPrecedenceList() {
            return new SqlTypeExplicitPrecedenceList(ImmutableList.of());
        }

        protected void generateTypeString(StringBuilder sb, boolean withDetail) {
            sb.append("(DynamicRecordRow").append(this.getFieldNames()).append(")");
        }

        public boolean isStruct() {
            return true;
        }

        public RelDataTypeFamily getFamily() {
            return this.getSqlTypeName().getFamily();
        }
    }

    private static class RelDataTypeHolderWithNaChecks {
        private final List<RelDataTypeField> fields = new ArrayList<>();
        private final RelDataTypeFactory typeFactory;

        RelDataTypeHolderWithNaChecks(RelDataTypeFactory typeFactory) {
            this.typeFactory = typeFactory;
        }

        public List<RelDataTypeField> getFieldList() {
            return this.fields;
        }

        public int getFieldCount() {
            return this.fields.size();
        }

        Pair<RelDataTypeField, Boolean> getFieldOrInsert(String fieldName, boolean caseSensitive) {
            Iterator<RelDataTypeField> var3 = this.fields.iterator();
            boolean nullable = false;

            RelDataTypeField f;
            do {
                if (!var3.hasNext()) {
                    SqlTypeName typeName = DynamicRecordType.isDynamicStarColName(fieldName) ? SqlTypeName.DYNAMIC_STAR : SqlTypeName.ANY;
                    RelDataTypeField newField = new RelDataTypeFieldImpl(fieldName, this.fields.size(), this.typeFactory.createTypeWithNullability(this.typeFactory.createSqlType(typeName), nullable));
                    this.fields.add(newField);
                    return Pair.of(newField, true);
                }

                f = var3.next();
                if (Util.matches(caseSensitive, f.getName(), fieldName)) {
                    return Pair.of(f, false);
                }
            } while(f.getType().getSqlTypeName() != SqlTypeName.DYNAMIC_STAR);

            return Pair.of(f, false);
        }

        public List<String> getFieldNames() {
            return Pair.left(this.fields);
        }
    }

}