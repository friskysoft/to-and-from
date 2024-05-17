package com.friskysoft.tools.taf.utils;

import com.friskysoft.tools.taf.models.DataType;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilTest {

    @AllArgsConstructor
    static class TestCase {
        String input;
        DataType format;
        String expected;
    }

    static List<TestCase> testCases = Arrays.asList(
            new TestCase("10/12/2024", DataType.DATE, "2024-10-12"),
            new TestCase("12/31/2024", DataType.DATE, "2024-12-31"),
            new TestCase("13/12/2024", DataType.DATE, "2024-12-13"),
            new TestCase("19/12/2024", DataType.DATE, "2024-12-19"),
            new TestCase("20/12/2024", DataType.DATE, "2024-12-20"),
            new TestCase("21/12/2024", DataType.DATE, "2024-12-21"),
            new TestCase("22/12/2024", DataType.DATE, "2024-12-22"),
            new TestCase("30/12/2024", DataType.DATE, "2024-12-30"),
            new TestCase("31/12/2024", DataType.DATE, "2024-12-31"),
            new TestCase("31/12/2024", DataType.DATE, "2024-12-31"),
            new TestCase("10/12/2024", DataType.DATE_US, "10/12/2024"),
            new TestCase("13/12/2024", DataType.DATE_US, "12/13/2024"),
            new TestCase("12/13/2024", DataType.DATETIME, "2024-12-13T00:00:00Z"),
            new TestCase("12/13/2024", DataType.DATETIME_SQL, "2024-12-13 00:00:00"),
            new TestCase("12/13/2024", DataType.TIMESTAMP, "1734066000000"),
            new TestCase("2024-12-13T00:00:05Z", DataType.TIMESTAMP, "1734066005000"),
            new TestCase("2024-12-13 00:00:05", DataType.TIMESTAMP, "1734066005000"),
            new TestCase("31-12-2024", DataType.DATE, "2024-12-31"),
            new TestCase("12-31-2024", DataType.DATE, "2024-12-31"),
            new TestCase("2024-12-31", DataType.DATE, "2024-12-31")
    );

    @Test
    public void convert() {
        testCases.forEach(testCase -> {
            String date = DateUtil.convert(testCase.input, testCase.format);
            assertThat(date).as(testCase.input + " converted to " + testCase.format).isEqualTo(testCase.expected);
        });
    }
}
