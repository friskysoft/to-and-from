package com.friskysoft.tools.taf.utils;

import com.friskysoft.tools.taf.models.ToAndFromException;
import lombok.experimental.UtilityClass;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class ResourceUtil {

    public static final String CLASSPATH_PREFIX = "classpath:";
    public static final String FILE_SCHEME_PREFIX = "file://";

    public static String resourcePath(String filepath) {
        if (filepath.startsWith("/")) {
            return filepath;
        }
        if (filepath.contains(FILE_SCHEME_PREFIX)) {
            return filepath.replace(FILE_SCHEME_PREFIX, "");
        }
        if (filepath.startsWith(CLASSPATH_PREFIX)) {
            return filepath;
        }
        return CLASSPATH_PREFIX + filepath;
    }

    public static String absolutePath(String filepath) {
        if (filepath.startsWith("/")) {
            return filepath;
        }
        if (filepath.contains(FILE_SCHEME_PREFIX)) {
            return filepath.replace(FILE_SCHEME_PREFIX, "");
        }
        if (filepath.startsWith(CLASSPATH_PREFIX)) {
            filepath = filepath.replaceFirst(CLASSPATH_PREFIX, "");
        }
        try {
            URL url = ResourceUtil.class.getClassLoader().getResource(filepath);
            Path path = Paths.get(url.toURI());
            return path.toAbsolutePath().toString();
        } catch (Exception ex) {
            throw new ToAndFromException("Unable to find file in resources: " + filepath, ex);
        }
    }

    public static String readFile(String filepath) {
        String resourcePath = resourcePath(filepath);
        try {
            if (resourcePath.startsWith(CLASSPATH_PREFIX)) {
                resourcePath = resourcePath.replaceFirst(CLASSPATH_PREFIX, "");
                try (InputStream inputStream = ResourceUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    assert inputStream != null;
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                return FileUtil.read(resourcePath);
            }
        } catch (Exception ex) {
            throw new ToAndFromException("Unable to read file: " + filepath, ex);
        }
    }
}
