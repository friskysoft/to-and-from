package com.friskysoft.tools.taf.utils;

import com.friskysoft.tools.taf.models.ToAndFromException;
import lombok.experimental.UtilityClass;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@UtilityClass
public class ExcelUtil {

    public static List<List<String>> read(String filepath) {
        return read(filepath, 0);
    }

    public static List<List<String>> read(String filepath, int index) {
        try (FileInputStream file = new FileInputStream(filepath)) {
            return read(file, index);
        } catch (Exception ex) {
            throw new ToAndFromException("Error while reading excel file at: " + filepath, ex);
        }
    }

    public static List<List<String>> read(InputStream file) {
        return read(file, 0);
    }

    public static List<List<String>> read(InputStream file, int index) {
        ArrayList<List<String>> data = new ArrayList<>();

        try (ReadableWorkbook wb = new ReadableWorkbook(file)) {
            Sheet sheet = wb.getSheet(index).orElseThrow(() -> new ToAndFromException("Invalid worksheet index"));
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
            throw new ToAndFromException("Error while reading excel file", ex);
        }

        return data;
    }
}
