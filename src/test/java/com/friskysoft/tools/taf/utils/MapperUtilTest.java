package com.friskysoft.tools.taf.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MapperUtilTest {

    @Test
    public void xmlReadListNodes() {
        Map<String, ?> expected = Map.of("list", Map.of("item", List.of(
                Map.of("item", "hello world!", "description", "this is a description", "primaryId", "abcd-1234"),
                Map.of("item", "hello another world", "description", "we are in a multiverse", "primaryId", "efef-9876")
        )));
        String xml = ResourceUtil.readFile("xml-data/xml-with-list.xml");
        Map<String, Object> parsed = MapperUtil.xml().read(xml);
        assertThat(parsed).containsOnlyKeys("list");
        assertThat(parsed).isEqualTo(expected);
    }

    @Test
    public void xmlWriter() {
        Map<String, Object> data = Map.of(
                "hello", Map.of(
                        "world", "blah"
                )
        );
        String xml = MapperUtil.xml().write(data);
        xml = xml.trim()
                .replace("\r\n", "")
                .replace("\n", "");
        assertThat(xml)
                .isEqualTo(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                                "<hello>" +
                                "    <world>blah</world>" +
                                "</hello>"
                );
    }

    @Test
    public void xmlWriterWithPrefix() {
        Map<String, Object> data = Map.of(
                "name1:hello", Map.of(
                        "name2:world", "blah"
                )
        );
        String xml = MapperUtil.xml().write(data);
        xml = xml.trim()
                .replace("\r\n", "")
                .replace("\n", "");
        assertThat(xml)
                .isEqualTo(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                                "<name1:hello>" +
                                "    <name2:world>blah</name2:world>" +
                                "</name1:hello>"
                );
    }

    @Test
    public void xmlReaderWithPrefix() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<name1:hello>" +
                "    <name1:world>blah</name1:world>" +
                "    <name1:foo>" +
                "       <name2:bar>1</name2:bar>" +
                "       <name2:bar>2</name2:bar>" +
                "    </name1:foo>" +
                "</name1:hello>";

        Map<String, Object> expected = Map.of(
                "name1:hello", Map.of(
                        "name1:world", "blah",
                        "name1:foo", Map.of("name2:bar", List.of("1", "2"))
                )
        );

        Map<String, Object> parsed = MapperUtil.xml().read(xml);

        assertThat(parsed).isEqualTo(expected);
    }
}
