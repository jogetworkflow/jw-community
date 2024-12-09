package net.sf.ehcache;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;

/**
 * This is a wrapper class to support backward compatible on cache implementation
 */
public class Cache implements javax.cache.Cache {
    private javax.cache.Cache cache;
    
    public Cache(javax.cache.Cache cache) {
        this.cache = cache;
    } 
    
    //Backward compatible method start
    
    public Element get(Serializable k) {
        return (Element) get((Object) k);
    }

    public void put(Element k) {
        cache.put(k.getKey(), k);
    }
    
    public boolean remove(Serializable key) {
        return cache.remove((Object) key);
    }
    
    //Backward compatible method end
    
    @Override
    public Object get(Object k) {
        return cache.get(k);
    }

    @Override
    public Map getAll(Set set) {
        return cache.getAll(set);
    }

    @Override
    public boolean containsKey(Object k) {
        return cache.containsKey(k);
    }

    @Override
    public void loadAll(Set set, boolean bln, CompletionListener cl) {
        cache.loadAll(set, bln, cl);
    }

    @Override
    public void put(Object k, Object v) {
        cache.put(k, v);
    }

    @Override
    public Object getAndPut(Object k, Object v) {
        return cache.getAndPut(k, v);
    }

    @Override
    public void putAll(Map map) {
        cache.putAll(map);
    }

    @Override
    public boolean putIfAbsent(Object k, Object v) {
        return cache.putIfAbsent(k, v);
    }

    @Override
    public boolean remove(Object k) {
        return cache.remove(k);
    }

    @Override
    public boolean remove(Object k, Object v) {
        return cache.remove(k, v);
    }

    @Override
    public Object getAndRemove(Object k) {
        return cache.getAndRemove(k);
    }

    @Override
    public boolean replace(Object k, Object v, Object v1) {
        return cache.replace(k, v, v1);
    }

    @Override
    public boolean replace(Object k, Object v) {
        return cache.replace(k, v);
    }

    @Override
    public Object getAndReplace(Object k, Object v) {
        return cache.getAndReplace(k, v);
    }

    @Override
    public void removeAll(Set set) {
        cache.removeAll(set);
    }

    @Override
    public void removeAll() {
        cache.removeAll();
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Configuration getConfiguration(Class type) {
        return cache.getConfiguration(type);
    }

    @Override
    public Object invoke(Object k, EntryProcessor ep, Object... os) throws EntryProcessorException {
        return cache.invoke(k, ep, os);
    }

    @Override
    public Map invokeAll(Set set, EntryProcessor ep, Object... os) {
        return cache.invokeAll(set, ep, os);
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public CacheManager getCacheManager() {
        return cache.getCacheManager();
    }

    @Override
    public void close() {
        cache.close();
    }

    @Override
    public boolean isClosed() {
        return cache.isClosed();
    }

    @Override
    public Object unwrap(Class type) {
        return cache.unwrap(type);
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration celc) {
        cache.registerCacheEntryListener(celc);
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration celc) {
        cache.deregisterCacheEntryListener(celc);
    }

    @Override
    public Iterator iterator() {
        return cache.iterator();
    }

    @Override
    public void forEach(Consumer action) {
        cache.forEach(action);
    }

    @Override
    public Spliterator spliterator() {
        return cache.spliterator();
    }
    
}
