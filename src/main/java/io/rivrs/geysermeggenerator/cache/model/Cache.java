package io.rivrs.geysermeggenerator.cache.model;

import java.util.List;

import io.rivrs.geysermeggenerator.cache.CacheType;

public record Cache(CacheType type, List<CachedFile> files) {

}
