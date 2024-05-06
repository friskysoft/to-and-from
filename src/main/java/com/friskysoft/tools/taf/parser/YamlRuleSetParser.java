package com.friskysoft.tools.taf.parser;

import com.friskysoft.tools.taf.models.RuleSet;
import com.friskysoft.tools.taf.utils.FileUtil;
import com.friskysoft.tools.taf.utils.MapperUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlRuleSetParser implements FileRuleSetParser {

    private static YamlRuleSetParser instance;

    private YamlRuleSetParser() {}

    public static synchronized YamlRuleSetParser instance() {
        if (instance == null) {
            instance = new YamlRuleSetParser();
        }
        return instance;

    }

    public RuleSet parseRuleSet(String filepath) {
        final Map<String, Object> originalMap = MapperUtil.yaml().read(FileUtil.read(filepath));
        final Map<String, Object> flatMap = JsonFlattener.flattenAsMap(MapperUtil.json().write(originalMap));

        final Map<String, Object> normalizedFlatMap = new LinkedHashMap<>();
        final Map<String, Map<String, Object>> outputFlatKeyMap = new LinkedHashMap<>();

        flatMap.forEach((key, val) -> {
            if (StringUtils.startsWith(key, "output")) {
                final String parsedKey = RegExUtils.replaceFirst(key, "output\\.", "");
                final String leaf = parsedKey.substring(parsedKey.lastIndexOf('.') + 1);
                final String flatKey = parsedKey.substring(0, parsedKey.lastIndexOf('.'));

                final Map<String, Object> ruleVal = outputFlatKeyMap.getOrDefault(flatKey, new LinkedHashMap<>());

                ruleVal.put(leaf, val);
                outputFlatKeyMap.put(flatKey, ruleVal);
            } else {
                normalizedFlatMap.put(key, val);
            }
        });
        normalizedFlatMap.put("output", outputFlatKeyMap);
        return MapperUtil.json().getJacksonMapper().convertValue(normalizedFlatMap, RuleSet.class);
    }

}
