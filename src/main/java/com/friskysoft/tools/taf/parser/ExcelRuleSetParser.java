package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.utils.ExcelUtil;

import java.util.List;

public class ExcelRuleSetParser extends AbstractRowFileRuleSetParser {

    protected char getSourceColumn() {
        return 'A';
    }

    protected char getTargetColumn() {
        return 'B';
    }

    protected char getTypeColumn() {
        return '-';
    }

    protected char getDefaultValueColumn() {
        return '-';
    }

    protected char getExpressionColumn() {
        return '-';
    }

    public static String getColumn(List<String> row, char col) {
        return row.get(getColumnIndexForLetter(col));
    }

    public String getSource(List<String> row) {
        return sanitizeField(getColumn(row, this.getSourceColumn()));
    }

    public String getTarget(List<String> row) {
        return sanitizeField(getColumn(row, this.getTargetColumn()));
    }

    public String getExpression(List<String> row) {
        return getIfDefined(row, this.getExpressionColumn());
    }

    public String getDefaultValue(List<String> row) {
        return getIfDefined(row, this.getDefaultValueColumn());
    }

    public String getType(List<String> row) {
        return getIfDefined(row, this.getTypeColumn());
    }

    protected List<List<String>> readFile(String filepath) {
        return ExcelUtil.read(filepath);
    }

}
