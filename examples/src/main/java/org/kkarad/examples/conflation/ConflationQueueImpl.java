package org.kkarad.examples.conflation;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public final class ConflationQueueImpl<K, V> implements ConflationQueue<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final ArrayBlockingQueue<K> queue = new ArrayBlockingQueue<>(1000);

    public ConflationQueueImpl() {
    }


    @Override
    public void put(K key, V value) {
        try {
            V previousValue = map.put(key, value);
            if (previousValue == null) {
                queue.put(key);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while putting into the queue", e);
        }
    }

    @Override
    public V take() {
        try {
            K key = queue.take();
            return map.get(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while taking from the queue", e);
        }
    }
}
