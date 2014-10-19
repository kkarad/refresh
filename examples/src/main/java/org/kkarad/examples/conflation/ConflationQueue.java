package org.kkarad.examples.conflation;

public interface ConflationQueue<K, V> {
    void put(K key, V value);

    V take();
}
