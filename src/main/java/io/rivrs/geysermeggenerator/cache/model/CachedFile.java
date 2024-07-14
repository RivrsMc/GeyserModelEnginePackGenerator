package io.rivrs.geysermeggenerator.cache.model;

import java.nio.file.Path;

public record CachedFile(Path path, String checksum) {

}
