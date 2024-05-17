package com.friskysoft.tools.taf.utils;

import com.friskysoft.tools.taf.models.ToAndFromException;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

@UtilityClass
public class FileUtil {

    public static boolean exists(String filepath) {
        try {
            Path path = Paths.get(filepath);
            return Files.exists(path);
        } catch (Exception ex) {
            return false;
        }
    }

    public static String read(String filepath) {
        try {
            Path path = Paths.get(filepath);
            return Files.readString(path);
        } catch (Exception ex) {
            throw new ToAndFromException("Failed to read content from file: " + filepath, ex);
        }
    }

    public static void write(String filepath, String content) {
        try {
            Path path = Paths.get(filepath);
            Files.createDirectories(path.getParent());
            Files.write(path, content.getBytes());
        } catch (Exception ex) {
            throw new ToAndFromException("Failed to save content to file: " + filepath, ex);
        }
    }

    public static void deleteDir(String path) {
        Path dir = Path.of(path);
        if (Files.exists(dir)) {
            try (Stream<Path> pathStream = Files.walk(dir)) {
                pathStream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException ex) {
                throw new ToAndFromException("Failed to delete directory: " + path, ex);
            }
        }
    }
}
