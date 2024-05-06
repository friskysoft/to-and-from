package com.friskysoft.tools.taf.utils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtil {

    public static String absolutePath(String filepath) {
        if (filepath.startsWith("/")) {
            return filepath;
        }
        if (filepath.contains("file://")) {
            return filepath.replace("file://", "");
        }
        try {
            URL url = ResourceUtil.class.getClassLoader().getResource(filepath);
            Path path = Paths.get(url.toURI());
            return path.toAbsolutePath().toString();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to find file in resources: " + filepath, ex);
        }
    }

    public static String readFile(String filepath) {
        try {
            Path path = Paths.get(absolutePath(filepath));
            return new String(Files.readAllBytes(path));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to read file: " + filepath, ex);
        }
    }
}
