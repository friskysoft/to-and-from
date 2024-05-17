package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.utils.CSVUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CSVRuleSetParser extends AbstractRowFileRuleSetParser {

    @Builder.Default
    private int sourceColumn = 0;

    @Builder.Default
    private int targetColumn = 1;

    @Builder.Default
    private int defaultValueColumn = -1;

    @Builder.Default
    private int typeColumn = -1;

    protected String getSource(List<String> row) {
        return sanitizeField(row.get(this.getSourceColumn()));
    }

    protected String getTarget(List<String> row) {
        return sanitizeField(row.get(this.getTargetColumn()));
    }

    protected String getDefaultValue(List<String> row) {
        return getIfDefined(row, getDefaultValueColumn());
    }

    protected String getType(List<String> row) {
        return getIfDefined(row, getTypeColumn());
    }

    protected String getExpression(List<String> row) {
        return null;
    }

    protected List<List<String>> readFile(String filepath) {
        return CSVUtil.read(filepath);
    }

}
