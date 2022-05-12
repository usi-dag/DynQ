package ch.usi.inf.dag.dynq.runtime.objects.api;

import ch.usi.inf.dag.dynq.language.nodes.TruffleLinqRootNode;
import ch.usi.inf.dag.dynq.parser.CalciteParser;
import ch.usi.inf.dag.dynq.runtime.objects.data.DynQNullValue;
import ch.usi.inf.dag.dynq.runtime.utils.InteropUtils;
import ch.usi.inf.dag.dynq_r.language.RLanguageSpecificExtension;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.volcano.table_scans.VolcanoIteratorRDataFrameTableScanNode;
import ch.usi.inf.dag.dynq_r.parser.tpch_hardcoded_plans.HardCodedPlanGetter;
import ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory.DynamicRDataFrameInputArrayTable;
import ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory.DynamicRDataFrameInputArrayTableWithNAChecks;
import ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory.RDataFrameInputArrayWithSchemaAnyTypeTable;
import ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory.RDataFrameInputArrayWithSchemaTable;
import ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory.RDataFrameTable;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.Table;


@ExportLibrary(InteropLibrary.class)
public class APISessionManagementR extends APISessionManagement {

    private static final boolean USE_NA_CHECKS = "true".equals(System.getenv().get("DYNQ_R_USE_NA_CHECKS"));
    private static final String PREPARE_HARDCODED_TPCH = "prepareHardCodedTPCH";
    private static final String DYNQ_PREPARE_QUERY_API = System.getenv().getOrDefault("DYNQ_PREPARE_QUERY_API", "prepare");

    private final GetHardCodedTPCHMember getHardCodedTPCHMember = new GetHardCodedTPCHMember();

    public APISessionManagementR() {
        super(PREPARE_HARDCODED_TPCH);
    }

    @Override
    @ExportMessage
    public Object readMember(String name) throws UnknownIdentifierException {
        if(name.equals(PREPARE_HARDCODED_TPCH)) {
            return getHardCodedTPCHMember;
        }
        return super.readMember(name);
    }

    @Override
    protected Table getDynamicTable(String name, Object input) {
        if(input instanceof RList) {
            // TODO check that the list is actually a data frame
            RList rList = (RList) input;
            int size = (int) VolcanoIteratorRDataFrameTableScanNode.getInputSize(rList);
            return USE_NA_CHECKS
                ? new DynamicRDataFrameInputArrayTableWithNAChecks(parseRTable(name, rList), size)
                : new DynamicRDataFrameInputArrayTable(rList, size);
        }
        return super.getDynamicTable(name, input);
    }

    @Override
    protected Table getSchemaTable(String name, Object input, Object schema) {
        if(input instanceof RList) {
            // TODO check that the list is actually a data frame
            try {
                if(InteropUtils.INTEROP.isString(schema) && "schema_anytype".equals(InteropUtils.expectString(schema))) {
                    return new RDataFrameInputArrayWithSchemaAnyTypeTable(parseRTable(name, (RList) input));
                } else {
                    return new RDataFrameInputArrayWithSchemaTable(parseRTable(name, (RList) input));
                }
            } catch (UnsupportedTypeException e) {
                throw new RuntimeException(e);
            }
        } else {
            return super.getDynamicTable(name, input);
        }
    }

    private static RDataFrameTable.Table parseRTable(String name, RList rList) {
        int size = (int) VolcanoIteratorRDataFrameTableScanNode.getInputSize(rList);
        RStringVector names = (RStringVector) rList.getAttr("names");
        Object[] data = (Object[]) rList.getData();
        RDataFrameTable.Column[] columns = new RDataFrameTable.Column[data.length];
        for (int i = 0; i < data.length; i++) {
            String colName = names.getDataAt(i);
            RAbstractVector colData = (RAbstractVector) data[i];
            RType type = colData.getRType();
            RDataFrameTable.RDataFrameTableDataType truffleLinqType;
            switch(type) {
                case Integer:
                    truffleLinqType = RDataFrameTable.RDataFrameTableDataType.INT;
                    break;
                case Double:
                    try {
                        RStringVector rClasses = (RStringVector) ((RAbstractVector) rList.getDataAt(i)).getAttr("class");
                        String rClass = rClasses.getDataAt(0);
                        if(rClass.equals("Date")) {
                            truffleLinqType = RDataFrameTable.RDataFrameTableDataType.DATE;
                        } else {
                            truffleLinqType = RDataFrameTable.RDataFrameTableDataType.DOUBLE;
                        }
                    } catch (NullPointerException ignore) {
                        truffleLinqType = RDataFrameTable.RDataFrameTableDataType.DOUBLE;
                    }
                    break;
                case Character:
                    truffleLinqType = RDataFrameTable.RDataFrameTableDataType.STRING;
                    break;
                default:
                    CompilerDirectives.transferToInterpreter();
                    throw new RuntimeException("Unexpected vector type: " + type);
            }
            columns[i] = new RDataFrameTable.Column(colName, colData, truffleLinqType);
        }
        return new RDataFrameTable.Table(name, size, columns, rList);
    }


    @ExportLibrary(InteropLibrary.class)
    class GetHardCodedTPCHMember extends AbstractAPITruffleObject {
        @ExportMessage
        public boolean isExecutable() {
            return true;
        }

        @ExportMessage
        public Object execute(Object... arguments) throws UnsupportedTypeException {
            int query = InteropUtils.expectInt(arguments[0]);
            CalciteParser calciteParser = new CalciteParser(APISessionManagementR.this, new RLanguageSpecificExtension());
            RelNode relNode = HardCodedPlanGetter.getPlan(query, APISessionManagementR.this);
            if(relNode == null) {
                return DynQNullValue.INSTANCE;
            }
            if("prepare".equals(DYNQ_PREPARE_QUERY_API)) {
                TruffleLinqRootNode executableNode = calciteParser.convertToTruffle(relNode);
                CallTarget ct = Truffle.getRuntime().createCallTarget(executableNode);
                return new APIEntryPoint.PreparedQuery(Truffle.getRuntime().createDirectCallNode(ct));
            } else {
                return new APIEntryPoint.ParsedQuery(relNode, new CalciteParser(APISessionManagementR.this, new RLanguageSpecificExtension()));
            }
        }
    }
}
