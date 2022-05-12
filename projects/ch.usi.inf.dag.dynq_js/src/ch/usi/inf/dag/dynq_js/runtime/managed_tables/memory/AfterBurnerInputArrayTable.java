package ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory;

import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.ResettableDynamicRecordTable;
import com.oracle.truffle.api.object.DynamicObject;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;

public abstract class AfterBurnerInputArrayTable extends AbstractTable
        implements TranslatableTable, ResettableDynamicRecordTable {

    protected final DynamicObject jsArrayBuffer;
    protected final AfterBurnerDB.Table table;
    protected final long nElements;

    public AfterBurnerInputArrayTable(DynamicObject jsArrayBuffer, AfterBurnerDB.Table table) {
        this.jsArrayBuffer = jsArrayBuffer;
        this.table = table;
        this.nElements = table.getnElements();
    }

    public AfterBurnerDB.Table getTable() {
        return table;
    }
    public DynamicObject getBuffer() {
        return jsArrayBuffer;
    }

}
