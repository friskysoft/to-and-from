package com.friskysoft.tools.taf.utils;

import com.friskysoft.tools.taf.models.ToAndFromException;
import com.opencsv.CSVReader;
import lombok.experimental.UtilityClass;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class CSVUtil {

    public static List<List<String>> read(Reader reader) {
        final ArrayList<List<String>> data = new ArrayList<>();
        final AtomicInteger maxColumns = new AtomicInteger();

        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                final List<String> rowCells = new ArrayList<>();
                data.add(rowCells);
                for (String cell : values) {
                    rowCells.add(cell == null ? "" : cell.trim());
                }
                maxColumns.set(Math.max(maxColumns.get(), rowCells.size()));
            }
            data.forEach(rowCells -> {
                while (rowCells.size() < maxColumns.get()) {
                    rowCells.add("");
                }
            });
        } catch (Exception ex) {
            throw new ToAndFromException("Error while reading CSV file", ex);
        }
        return data;
    }

    public static List<List<String>> read(InputStream inputStream) {
        return read(new InputStreamReader(inputStream));
    }

    public static List<List<String>> read(String filepath) {
        try (Reader fileReader = new FileReader(filepath)) {
            return read(fileReader);
        } catch (Exception ex) {
            throw new ToAndFromException("Error while reading CSV file at: " + filepath, ex);
        }
    }
}
