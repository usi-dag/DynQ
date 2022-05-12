package ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory;


import com.google.common.collect.ImmutableList;
import com.oracle.truffle.r.runtime.data.RList;
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
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DynamicRDataFrameInputArrayTableWithNAChecks extends DynamicRDataFrameInputArrayTable {

    private RelDataType dataType = null;
    private final int nElements;
    private final RList input;
    private final RDataFrameTable.Table table;

    public DynamicRDataFrameInputArrayTableWithNAChecks(RDataFrameTable.Table table, int nElements) {
        super(table.getrList(), nElements);
        this.input = table.getrList();
        this.table = table;
        this.nElements = nElements;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if(dataType == null) {
            dataType = new DynamicRecordTypeImplWithNaChecks(typeFactory, table);
        }
        return dataType;
    }

    public RList getInput() {
        return input;
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

        public DynamicRecordTypeImplWithNaChecks(RelDataTypeFactory typeFactory, RDataFrameTable.Table table) {
            this.holder = new RelDataTypeHolderWithNaChecks(typeFactory, table);
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
        private final RDataFrameTable.Table table;

        RelDataTypeHolderWithNaChecks(RelDataTypeFactory typeFactory, RDataFrameTable.Table table) {
            this.typeFactory = typeFactory;
            this.table = table;
        }

        public List<RelDataTypeField> getFieldList() {
            return this.fields;
        }

        public int getFieldCount() {
            return this.fields.size();
        }

        Pair<RelDataTypeField, Boolean> getFieldOrInsert(String fieldName, boolean caseSensitive) {
            Iterator<RelDataTypeField> var3 = this.fields.iterator();
            boolean nullable = true;
            for(RDataFrameTable.Column column : table.getColumns()) {
                if(column.getName().equals(fieldName)) {
                     nullable = !column.getVector().access().na.neverSeenNA();
                }
            }

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
