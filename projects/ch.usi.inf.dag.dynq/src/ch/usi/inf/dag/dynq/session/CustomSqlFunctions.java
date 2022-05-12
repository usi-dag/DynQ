package ch.usi.inf.dag.dynq.session;


import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;

import java.util.Arrays;


public enum CustomSqlFunctions {

    STARTS_WITH(
            new SqlFunction("STARTS_WITH",
                    SqlKind.OTHER_FUNCTION,
                    ReturnTypes.BOOLEAN,
                    null,
                    OperandTypes.ANY_ANY,
                    SqlFunctionCategory.NUMERIC)),

    STARTS_WITH_UNSENSITIVE(
            new SqlFunction("STARTS_WITH_UNSENSITIVE",
                    SqlKind.OTHER_FUNCTION,
                    ReturnTypes.BOOLEAN,
                    null,
                    OperandTypes.ANY_ANY,
                    SqlFunctionCategory.NUMERIC));

    public final SqlOperator function;

    CustomSqlFunctions(SqlFunction function) {
        this.function = function;
    }

    public static boolean exists(String name) {
        String upper = name.toUpperCase();
        return Arrays.stream(values()).anyMatch(f -> f.name().toUpperCase().equals(upper));
    }

}
