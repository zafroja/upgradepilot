package io.upgradepilot.scan.analysis;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Everything a rule needs to know about the uploaded project. */
public record ProjectContext(
        Path root,
        List<Path> files,
        List<Path> pomFiles,
        String javaVersion,
        String springBootVersion) {

    public List<Path> javaFiles() {
        return files.stream().filter(p -> p.toString().endsWith(".java")).toList();
    }

    public String relative(Path file) {
        return root.relativize(file).toString().replace('\\', '/');
    }

    public List<String> lines(Path file) {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            try {
                return Files.readAllLines(file, StandardCharsets.ISO_8859_1);
            } catch (IOException ex) {
                return List.of();
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    public String content(Path file) {
        return String.join("\n", lines(file));
    }

    /** Parses "1.8" -> 8, "11" -> 11, "3.2.1" -> 3. Returns -1 if unknown. */
    public static int major(String version, boolean javaStyle) {
        if (version == null || version.isBlank()) return -1;
        String v = version.trim();
        if (javaStyle && v.startsWith("1.")) v = v.substring(2);
        String first = v.split("[.\\-]")[0];
        try {
            return Integer.parseInt(first);
        } catch (NumberFormatException e) {
            return -1; // e.g. an unresolved ${property}
        }
    }
}
