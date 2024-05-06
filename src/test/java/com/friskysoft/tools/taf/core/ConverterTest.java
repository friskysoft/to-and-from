package com.friskysoft.tools.taf.core;

import com.friskysoft.tools.taf.models.Rule;
import com.friskysoft.tools.taf.models.RuleSet;
import com.friskysoft.tools.taf.parser.YamlRuleSetParser;
import com.friskysoft.tools.taf.utils.FileUtil;
import com.friskysoft.tools.taf.utils.MapperUtil;
import com.friskysoft.tools.taf.models.DataFormat;
import com.friskysoft.tools.taf.utils.ResourceUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.json.JSONException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ConverterTest {

    @Test
    public void testResolveArrayMappings() {
        Map<String, Object> inputMap = Map.of(
                "a", Map.of(
                        "b", List.of("x", "y", "z")
                )
        );
        String inputAsJson = MapperUtil.json().write(inputMap);
        Map<String, Object> flatInputMap = JsonFlattener.flattenAsMap(inputAsJson);
        Map<String, Object> flatOutputMap = new LinkedHashMap<>();
        Map<String, String> keyMaps = Map.of(
                "a.b[]", "hello.world.mylist[].item"
        );
        Set<String> resolved = Converter.resolveArrayMappings(flatInputMap, flatOutputMap, keyMaps, new HashSet<>());
        assertThat(resolved).containsExactlyInAnyOrder("a.b[0]", "a.b[2]", "a.b[1]");
        assertThat(flatOutputMap).containsExactlyInAnyOrderEntriesOf(Map.of(
                "hello.world.mylist[0].item", "x",
                "hello.world.mylist[1].item", "y",
                "hello.world.mylist[2].item", "z"
        ));
    }

    @Test
    @Disabled
    public void testResolveScalarToArrayMappings() {
        Map<String, Object> inputMap = Map.of(
                "field", Map.of(
                        "a", "x",
                        "b", "y"
                )
        );
        String inputAsJson = MapperUtil.json().write(inputMap);
        Map<String, Object> flatInputMap = JsonFlattener.flattenAsMap(inputAsJson);
        Map<String, Object> flatOutputMap = new LinkedHashMap<>();
        Map<String, String> keyMaps = Map.of(
                "field.a", "hello.world.mylist[0].itemKey",
                "field.b", "hello.world.mylist[0].itemVal"
        );
        Set<String> resolved = Converter.resolveArrayMappings(flatInputMap, flatOutputMap, keyMaps, new HashSet<>());
        assertThat(resolved).containsExactlyInAnyOrder("field.a", "field.b");
        assertThat(flatOutputMap).containsExactlyInAnyOrderEntriesOf(Map.of(
                "hello.world.mylist[0].itemKey", "x",
                "hello.world.mylist[0].itemVal", "y"
        ));
    }

    @Test
    @Disabled
    public void convertScalarToArrayMappings() {
        Map<String, Object> source = Map.of(
                "field", Map.of(
                        "a", "x",
                        "b", "11",
                        "c", "y",
                        "d", "22"
                )
        );
        String sourceJson = MapperUtil.json().write(source);
        RuleSet ruleSet = RuleSet.builder()
                .output(Map.of(
                        "hello.world.mylist[0].itemKey", Rule.builder().from("field.a").build(),
                        "hello.world.mylist[0].itemVal", Rule.builder().from("field.b").build(),
                        "hello.world.mylist[1].itemKey", Rule.builder().from("field.not_present").build(),
                        "hello.world.mylist[1].itemVal", Rule.builder().from("field.nope_nada").build(),
                        "hello.world.mylist[2].itemKey", Rule.builder().from("field.c").build(),
                        "hello.world.mylist[2].itemVal", Rule.builder().from("field.d").build()
                ))
                .build();
        String outputJson = Converter.convert(sourceJson, ruleSet);
        assertThat(outputJson).isNotBlank();
        String outputXML = Converter.convert(outputJson, DataFormat.JSON, DataFormat.XML);
        assertThat(outputXML).isNotBlank();
        assertThat(outputXML.trim()).isEqualTo(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<hello>\n" +
                "    <world>\n" +
                "        <mylist>\n" +
                "            <itemVal>11</itemVal>\n" +
                "            <itemKey>x</itemKey>\n" +
                "        </mylist>\n" +
                "        <--mylist/-->\n" + //FIXME: ignore null list items
                "        <mylist>\n" +
                "            <itemVal>22</itemVal>\n" +
                "            <itemKey>y</itemKey>\n" +
                "        </mylist>\n" +
                "    </world>\n" +
                "</hello>");
    }

    @Test
    public void testXmlToJson() throws JSONException {
        String yaml = ResourceUtil.readFile("test-xml-to-json/ruleset2.yaml");
        RuleSet ruleset = MapperUtil.yaml().read(yaml, RuleSet.class);

        String xmlInput = ResourceUtil.readFile("test-xml-to-json/input.xml");

        String output = Converter.convert(xmlInput, DataFormat.XML, DataFormat.JSON, ruleset, true);
        System.out.println(output);
        Map<String, Object> expectedMap = Map.of(
                "metadata", Map.of(
                        "primaryId", "abcd1234",
                        "name", "hello world!",
                        "description", "this is a default description",
                        "item", Map.of("count", 99)
                ),
                "items", List.of("a", "b"),
                "___REPORT___", Map.of(
                        "ABSENT_INPUT_TAGS", Set.of("ROOT.INFO.ABSENT", "ROOT.INFO.ABSENT_ARRAY.ITEM[]"),
                        "UNMAPPED_INPUT_TAGS", Map.of(
                                "ROOT", Map.of(
                                        "INFO", Map.of(
                                                "schema", "schema.xsd",
                                                "PRIMARY", Map.of(
                                                        "ANOTHER_ID", "999-xyz-123"
                                                ),
                                                "SECONDARY", Map.of(
                                                        "LOG", "something"
                                                )
                                        )
                                )
                        ),
                        "MAPPED_DUPLICATE_INPUT_TAGS", Set.of(),
                        "MAPPED_DUPLICATE_OUTPUT_TAGS", Set.of()
                )
        );
        String expected = MapperUtil.json().write(expectedMap);
        JSONAssert.assertEquals(expected, output, false);

        // without additional reports
        String outputNoReports = Converter.convert(xmlInput, DataFormat.XML, DataFormat.JSON, ruleset, false);
        System.out.println(outputNoReports);

        Map<String, Object> expectedMap2 = Map.of(
                "metadata", expectedMap.get("metadata"),
                "items", expectedMap.get("items")
        );
        String expected2 = MapperUtil.json().write(expectedMap2);
        JSONAssert.assertEquals(expected2, outputNoReports, true);
    }

    @Test
    public void testJsonToJson() throws JSONException {
        String rulesetPath = ResourceUtil.absolutePath("test-json-to-json/ruleset.yaml");
        RuleSet ruleset = YamlRuleSetParser.instance().parseRuleSet(rulesetPath);

        String jsonInput = ResourceUtil.readFile("test-json-to-json/input.json");

        String output = Converter.convert(jsonInput, DataFormat.JSON, DataFormat.JSON, ruleset, true);
        System.out.println(output);
        String expectedOutput = ResourceUtil.readFile("test-json-to-json/expected-output-with-report.json");
        JSONAssert.assertEquals(expectedOutput, output, false);

        // without additional reports
        String outputNoReports = Converter.convert(jsonInput, DataFormat.JSON, DataFormat.JSON, ruleset, false);
        System.out.println(outputNoReports);

        String expectedOutputNoReport = ResourceUtil.readFile("test-json-to-json/expected-output-no-report.json");
        JSONAssert.assertEquals(expectedOutputNoReport, outputNoReports, true);
    }

    @Test
    public void testBankAllCases() throws JSONException {
        String bankPath = ResourceUtil.absolutePath("test-bank");
        File[] directories = new File(bankPath).listFiles(File::isDirectory);

        for (File dir : directories) {
            RuleSet ruleset = YamlRuleSetParser.instance().parseRuleSet(dir.getAbsolutePath() + "/ruleset.yaml");
            System.out.println("--- RULESET ---");
            System.out.println(MapperUtil.yaml().write(ruleset));

            String input;
            String output;
            if (FileUtil.exists(dir.getAbsolutePath() + "/input.xml")) {
                input = ResourceUtil.readFile(dir.getAbsolutePath() + "/input.xml");
                output = Converter.convert(input, DataFormat.XML, DataFormat.JSON, ruleset, false);
            } else {
                input = ResourceUtil.readFile(dir.getAbsolutePath() + "/input.json");
                output = Converter.convert(input, DataFormat.JSON, DataFormat.JSON, ruleset, false);
            }
            System.out.println("--- INPUT ---");
            System.out.println(input);
            System.out.println("--- CONVERTED OUTPUT ---");
            System.out.println(output);
            String expectedOutput = ResourceUtil.readFile(dir.getAbsolutePath() + "/output.json");
            System.out.println("--- EXPECTED OUTPUT ---");
            System.out.println(expectedOutput);
            JSONAssert.assertEquals(expectedOutput, output, true);
        }
    }
}
