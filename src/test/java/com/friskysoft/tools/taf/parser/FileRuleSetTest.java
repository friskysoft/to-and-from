package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.models.RuleSet;
import com.friskysoft.tools.taf.utils.FileUtil;
import com.friskysoft.tools.taf.utils.MapperUtil;
import com.friskysoft.tools.taf.utils.ResourceUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileRuleSetTest {

    private static class TestMapping extends ExcelRuleSetParser {
        public char getDefaultValueColumn() {
            return 'D';
        }

        public char getTypeColumn() {
            return 'E';
        }
    }

    @Test
    public void parseFromFile() {
        String excelRulesFile = ResourceUtil.absolutePath("test-xml-to-json/ruleset.xlsx");
        RuleSet ruleSetFromExcel = new TestMapping().parseRuleSet(excelRulesFile);
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
