package com.vladimanaev.spiders.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 1:43 AM
 * Copyright VMSR
 */
public class NestResult<V> {

    private final Map<String, V> results;

    public NestResult() {
        results = new ConcurrentHashMap<>();
    }

    public V get(String key) {
        return results.get(key);
    }

    public Map<String, V> getAll() {
        return results;
    }

    public void update(String key, V value) {
        results.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NestResult nestResult1 = (NestResult) o;

        if (results != null ? !results.equals(nestResult1.results) : nestResult1.results != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return results != null ? results.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NestResult{" +
        "results=" + results +
        '}';
    }
}
