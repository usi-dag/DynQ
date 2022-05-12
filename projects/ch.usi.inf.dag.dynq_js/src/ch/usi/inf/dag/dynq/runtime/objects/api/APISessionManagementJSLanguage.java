package ch.usi.inf.dag.dynq.runtime.objects.api;


import ch.usi.inf.dag.dynq.language.nodes.TruffleLinqRootNode;
import ch.usi.inf.dag.dynq.parser.CalciteParser;
import ch.usi.inf.dag.dynq.runtime.objects.data.DynQNullValue;
import ch.usi.inf.dag.dynq.runtime.utils.InteropUtils;
import ch.usi.inf.dag.dynq_js.language.JSLanguageSpecificExtension;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.functions.HaversineUDFNodeGen;
import ch.usi.inf.dag.dynq_js.parser.tpch_hardcoded_plans.HardCodedPlanGetter;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.AfterBurnerDB;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.AfterBurnerInputArrayTable;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.DynamicAfterBurnerInputArrayTable;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.SchemaAfterBurnerInputArrayTable;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.builtins.JSArrayBuffer;
import com.oracle.truffle.js.runtime.objects.Undefined;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;


@ExportLibrary(InteropLibrary.class)
public class APISessionManagementJSLanguage extends APISessionManagement {

    private static final String REGISTER_AFTERBURNER = "registerAfterBurner";
    private static final String PREPARE_HARDCODED_TPCH = "prepareHardCodedTPCH";
    private static final String DYNQ_PREPARE_QUERY_API = System.getenv().getOrDefault("DYNQ_PREPARE_QUERY_API", "prepare");


    public APISessionManagementJSLanguage(String... extraMembers) {
        super(extraMembers);
        // TODO get rid of this hack... it should be registered from JS
        super.getRegisteredJavaUDFTable().registerUDF("haversineJavaUDF", HaversineUDFNodeGen::create);
    }

    @Override
    @ExportMessage
    public Object invokeMember(String member, Object... arguments) throws UnknownIdentifierException, UnsupportedMessageException, ArityException, UnsupportedTypeException {
        if(REGISTER_AFTERBURNER.equals(member)) {
            try {
                return registerAfterBurner(arguments);
            } catch (InvalidArrayIndexException e) {
                throw new RuntimeException(e); // TODO
            }
        }
        if(PREPARE_HARDCODED_TPCH.equals(member)) {
            int query = InteropUtils.expectInt(arguments[0]);
            CalciteParser calciteParser = new CalciteParser(this, new JSLanguageSpecificExtension());
            RelNode relNode = HardCodedPlanGetter.getPlan(query, this);
            if(relNode == null) {
                return Undefined.instance;
            }
            if("prepare".equals(DYNQ_PREPARE_QUERY_API)) {
                TruffleLinqRootNode executableNode = calciteParser.convertToTruffle(relNode);
                CallTarget ct = Truffle.getRuntime().createCallTarget(executableNode);
                return new APIEntryPoint.PreparedQuery(Truffle.getRuntime().createDirectCallNode(ct));
            } else {
                return new APIEntryPoint.ParsedQuery(relNode, new CalciteParser(this, new JSLanguageSpecificExtension()));
            }
        }
        return super.invokeMember(member, arguments);
    }

    private Object registerAfterBurner(Object... arguments) throws ArityException, UnsupportedTypeException, UnknownIdentifierException, UnsupportedMessageException, InvalidArrayIndexException {
        // Expects two args: a memory reference and an AfterBurner Schema object
        InteropUtils.checkArgumentLength(arguments, 2);
        if(!JSArrayBuffer.isJSDirectArrayBuffer(arguments[0])) {
            throw UnsupportedTypeException.create(new Object[]{arguments[0]}, "Expected JSArrayBuffer");
        }

        DynamicObject memory = (DynamicObject) arguments[0];
        Object afterBurner = arguments[1];
        Object dynamicObjectTables = InteropUtils.INTEROP.readMember(afterBurner, "tables");
        int nTables = (int) InteropUtils.INTEROP.getArraySize(dynamicObjectTables);
        AfterBurnerDB.Table[] tables = new AfterBurnerDB.Table[nTables];
        AfterBurnerDB.AfterBurnerDataType[] types = AfterBurnerDB.AfterBurnerDataType.values();
        for(int i = 0; i < nTables; i++) {
            Object dynamicObjectTable = InteropUtils.INTEROP.readArrayElement(dynamicObjectTables, i);
            String name = InteropUtils.INTEROP.asString(InteropUtils.INTEROP.readMember(dynamicObjectTable, "name"));
            long nElements = InteropUtils.INTEROP.asLong(InteropUtils.INTEROP.readMember(dynamicObjectTable, "numrows"));
            int nCols = InteropUtils.INTEROP.asInt(InteropUtils.INTEROP.readMember(dynamicObjectTable, "numcols"));
            AfterBurnerDB.Column[] columns = new AfterBurnerDB.Column[nCols];

            Object dynamicObjectColNames = InteropUtils.INTEROP.readMember(dynamicObjectTable, "colnames");
            Object dynamicObjectColPtrs = InteropUtils.INTEROP.readMember(dynamicObjectTable, "cols");
            Object dynamicObjectColTypes = InteropUtils.INTEROP.readMember(dynamicObjectTable, "coltypes");
            for (int j = 0 ; j < nCols; j++) {
                String colName = InteropUtils.INTEROP.asString(InteropUtils.INTEROP.readArrayElement(dynamicObjectColNames, j));
                long colOffset = InteropUtils.INTEROP.asLong(InteropUtils.INTEROP.readArrayElement(dynamicObjectColPtrs, j));
                int colType = InteropUtils.INTEROP.asInt(InteropUtils.INTEROP.readArrayElement(dynamicObjectColTypes, j));
                columns[j] = new AfterBurnerDB.Column(colName, colOffset, types[colType]);
            }
            tables[i] = new AfterBurnerDB.Table(name, nElements, columns);
        }

        SchemaPlus rootSchema = getConnection().getRootSchema();
        for(AfterBurnerDB.Table table : tables) {
            AfterBurnerInputArrayTable afterBurnerInputArrayTable = USE_SCHEMA()
                    ? new SchemaAfterBurnerInputArrayTable(memory, table)
                    : new DynamicAfterBurnerInputArrayTable(memory, table);

            rootSchema.add(table.getName(), afterBurnerInputArrayTable);
            getRegisteredTables().registerTable(table.getName(), afterBurnerInputArrayTable);
        }

        return DynQNullValue.INSTANCE;
    }

    private static boolean USE_SCHEMA() {
        return "true".equals(
                System.getProperty("DYNQ_AFTERBURNER_USE_SCHEMA",
                        System.getenv().getOrDefault("DYNQ_AFTERBURNER_USE_SCHEMA", "false")));
    }
}