package com.friskysoft.tools.taf.core;

import com.friskysoft.tools.taf.models.DataFormat;
import com.friskysoft.tools.taf.models.DataType;
import com.friskysoft.tools.taf.models.Rule;
import com.friskysoft.tools.taf.models.RuleSet;
import com.friskysoft.tools.taf.utils.CommonUtil;
import com.friskysoft.tools.taf.utils.DateUtil;
import com.friskysoft.tools.taf.utils.MapperUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Converter {

    private static final Logger logger = LoggerFactory.getLogger(Converter.class);

    public static final String REPORT_PREFIX = "___REPORT___.";
    public static final String REPORT_ABSENT_PREFIX = REPORT_PREFIX + "MAPPED_ABSENT_INPUT";
    public static final String REPORT_UNMAPPED_PREFIX = REPORT_PREFIX + "UNMAPPED_PRESENT_INPUT";
    public static final String REPORT_UNMAPPED_TARGET_PREFIX = REPORT_PREFIX + "UNMAPPED_INPUT";
    public static final String REPORT_DUPLICATE_INPUT_PREFIX = REPORT_PREFIX + "MAPPED_DUPLICATE_INPUT";
    public static final String REPORT_DUPLICATE_OUTPUT_PREFIX = REPORT_PREFIX + "MAPPED_DUPLICATE_OUTPUT";

    public static String convert(final String input,
                                 final DataFormat inputFormat,
                                 final DataFormat outputFormat) {
        final MapperUtil inputMapper = MapperUtil.forFormat(inputFormat);
        final Map<String, Object> inputMap = inputMapper.read(input);
        final MapperUtil outputMapper = MapperUtil.forFormat(outputFormat);
        return outputMapper.write(inputMap);
    }

    public static String convert(final String input,
                                 final RuleSet ruleset) {
        return convert(input, DataFormat.JSON, DataFormat.JSON, ruleset, false);
    }

    public static String convert(final String input,
                                 final DataFormat inputFormat,
                                 final DataFormat outputFormat,
                                 final RuleSet ruleset,
                                 final boolean addReports) {

        final MapperUtil inputMapper = MapperUtil.forFormat(inputFormat);
        final Map<String, Object> inputMap = inputMapper.read(input);
        final String inputAsJson = MapperUtil.json().write(inputMap);
        final Map<String, Object> flatInputMap = JsonFlattener.flattenAsMap(inputAsJson);
        final Set<String> mappedInputKeys = new LinkedHashSet<>();
        final Set<String> absentInputKeys = new LinkedHashSet<>();
        final Map<String, String> allMappedKeys = new LinkedHashMap<>();
        final Map<String, Object> flatOutputMap = new LinkedHashMap<>();

        if (outputFormat == DataFormat.XML && StringUtils.isBlank(ruleset.getRootOutput())) {
            // XML requires a root tag. So make sure there is a unique root tag present. If not, we will assign one.
            final Set<String> rootKeys = ruleset.getOutput().keySet().stream().map(k -> k.split("\\.")[0]).collect(Collectors.toSet());
            if (rootKeys.size() != 1) {
                logger.warn("Unique root XML tag was not present for output. Forcing \"ROOT\" as root tag.");
                ruleset.setRootOutput("ROOT");
            }
        }

        final String outputPrefix = ruleset.getRootOutput() + ".";

        ruleset.getOutput().forEach((key, rule) -> {

            final String outputKey;
            if (StringUtils.isNotBlank(ruleset.getRootOutput()) &&
                    (outputFormat == DataFormat.XML || !key.startsWith(REPORT_PREFIX))) {
                outputKey = outputPrefix + key;
            } else {
                outputKey = key;
            }

            if (StringUtils.isBlank(rule.getFrom()) && StringUtils.isNotBlank(rule.getDefaultValue())) {
                // no source mapped, but default available, so set default value directly
                flatOutputMap.put(outputKey, convertVal(rule, rule.getDefaultValue(), inputMap));
            } else {
                // source is mapped
                final String inputKey = ruleset.getRootInput() + (StringUtils.isBlank(ruleset.getRootInput()) ? "" : ".") + rule.getFrom();
                allMappedKeys.put(inputKey, outputKey);
                if (flatInputMap.containsKey(inputKey)) {
                    mappedInputKeys.add(inputKey);
                    final Object inputVal = flatInputMap.get(inputKey);
                    final Object outputVal = convertVal(rule, inputVal, inputMap);
                    flatOutputMap.put(outputKey, outputVal);
                } else { //TODO: if not array
                    // mapped source key was not present in the input message
                    if (StringUtils.isNotBlank(inputKey) && !inputKey.equalsIgnoreCase("null")) {
                        absentInputKeys.add(inputKey);
                    }
                }
            }
        });

        mappedInputKeys.addAll(resolveArrayMappings(flatInputMap, flatOutputMap, allMappedKeys, absentInputKeys));

        if (addReports) {
            final String reportPrefix = outputFormat == DataFormat.XML ? outputPrefix : "";

            // unmapped source keys present in the input message
            final Set<String> unmappedInputKeys = SetUtils.difference(flatInputMap.keySet(), mappedInputKeys);
            unmappedInputKeys.forEach(inputKey -> flatOutputMap.put(reportPrefix + REPORT_UNMAPPED_PREFIX + "." + inputKey, flatInputMap.get(inputKey)));

            // mapped but absent in input message
            flatOutputMap.put(reportPrefix + REPORT_ABSENT_PREFIX, absentInputKeys);

            // report duplicate mapped keys (often multiple casing)
            flatOutputMap.put(reportPrefix + REPORT_DUPLICATE_INPUT_PREFIX, findDuplicateInputKeys(ruleset));
            flatOutputMap.put(reportPrefix + REPORT_DUPLICATE_OUTPUT_PREFIX, findDuplicateOutputKeys(ruleset));
        } else {
            flatOutputMap.entrySet().removeIf(entry -> entry.getKey().startsWith(REPORT_PREFIX));
        }

        final Map<String, Object> outputMap = JsonUnflattener.unflattenAsMap(flatOutputMap);
        final MapperUtil outputMapper = MapperUtil.forFormat(outputFormat);
        return outputMapper.write(outputMap);
    }

    private static Object convertVal(final Rule rule,
                                     final Object inputVal,
                                     final Map<String, Object> inputMapContext) {

        final String outputVal = resolveValue(String.valueOf(inputVal), inputMapContext);

        if (rule.getType() == DataType.NUMBER) {
            return Long.parseLong(outputVal);
        } else if (rule.getType() == DataType.BOOLEAN) {
            return Boolean.parseBoolean(outputVal);
        } else if (rule.getType() == DataType.DECIMAL) {
            return Double.valueOf(outputVal);
        } else if (DateUtil.DATE_FORMAT_MAP.containsKey(rule.getType())) {
            return DateUtil.convert(outputVal, rule.getType());
        } else {
            return outputVal;
        }
    }

    public static String resolveValue(final String value,
                                      final Map<String, Object> contextMap) {
        // TODO: run templating engine
        String resolvedValue = value;
        return resolvedValue;
    }

    public static Set<String> resolveArrayMappings(final Map<String, Object> flatInputMap,
                                                   final Map<String, Object> flatOutputMap,
                                                   final Map<String, String> keyMap,
                                                   final Set<String> absentKeys) {

        final Set<String> resolvedInputKeys = new LinkedHashSet<>();
        for (Map.Entry<String, Object> inputMapEntry : flatInputMap.entrySet()) {
            final String fullKey = inputMapEntry.getKey();
            final Object inputVal = inputMapEntry.getValue();
            if (!fullKey.contains("[") || !fullKey.contains("]")) {
                continue;
            }
            for (Map.Entry<String, String> keyMapEntry : keyMap.entrySet()) {
                final String inputKey = keyMapEntry.getKey();
                final String outputKey = keyMapEntry.getValue();
                if (!inputKey.contains("[") || !inputKey.contains("]")) {
                    continue;
                }
                if (StringUtils.countMatches(inputKey, "[]") > 1 ||
                        StringUtils.countMatches(outputKey, "[]") > 1) {
                    // for now we support only one to one array conversion. array of array not supported
                    logger.warn("Unsupported array mapping found, will be skipped: {} -> {}", inputKey, outputKey);
                    continue;
                }
                final String pattern = inputKey.replace("[]", "\\[(\\d+)\\]");
                final Matcher matcher = Pattern.compile(pattern).matcher(fullKey);
                if (matcher.matches()) {
                    resolvedInputKeys.add(fullKey);
                    absentKeys.remove(inputKey);
                    final MatchResult result = matcher.toMatchResult();
                    final String index = result.group(1);
                    final String outputKeyIndexed = keyMap.get(inputKey)
                            .replace("[0]", "[" + index + "]")
                            .replace("[]", "[" + index + "]");
                    flatOutputMap.put(outputKeyIndexed, inputVal);
                }
            }
        }
        return resolvedInputKeys;
    }

    public static Set<String> findDuplicateInputKeys(final RuleSet ruleset) {
        final Set<String> inputKeys = new LinkedHashSet<>();
        ruleset.getOutput().values().forEach(rule -> {
            collectKeys(rule.getFrom(), inputKeys);
        });
        return CommonUtil.findDuplicateIgnoreCase(inputKeys);
    }

    public static Set<String> findDuplicateOutputKeys(final RuleSet ruleset) {
        final Set<String> outputKeys = new LinkedHashSet<>();
        ruleset.getOutput().keySet().forEach(key -> {
            collectKeys(key, outputKeys);
        });
        return CommonUtil.findDuplicateIgnoreCase(outputKeys);
    }

    private static void collectKeys(String fullKey, Set<String> inputKeys) {
        if (StringUtils.isNotBlank(fullKey)) {
            String[] keys = fullKey.split("\\.");
            String key = "";
            for (String s : keys) {
                key = key + (key.isEmpty() ? "" : ".") + s;
                inputKeys.add(key);
            }
        }
    }

}
