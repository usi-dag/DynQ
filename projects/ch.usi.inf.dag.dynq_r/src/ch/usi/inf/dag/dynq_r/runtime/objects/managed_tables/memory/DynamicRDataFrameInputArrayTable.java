package ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory;


import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.ResettableDynamicRecordTable;
import com.google.common.collect.ImmutableList;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.DynamicRecordTypeImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;

import java.util.HashMap;


public class DynamicRDataFrameInputArrayTable extends AbstractTable
        implements TranslatableTable, ResettableDynamicRecordTable {

    private static final boolean USE_NATIVE_STRINGS = "true".equals(System.getenv("DYNQ_R_NATIVE_STRINGS"));

    private RelDataType dataType = null;
    private final int nElements;
    private final RList input;

    private final HashMap<String, RAbstractVector> vectorNames;


    public DynamicRDataFrameInputArrayTable(RList input, int nElements) {
        this.input = input;
        this.nElements = nElements;

        vectorNames = new HashMap<>();
        RStringVector names = input.getNames();
        Object[] data = (Object[]) input.getData();
        RAbstractVector[] vectors = new RAbstractVector[names.getLength()];
        for (int i = 0; i < input.getNames().getLength(); i++) {
            String name = input.getNames().getDataAt(i);
            RAbstractVector vector = (RAbstractVector) data[i];
            vectors[i] = vector;
            vectorNames.put(name, vector);
            if(USE_NATIVE_STRINGS && vector.getRType() == RType.Character && (!vector.hasNativeMemoryData())) {
                ((RStringVector) vector).containerLibToNative(ConditionProfile.create());
            }
            if(vector.hasNativeMemoryData()) {
                long address = vector.getNativeMirror().getDataAddress();
            }
        }
    }

    public RAbstractVector getVector(String name) {
        return vectorNames.get(name);
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if(dataType == null) {
            dataType = new DynamicRecordTypeImpl(typeFactory);
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

}
