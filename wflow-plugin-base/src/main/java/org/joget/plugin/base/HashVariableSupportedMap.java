package org.joget.plugin.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class HashVariableSupportedMap<K,V> extends HashMap<K,V> {
    protected Map<K,V> initialMap = new HashMap<K,V>();
    
    public HashVariableSupportedMap(Map<K,V> initialValues) {
        if (initialValues != null) {
            initialMap = initialValues;
        }
    }
    
    @Override
    public int size() {
        return super.size() + initialMap.size();
    }
    
    @Override
    public boolean isEmpty() {
        return super.isEmpty() && initialMap.isEmpty();
    }
    
    protected abstract Object getProcessedValue(V value);
    
    @Override
    public V get(Object key) {
        if (super.containsKey(key)) {
            return super.get(key);
        } else if (initialMap.containsKey(key)) {
            V value = initialMap.remove(key);
            if (value != null) {
                Object newValue = getProcessedValue(value);
                put((K) key, (V) newValue);
                return (V) newValue;
            }
        }
        return null;
    }
    
    @Override
    public V put(K key, V value) {
        if (initialMap.containsKey(key)) {
            initialMap.remove(key);
        }
        return super.put(key, value);
    }
    
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key) || initialMap.containsKey(key);
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m instanceof HashVariableSupportedMap) {
            initialMap.putAll(((HashVariableSupportedMap) m).initialMap);
        }
        super.putAll(m);
    }
    
    @Override
    public V remove(Object key) {
        if (super.containsKey(key)) {
            return super.remove(key);
        } else if (initialMap.containsKey(key)) {
            return initialMap.remove(key);
        }
        return null;
    }
    
    @Override
    public void clear() {
        super.clear();
        initialMap.clear();
    }
    
    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(value) || initialMap.containsValue(value);
    }
    
    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<K>();
        keySet.addAll(initialMap.keySet());
        keySet.addAll(super.keySet());
        return keySet;
    }
    
    protected void processAllValues() {
        if (!initialMap.isEmpty()) {
            for (K key : initialMap.keySet()) {
                V value = initialMap.get(key);
                if (value != null) {
                    Object newValue = getProcessedValue(value);
                    super.put((K) key, (V) newValue);
                }
            }
            initialMap.clear();
        }
    }
    
    @Override
    public Collection<V> values() {
        processAllValues();
        return super.values();
    }
    
    protected Boolean isInternal = null;

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        if (isInternal == null) {
            int i = 0;
            for (StackTraceElement elem : new Throwable().getStackTrace()) {
                if (i++ < 2) {
                    continue;
                }
                String className = elem.getClassName();
                if (className.equalsIgnoreCase("org.joget.plugin.base.HashVariableSupportedMap")) {
                    isInternal = true;
                    break;
                }
                if (i == 5) {
                    isInternal = false;
                    break;
                }
            }
            if (!isInternal) {
                processAllValues();
            }
        }
        return super.entrySet();
    }
    
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = get(key);
        return (value != null)?value:defaultValue;
    }
    
    @Override
    public V putIfAbsent(K key, V value) {
        V preValue = get(key);
        if (preValue == null) {
            put(key, value);
        }
        return preValue;
    }
    
    @Override
    public boolean remove(Object key, Object value) {
        V preValue = get(key);
        if (preValue != null && preValue.equals(value)) {
            remove(key);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        V preValue = get(key);
        if (preValue != null && preValue.equals(oldValue)) {
            put(key, newValue);
            return true;
        }
        return false;
    }
    
    @Override
    public V replace(K key, V value) {
        V preValue = get(key);
        if (preValue != null) {
            put(key, value);
            return preValue;
        }
        return null;
    }
}
