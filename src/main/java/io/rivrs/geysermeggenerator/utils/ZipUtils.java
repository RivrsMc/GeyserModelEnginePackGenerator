package io.rivrs.geysermeggenerator.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ZipUtils {

    /**
     * Zip a folder.
     *
     * @param sourceFolderPath Path of the folder to be zipped
     * @param zipFilePath      Path of the resulting zip file
     * @throws IOException If an I/O error occurs
     */
    public static void zipFolder(Path sourceFolderPath, Path zipFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipFolder(sourceFolderPath, zos, sourceFolderPath);
        }
    }

    private static void zipFolder(Path folder, ZipOutputStream zos, Path rootFolder) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder)) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    zipFolder(path, zos, rootFolder);
                } else {
                    String relativePath = rootFolder.relativize(path).toString().replace("\\", "/");
                    ZipEntry zipEntry = new ZipEntry(relativePath);
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * Unzip an archive.
     *
     * @param zipFilePath Path of the zip file to be unzipped
     * @param destFolderPath Path of the destination folder
     * @throws IOException If an I/O error occurs
     */
    public static void unzip(Path zipFilePath, Path destFolderPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path filePath = destFolderPath.resolve(zipEntry.getName());
                if (!zipEntry.isDirectory()) {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath);
                } else {
                    Files.createDirectories(filePath);
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }
}