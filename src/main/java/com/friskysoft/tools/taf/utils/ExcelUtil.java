package com.friskysoft.tools.taf.utils;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ExcelUtil {

    public static ArrayList<List<String>> read(String filepath) {
        ArrayList<List<String>> data = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(filepath); ReadableWorkbook wb = new ReadableWorkbook(file)) {
            Sheet sheet = wb.getFirstSheet();
            AtomicInteger maxColumns = new AtomicInteger();
            try (Stream<Row> rows = sheet.openStream()) {
                rows.forEach(r -> {
                    List<String> rowCells = new ArrayList<>();
                    data.add(rowCells);
                    for (Cell cell : r) {
                        rowCells.add(cell == null || cell.getRawValue() == null ? "" : cell.getRawValue().trim());
                    }
                    maxColumns.set(Math.max(maxColumns.get(), rowCells.size()));
                });
            }
            data.forEach(rowCells -> {
                while (rowCells.size() < maxColumns.get()) {
                    rowCells.add("");
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Error while reading excel file at: " + filepath, ex);
        }

        return data;
    }
}
