package org.kkarad.examples.conflation;

public interface ConflationQueue<K, V> {
    void put(V value);

    V take();
}
