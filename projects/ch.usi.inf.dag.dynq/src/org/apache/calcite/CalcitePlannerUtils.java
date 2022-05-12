package org.apache.calcite;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.parser.SqlParser;


public class CalcitePlannerUtils {

    public static SqlParser.Config getSqlParserConfig() {
        return SqlParser.config().withLex(Lex.JAVA);
    }

}
