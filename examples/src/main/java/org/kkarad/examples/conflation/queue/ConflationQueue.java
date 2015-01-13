package org.kkarad.examples.conflation.queue;

public interface ConflationQueue<K, V> {
    void put(K key, V value);

    V take();
}
