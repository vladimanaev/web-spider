package com.vladimanaev.spiders.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 1:43 AM
 * Copyright VMSR
 */
public class Results<V> {

    private final Map<String, V> results;

    public Results() {
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

        Results results1 = (Results) o;

        if (results != null ? !results.equals(results1.results) : results1.results != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return results != null ? results.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CrawlerResults{" +
        "results=" + results +
        '}';
    }
}
