package com.friskysoft.tools.taf.models;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum DataType {
    STRING("TEXT", "CHAR", "VARCHAR"),
    NUMBER("INT", "INTEGER", "LONG"),
    DECIMAL("FLOAT", "DOUBLE", "FRACTION"),
    DATE("DATE", "YYYY_MM_DD"),
    DATE_US("US_DATE", "MM_DD_YYYY"),
    DATETIME("TIME", "DATETIME_ISO"),
    DATETIME_SQL("DATETIME_SQL"),
    TIMESTAMP("EPOCH", "MILLIS", "EPOCH_MILLIS"),
    BOOLEAN("BOOL", "TRUE_FALSE"),
    ARRAY("LIST", "SET", "COLLECTION"),
    OBJECT("NESTED", "MAP");

    private final String[] aliases;

    DataType(String... aliases) {
        this.aliases = aliases;
    }

    private static final Map<String, DataType> lookupMap = new HashMap<>();

    static {
        for (DataType value : DataType.values()) {
            lookupMap.put(value.name().toUpperCase(), value);
            for (String alias : value.aliases) {
                lookupMap.put(alias.toUpperCase(), value);
            }
        }
    }

    public static DataType parse(String type) {
        if (StringUtils.isBlank(type)) {
            return STRING;
        } else {
            String normalized = type.trim().toUpperCase();
            return lookupMap.getOrDefault(normalized, STRING);
        }
    }
}
