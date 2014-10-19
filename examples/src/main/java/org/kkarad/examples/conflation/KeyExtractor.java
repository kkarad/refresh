package org.kkarad.examples.conflation;

public interface KeyExtractor<K, V> {
    K toKey(V value);
}
