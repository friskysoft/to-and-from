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
}
