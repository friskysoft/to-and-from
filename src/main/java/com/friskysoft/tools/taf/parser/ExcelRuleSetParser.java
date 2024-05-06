package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.models.DataType;
import com.friskysoft.tools.taf.models.Rule;
import com.friskysoft.tools.taf.models.RuleSet;
import com.friskysoft.tools.taf.utils.ExcelUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ExcelRuleSetParser implements FileRuleSetParser {

    protected static final String defaultInvalidTagChars = "[^A-Za-z0-9_:.\\-\\s]";

    protected String invalidTagCharPattern() {
        return defaultInvalidTagChars;
    }

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

    protected char getDictionaryColumn() {
        return '-';
    }

    protected char getExpressionColumn() {
        return '-';
    }

    public static int getColumnIndex(char col) {
        return Character.toUpperCase(col) - 'A';
    }

    public static String getColumn(List<String> row, char col) {
        return row.get(getColumnIndex(col));
    }

    private String sanitizeTag(String tag) {
        if (tag == null) {
            return null;
        }
        return tag.trim().replaceAll(this.invalidTagCharPattern(), "");
    }

    public String getSource(List<String> row) {
        return sanitizeTag(getColumn(row, this.getSourceColumn()));
    }

    public String getTarget(List<String> row) {
        return sanitizeTag(getColumn(row, this.getTargetColumn()));
    }


    public String getDictionary(List<String> row) {
        return getIfDefined(row, this.getDictionaryColumn());
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

    protected String getIfDefined(List<String> row, char column) {
        if (!Character.isAlphabetic(column)) {
            return null;
        }
        String val = getColumn(row, column);
        if (StringUtils.isBlank(val)) {
            return null;
        }
        return StringUtils.trim(val);
    }

    public RuleSet parseRuleSet(final String excelFilepath) {
        final ArrayList<List<String>> rows = ExcelUtil.read(excelFilepath);
        final RuleSet ruleSet = RuleSet.builder().build();

        for (int i = 1; i < rows.size(); i++) {

            final List<String> row = rows.get(i);

            final String source = this.getSource(row);
            final String target = this.getTarget(row);
            final DataType dataType = DataType.parse(this.getType(row));
            final String defaultVal = this.getDefaultValue(row);
            final String dictionary = this.getDictionary(row);
            final String expression = this.getExpression(row);

            final String[] sources;
            final String[] targets;

            if (StringUtils.isBlank(source)) {
                sources = new String[]{null};
            } else {
                sources = source.split("\\s");
            }

            if (StringUtils.isBlank(target)) {
                targets = new String[]{null};
            } else {
                targets = target.split("\\s");
            }

            for (String from : sources) {
                for (String to : targets) {
                    if (StringUtils.isBlank(to)) {
                        to = "___UNMAPPED_TARGET___." + from;
                    }
                    final Rule rule = Rule.builder().from(from).type(dataType).build();
                    if (StringUtils.isNotBlank(defaultVal)) {
                        rule.setDefaultValue(defaultVal);
                    }
                    if (StringUtils.isNotBlank(dictionary)) {
                        //TODO
                    }
                    if (StringUtils.isNotBlank(expression)) {
                        //TODO
                    }
                    ruleSet.getOutput().put(to, rule);
                }
            }
        }
        return ruleSet;
    }
}
