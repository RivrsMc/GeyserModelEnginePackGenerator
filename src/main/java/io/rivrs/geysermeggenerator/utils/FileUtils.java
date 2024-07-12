package io.rivrs.geysermeggenerator.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {

    public static String fileNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString().replace(" ", "");
        int index = fileName.lastIndexOf('.');
        return index == -1 ? fileName : fileName.substring(0, index);
    }

    @Nullable
    public static String checksum(Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            return new BigInteger(1, hash).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static void createDirectories(Path... paths) {
        for (Path path : paths) {
            try {
                if (!Files.isDirectory(path))
                    Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void deleteFolder(Path folder) {
        try (Stream<Path> pathStream = Files.walk(folder)) {
            pathStream.sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete file", e);
                        }
                    });
            Files.deleteIfExists(folder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete folder", e);
        }
    }

    public static void deleteFolders(Path... folders) {
        for (Path folder : folders) {
            deleteFolder(folder);
        }
    }
}
