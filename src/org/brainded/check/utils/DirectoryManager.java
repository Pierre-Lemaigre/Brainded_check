package org.brainded.check.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryManager {
    public static Path createResourceDirectory() throws IOException {
        Path path = Paths.get("./resource");
        path = Files.createDirectories(path);
        return path;
    }

    public static List<String> getFilesNameInDirectory(Path path) throws IOException {
        try (Stream<Path> stream = Files.walk(path, 1)) {
            return stream
                    .filter(f -> !Files.isDirectory(f))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(name -> name.split("\\.")[0])
                    .collect(Collectors.toList());
        }
    }

    public static FileWriter createFileInRD(String fileName) throws IOException {
        File file = new File("./resource/" + fileName);
        Files.createFile(file.toPath());
        return new FileWriter(file);
    }
}
