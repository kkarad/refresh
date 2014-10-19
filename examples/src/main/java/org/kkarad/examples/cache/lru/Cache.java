package org.kkarad.examples.cache.lru;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class Cache<K, V> {
    private final Loader<K, V> source;
    private final int maxSize;
    private final ConcurrentHashMap<K, CacheEntry<K, V>> map;
    private final LruQueue<K> lruQueue;


    public Cache(Loader<K, V> source, int maxSize) {
        this.source = source;
        this.maxSize = maxSize;
        this.map = new ConcurrentHashMap<K, CacheEntry<K, V>>(maxSize);
        this.lruQueue = new LruQueue<K>();
    }

    public V get(K key) {
        CacheEntry<K, V> cachedEntry = map.get(key);
        if (cachedEntry != null) {
            lruQueue.purgeEmptyNodes();
            return cachedEntry.value;
        }

        //race condition but negligible (no side effect due to putIfAbsent)
        V loadedValue = source.load(key);
        CacheEntry<K, V> entry = new CacheEntry<K, V>(key, loadedValue, null);
        CacheEntry<K, V> existingValue = map.putIfAbsent(key, entry);

        if (existingValue == null) {
            if (evictionThresholdIsExceeded()) {
                evictLruEntry();
            }
            lruQueue.offer(key);
            return loadedValue;
        } else {
            return existingValue.value;
        }
    }

    private boolean evictionThresholdIsExceeded() {
        return map.size() > maxSize;
    }

    private void evictLruEntry() {
        K evictedKey = lruQueue.poll();
        map.remove(evictedKey);
    }

    public static interface Loader<K, V> {
        V load(K key);
    }

    private static class CacheEntry<K, V> {
        private final K key;
        private final V value;
        private final LruNode<K> node;

        private CacheEntry(K key, V value, LruNode<K> node) {
            this.key = key;
            this.value = value;
            this.node = node;
        }
    }

    private static class LruNode<K> {
        private final LruNode previous;
        private final K key;
        private final LruNode next;

        private LruNode(LruNode<K> previous, K key, LruNode<K> next) {
            this.previous = previous;
            this.key = key;
            this.next = next;
        }
    }

    private static class LruQueue<K> {
        private final AtomicReference<LruNode<K>> head;
        private final AtomicReference<LruNode<K>> tail;

        private LruQueue() {
            head = tail = null;
        }

        public K poll() {
            return head.get().key;
        }

        public void offer(K key) {
            LruNode<K> newNode = new LruNode<K>(null, key, null);
            head.set(newNode);
            tail.set(newNode);
        }

        public void purgeEmptyNodes() {

        }
    }
}
