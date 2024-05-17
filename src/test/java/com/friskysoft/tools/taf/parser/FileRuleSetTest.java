package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.models.RuleSet;
import com.friskysoft.tools.taf.utils.FileUtil;
import com.friskysoft.tools.taf.utils.MapperUtil;
import com.friskysoft.tools.taf.utils.ResourceUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class FileRuleSetTest {

    private static class TestCSVMapping extends CSVRuleSetParser {
        public int getSourceColumn() {
            return 2;
        }
        public int getTargetColumn() {
            return 3;
        }
        public int getDefaultValueColumn() {
            return 4;
        }
    }

    private static class TestCSVMappingWithFiltering extends TestCSVMapping {
        @Override
        protected boolean shouldProcessRow(List<String> row) {
            return row.get(0).equalsIgnoreCase("1");
        }
    }

    private static class TestCSVMappingWithTransform extends TestCSVMapping {
        @Override
        protected List<String> transformRow(List<String> row) {
            String targetKey = row.get(getTargetColumn());
            String transformedKey = targetKey + ".suffix";
            row.set(getTargetColumn(), transformedKey);
            return row;
        }
    }

    private static class TestExcelMapping extends ExcelRuleSetParser {
        public char getDefaultValueColumn() {
            return 'D';
        }
        public char getTypeColumn() {
            return 'E';
        }
    }

    @Test
    public void parseFromCSVFile() {
        String rulesFile = ResourceUtil.absolutePath("csv-rule-set/ruleset.csv");
        RuleSet ruleSetFromCSV = new TestCSVMapping().parseRuleSet(rulesFile);

        String yamlRulesFile = ResourceUtil.absolutePath("csv-rule-set/ruleset.yaml");
        RuleSet ruleSetFromYaml = YamlRuleSetParser.instance().parseRuleSet(yamlRulesFile);

        Assertions.assertThat(ruleSetFromCSV).usingRecursiveAssertion().isEqualTo(ruleSetFromYaml);
    }

    @Test
    public void parseFromCSVFileWithFilter() {
        String rulesFile = ResourceUtil.absolutePath("csv-rule-set/ruleset.csv");
        RuleSet ruleSetFromCSV = new TestCSVMappingWithFiltering().parseRuleSet(rulesFile);

        String yamlRulesFile = ResourceUtil.absolutePath("csv-rule-set/ruleset.yaml");
        RuleSet ruleSetFromYaml = YamlRuleSetParser.instance().parseRuleSet(yamlRulesFile);
        ruleSetFromYaml.getOutput().remove("target.foo.bar");

        Assertions.assertThat(ruleSetFromCSV).usingRecursiveAssertion().isEqualTo(ruleSetFromYaml);
    }

    @Test
    public void parseFromCSVFileWithTransform() {
        String rulesFile = ResourceUtil.absolutePath("csv-rule-set/ruleset.csv");
        RuleSet ruleSetFromCSV = new TestCSVMappingWithTransform().parseRuleSet(rulesFile);

        String yamlRulesFile = ResourceUtil.absolutePath("csv-rule-set/ruleset.yaml");
        RuleSet ruleSetFromYaml = YamlRuleSetParser.instance().parseRuleSet(yamlRulesFile);
        RuleSet transformedRuleSet = RuleSet.builder().output(new LinkedHashMap<>()).build();
        ruleSetFromYaml.getOutput().forEach((k, v) -> transformedRuleSet.getOutput().put(k + ".suffix", v));

        Assertions.assertThat(ruleSetFromCSV).usingRecursiveAssertion().isEqualTo(transformedRuleSet);
    }

    @Test
    public void parseFromExcelFile() {
        String excelRulesFile = ResourceUtil.absolutePath("test-xml-to-json/ruleset.xlsx");
        RuleSet ruleSetFromExcel = new TestExcelMapping().parseRuleSet(excelRulesFile);
        ruleSetFromExcel.setRootInput("ROOT.INFO.PRIMARY");

        String yamlRulesFile = ResourceUtil.absolutePath("test-xml-to-json/ruleset.yaml");
        RuleSet ruleSetFromYaml = MapperUtil.yaml().read(FileUtil.read(yamlRulesFile), RuleSet.class);

        Assertions.assertThat(ruleSetFromExcel).usingRecursiveAssertion().isEqualTo(ruleSetFromYaml);
    }

    @Test
    public void parseFromYamlSchema() {
        String yamlRulesFileNested = ResourceUtil.absolutePath("yaml-rule-set/nested-ruleset.yaml");
        RuleSet ruleSetNested = YamlRuleSetParser.instance().parseRuleSet(yamlRulesFileNested);

        String yamlRulesFileFlat = ResourceUtil.absolutePath("yaml-rule-set/flat-ruleset.yaml");
        RuleSet ruleSetFlat = MapperUtil.yaml().read(FileUtil.read(yamlRulesFileFlat), RuleSet.class);

        Assertions.assertThat(ruleSetNested).usingRecursiveAssertion().isEqualTo(ruleSetFlat);
    }
}
