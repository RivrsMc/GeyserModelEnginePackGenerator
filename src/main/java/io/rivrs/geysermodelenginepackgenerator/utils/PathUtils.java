package io.rivrs.geysermodelenginepackgenerator.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtils {

    public static String extension(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index == -1)
            return "";
        return fileName.substring(index + 1).toLowerCase();
    }

    public static boolean onlyContainsFolders(Path path) {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.allMatch(Files::isDirectory);
        } catch (IOException e) {
            return false;
        }
    }
}
