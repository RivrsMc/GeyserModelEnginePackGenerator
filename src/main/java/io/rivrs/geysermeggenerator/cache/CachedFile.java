package io.rivrs.geysermeggenerator.cache;

import java.nio.file.Path;

public record CachedFile(Path path, String checksum) {

}
