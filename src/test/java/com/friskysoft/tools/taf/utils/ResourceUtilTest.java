package com.friskysoft.tools.taf.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceUtilTest {

    @Test
    public void absolutePath() {
        Assertions.assertThat(ResourceUtil.absolutePath("csv-rule-set/ruleset.csv"))
                .isEqualTo(System.getProperty("user.dir") + "/build/resources/test/csv-rule-set/ruleset.csv");

        Assertions.assertThat(ResourceUtil.absolutePath(System.getProperty("user.dir") + "/build/resources/test/csv-rule-set/ruleset.csv"))
                .isEqualTo(System.getProperty("user.dir") + "/build/resources/test/csv-rule-set/ruleset.csv");

        Assertions.assertThat(ResourceUtil.absolutePath("classpath:csv-rule-set/ruleset.csv"))
                .isEqualTo(System.getProperty("user.dir") + "/build/resources/test/csv-rule-set/ruleset.csv");
    }
}
