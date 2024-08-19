package org.owasp.csrfguard.token.storage.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.owasp.csrfguard.token.storage.Token;
import org.apache.commons.lang3.tuple.Pair;

public class SessionToken implements Token, Serializable {

    private String masterToken;
    private Map<String, PageTokenValue> pageTokens = new ConcurrentHashMap<>();

    @Override
    public String getMasterToken() {
        return this.masterToken;
    }

    @Override
    public void setMasterToken(final String masterToken) {
        this.masterToken = masterToken;
    }

    @Override
    public String getPageToken(final String uri) {
        return this.pageTokens.get(uri).getValue();
    }

    @Override
    public PageTokenValue getTimedPageToken(final String uri) {
        return this.pageTokens.get(uri);
    }

    @Override
    public void setPageToken(final String uri, final String pageToken) {
        this.pageTokens.put(uri, PageTokenValue.from(pageToken));
    }

    @Override
    public String setPageTokenIfAbsent(final String uri, final Supplier<String> valueSupplier) {
        return this.pageTokens.computeIfAbsent(uri, k -> PageTokenValue.from(valueSupplier.get())).getValue();
    }

    @Override
    public Map<String, String> getPageTokens() {
        return this.pageTokens.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                                                                            e -> e.getValue().getValue()));
    }

    @Override
    public void setPageTokens(final Map<String, String> pageTokens) {
        this.pageTokens = pageTokens.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                                              e -> PageTokenValue.from(e.getValue()),
                                                              (e1, e2) -> e2,
                                                              ConcurrentHashMap::new
                                                             ));
    }

    @Override
    public void rotateAllPageTokens(final Supplier<String> tokenValueSupplier) {
        this.pageTokens.entrySet().forEach(e -> e.setValue(PageTokenValue.from(tokenValueSupplier.get())));
    }

    @Override
    public void regenerateUsedPageToken(final String tokenFromRequest, final Supplier<String> tokenValueSupplier) {
        this.pageTokens.replaceAll((k, v) -> v.getValue().equals(tokenFromRequest) ? PageTokenValue.from(tokenValueSupplier.get()) : v);
    }

    private static Map<String, PageTokenValue> toMap(final Pair<String, String> pageToken) {
        final Map<String, PageTokenValue> pageTokens = new ConcurrentHashMap<>();
        pageTokens.put(pageToken.getKey(), PageTokenValue.from(pageToken.getValue()));
        return pageTokens;
    }
}
