package com.digitilius.multireestr.legacy.notice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class LegacyResultSets {

    private LegacyResultSets() {
    }

    static LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDateTime.class);
    }

    static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDate.class);
    }

    static Integer integer(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new SQLException("Ожидалось число в колонке " + column + ", получено " + value.getClass().getName());
    }

    static Boolean flag(ResultSet rs, String column) throws SQLException {
        Integer value = integer(rs, column);
        if (value == null) {
            return null;
        }
        return value != 0;
    }
}
