package org.joget.commons.util;

import java.time.Duration;
import java.util.function.Supplier;
import org.ehcache.expiry.ExpiryPolicy;

public class DynamicExpiryPolicy implements ExpiryPolicy<String, DynamicCacheElement> {

    @Override
    public Duration getExpiryForCreation(String key, DynamicCacheElement value) {
        return Duration.ofSeconds(value.getExpiry());
    }

    @Override
    public Duration getExpiryForAccess(String key, Supplier<? extends DynamicCacheElement> value) {
        return Duration.ofSeconds(value.get().getExpiry());
    }

    @Override
    public Duration getExpiryForUpdate(String key, Supplier<? extends DynamicCacheElement> oldValue, DynamicCacheElement newValue) {
        return Duration.ofSeconds(newValue.getExpiry());
    }

}
