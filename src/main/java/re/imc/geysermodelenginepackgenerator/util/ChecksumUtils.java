package re.imc.geysermodelenginepackgenerator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChecksumUtils {

    public static String hashDirectory(Path directoryPath, boolean includeHiddenFiles) throws IOException {
        if (!Files.isDirectory(directoryPath))
            throw new IllegalArgumentException("Not a directory");

        Vector<FileInputStream> fileStreams = new Vector<>();
        collectFiles(directoryPath.toFile(), fileStreams, includeHiddenFiles);

        try (SequenceInputStream sequenceInputStream = new SequenceInputStream(fileStreams.elements())) {
            return DigestUtils.md5Hex(sequenceInputStream);
        }
    }

    private static void collectFiles(File directory, List<FileInputStream> fileInputStreams,
                                     boolean includeHiddenFiles) throws IOException {
        File[] files = directory.listFiles();

        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));

            for (File file : files) {
                if (includeHiddenFiles || !Files.isHidden(file.toPath())) {
                    if (file.isDirectory()) {
                        collectFiles(file, fileInputStreams, includeHiddenFiles);
                    } else {
                        fileInputStreams.add(new FileInputStream(file));
                    }
                }
            }
        }
    }
}
