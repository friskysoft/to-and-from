package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.models.RuleSet;

public interface FileRuleSetParser extends RuleSetParser {

    RuleSet parseRuleSet(String filepath);

}
