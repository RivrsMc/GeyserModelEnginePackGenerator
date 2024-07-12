package io.rivrs.geysermeggenerator.cache;

import java.util.List;

public record Cache(CacheType type, List<CachedFile> files) {

}
