package com.friskysoft.tools.taf.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class MapperUtilTest {

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
        Assertions.assertThat(xml)
                .isEqualTo(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<hello>" +
                        "    <world>blah</world>" +
                        "</hello>"
        );
    }
}
