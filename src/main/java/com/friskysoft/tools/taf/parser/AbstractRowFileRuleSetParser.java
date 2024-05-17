package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.core.Converter;
import com.friskysoft.tools.taf.models.DataType;
import com.friskysoft.tools.taf.models.Rule;
import com.friskysoft.tools.taf.models.RuleSet;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public abstract class AbstractRowFileRuleSetParser implements FileRuleSetParser {

    protected static final String defaultInvalidFieldChars = "[^A-Za-z0-9_:.\\-\\s]";

    protected String invalidFieldCharPattern() {
        return defaultInvalidFieldChars;
    }

    protected static int getColumnIndexForLetter(final char column) {
        if (Character.isDigit(column)) {
            return column - '0';
        } else if (!Character.isAlphabetic(column)) {
            return -1;
        } else {
            return Character.toUpperCase(column) - 'A';
        }
    }

    protected String sanitizeField(final String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll(this.invalidFieldCharPattern(), "");
    }

    protected abstract String getSource(List<String> row);

    protected abstract String getTarget(List<String> row);

    protected abstract String getDefaultValue(List<String> row);

    protected abstract String getExpression(List<String> row);

    protected String getDictionary(List<String> row) {
        // TODO
        return null;
    }

    protected String getTransform(List<String> row) {
        // TODO
        return null;
    }

    protected abstract String getType(List<String> row);

    protected String getIfDefined(final List<String> row, final char column) {
        return getIfDefined(row, getColumnIndexForLetter(column));
    }

    protected String getIfDefined(final List<String> row, final int columnIndex) {
        if (row == null || columnIndex >= row.size() || columnIndex < 0) {
            return null;
        }
        String val = row.get(columnIndex);
        if (StringUtils.isBlank(val)) {
            return null;
        } else {
            return StringUtils.trim(val);
        }
    }

    protected List<String> transformRow(final List<String> row) {
        return row;
    }

    protected boolean shouldProcessRow(final List<String> row) {
        return true;
    }

    protected abstract List<List<String>> readFile(final String filepath);

    public RuleSet parseRuleSet(final String filepath) {
        final List<List<String>> rows = readFile(filepath);
        return parseRuleSet(rows);
    }

    public RuleSet parseRuleSet(final List<List<String>> rows) {
        final RuleSet ruleSet = RuleSet.builder().build();

        for (int i = 1; i < rows.size(); i++) {

            final List<String> row = transformRow(rows.get(i));

            if (!shouldProcessRow(row)) {
                // skip row based on some filter logic
                continue;
            }

            final String source = this.getSource(row);
            final String target = this.getTarget(row);
            final DataType dataType = DataType.parse(this.getType(row));
            final String defaultVal = this.getDefaultValue(row);
            final String dictionary = this.getDictionary(row);
            final String expression = this.getExpression(row);
            final String transform = this.getTransform(row);

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
                        to = Converter.REPORT_UNMAPPED_TARGET_PREFIX + "." + from;
                    }
                    final Rule rule = Rule.builder().from(from).type(dataType).build();
                    if (StringUtils.isNotBlank(defaultVal)) {
                        rule.setDefaultValue(defaultVal);
                    }
                    if (StringUtils.isNotBlank(dictionary)) {
                        // TODO: implement
                    }
                    if (StringUtils.isNotBlank(expression)) {
                        // TODO: implement
                    }
                    if (StringUtils.isNotBlank(transform)) {
                        // TODO: implement
                    }
                    ruleSet.getOutput().put(to, rule);
                }
            }
        }
        return ruleSet;
    }
}
