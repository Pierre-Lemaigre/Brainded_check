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
    private static final String resource = "./resource/";

    public static Path createResourceDirectory() throws IOException {
        Path path = Paths.get(resource);
        path = Files.createDirectories(path);
        return path;
    }

    public static List<String> getFilesInDirNoExt() throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(resource), 1)) {
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

    public static List<String> listFilenames() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(resource))) {
            return stream
                    .filter(f -> !Files.isDirectory(f))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .sorted()
                    .toList();
        }
    }

    public static Path getFileFromNumber(int number) throws IOException {
        String subStr = Integer.toString(number);
        return Path.of(resource + listFilenames()
                .stream()
                .filter(f -> f.contains(subStr))
                .findFirst().orElseThrow());
    }
}
