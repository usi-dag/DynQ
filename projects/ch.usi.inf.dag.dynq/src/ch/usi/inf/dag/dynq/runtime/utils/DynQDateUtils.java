package ch.usi.inf.dag.dynq.runtime.utils;

import com.oracle.truffle.api.CompilerDirectives;

public class DynQDateUtils {

    // mostly copied from JSDate

    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MS_PER_SECOND = 1000;
    public static final int MS_PER_MINUTE = 60000;
    private static final int MS_PER_HOUR = 3600000;
    public static final int MS_PER_DAY = 3600000 * 24;
    public static final double MAX_DATE = 8.64E15;

    private static final int DAYS_IN_4_YEARS = 4 * 365 + 1;
    private static final int DAYS_IN_100_YEARS = 25 * DAYS_IN_4_YEARS - 1;
    private static final int DAYS_IN_400_YEARS = 4 * DAYS_IN_100_YEARS + 1;
    private static final int DAYS_FROM_1970_TO_2000 = 30 * 365 + 7;

    // Helper constants for yearFromTime(), YEAR_SHIFT must be divisible by 400
    // and represent more than 30 years plus 100,000,000 days
    private static final int YEAR_SHIFT = 280000;
    private static final int DAY_SHIFT = (YEAR_SHIFT / 400) * DAYS_IN_400_YEARS;


    public static int yearFromDays(int daysAfter1970) {
        // we need days relative to a year divisible by 400
        int daysAfter2000 = daysAfter1970 - DAYS_FROM_1970_TO_2000;
        // days after year (2000 - yearShift)
        int days = daysAfter2000 + DAY_SHIFT;
        // we need days > 0 to ensure that integer division rounds correctly
        assert days > 0;

        int year = 400 * (days / DAYS_IN_400_YEARS);
        int remainingDays = days % DAYS_IN_400_YEARS;
        remainingDays--;
        year += 100 * (remainingDays / DAYS_IN_100_YEARS);
        remainingDays %= DAYS_IN_100_YEARS;
        remainingDays++;
        year += 4 * (remainingDays / DAYS_IN_4_YEARS);
        remainingDays %= DAYS_IN_4_YEARS;
        remainingDays--;
        year += remainingDays / 365;

        return year - YEAR_SHIFT + 2000;
    }

    @CompilerDirectives.TruffleBoundary
    public static int monthFromDays(int daysAfter1970) {
        int year = yearFromDays(daysAfter1970);
        boolean leapYear = isLeapYear(year);
        int day = dayWithinYear(daysAfter1970, year);
        return monthFromTimeIntl(leapYear, day);
    }

    private static boolean isLeapYear(int year) {
        if (year % 4 != 0) {
            return false;
        }
        if (year % 100 != 0) {
            return true;
        }
        return year % 400 == 0;
    }

    private static int monthFromTimeIntl(boolean leapYear, int day) {
        assert (0 <= day) && (day < (365 + (leapYear ? 1 : 0))) : "should not reach here";

        if (day < 31) {
            return 0;
        }
        if (!leapYear) {
            if (day < 59) {
                return 1;
            }
            if (day < 90) {
                return 2;
            }
            if (day < 120) {
                return 3;
            }
            if (day < 151) {
                return 4;
            }
            if (day < 181) {
                return 5;
            }
            if (day < 212) {
                return 6;
            }
            if (day < 243) {
                return 7;
            }
            if (day < 273) {
                return 8;
            }
            if (day < 304) {
                return 9;
            }
            if (day < 334) {
                return 10;
            }
            return 11;
        } else {
            if (day < 60) {
                return 1;
            }
            if (day < 91) {
                return 2;
            }
            if (day < 121) {
                return 3;
            }
            if (day < 152) {
                return 4;
            }
            if (day < 182) {
                return 5;
            }
            if (day < 213) {
                return 6;
            }
            if (day < 244) {
                return 7;
            }
            if (day < 274) {
                return 8;
            }
            if (day < 305) {
                return 9;
            }
            if (day < 335) {
                return 10;
            }
            return 11;
        }
    }

    // 15.9.1.4
    private static int dayWithinYear(int daysAfter1970, int year) {
        return daysAfter1970 - dayFromYear(year);
    }

    private static int dayFromYear(int y) {
        return 365 * (y - 1970) + Math.floorDiv(y - 1969, 4) - Math.floorDiv(y - 1901, 100) + Math.floorDiv(y - 1601, 400);
    }
}
