package com.friskysoft.tools.taf.utils;

import com.friskysoft.tools.taf.models.DataType;
import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public final class DateUtil {

    private static final Map<String, String> DATE_FORMAT_REGEX_MAP = new LinkedHashMap<>() {{
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^[2-3][0-9]/\\d{1,2}/\\d{4}$", "dd/MM/yyyy");
        put("^[1][3-9]/\\d{1,2}/\\d{4}$", "dd/MM/yyyy");
        put("^\\d{1,2}-[2-3][0-9]-\\d{4}$", "MM-dd-yyyy");
        put("^\\d{1,2}-[1][3-9]-\\d{4}$", "MM-dd-yyyy");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}[tT]\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd'T'HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    }};

    public static final Map<DataType, String> DATE_FORMAT_MAP = Map.of(
            DataType.DATE, "yyyy-MM-dd",
            DataType.DATE_US, "MM/dd/yyyy",
            DataType.DATETIME, "yyyy-MM-dd'T'HH:mm:ss'Z'",
            DataType.DATETIME_SQL, "yyyy-MM-dd HH:mm:ss",
            DataType.TIMESTAMP, ""
    );

    public static String convert(String dateString, DataType toType) {
        if (DATE_FORMAT_MAP.containsKey(toType)) {
            Date date = parse(dateString);
            if (date == null) {
                return toType.name() + ":FORMAT_ERROR:" + dateString;
            } else if (toType == DataType.TIMESTAMP) {
                return String.valueOf(date.getTime());
            } else {
                return new SimpleDateFormat(DATE_FORMAT_MAP.get(toType)).format(date);
            }
        } else {
            return dateString;
        }
    }

    public static Date parse(String dateString) {
        String dateFormat = determineDateFormat(dateString);
        if (dateFormat == null) {
            return null;
        }
        return parse(dateString, dateFormat);
    }

    public static Date parse(String dateString, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        simpleDateFormat.setLenient(false); // Don't automatically convert invalid date.
        try {
            return simpleDateFormat.parse(dateString);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String determineDateFormat(String dateString) {
        if (dateString == null) {
            return null;
        }
        dateString = dateString
                .toLowerCase()
                .replace("z", "")
                .trim();

        for (String regexp : DATE_FORMAT_REGEX_MAP.keySet()) {
            if (dateString.matches(regexp)) {
                return DATE_FORMAT_REGEX_MAP.get(regexp);
            }
        }
        return null;
    }
}
