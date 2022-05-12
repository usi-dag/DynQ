package ch.usi.inf.dag.dynq.parser.rex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;


public class RexConversionUtils {

    public static LocalDate asLocalDate(Calendar calendar) {
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toLocalDate();
    }

}
