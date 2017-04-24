package com.vladimanaev.spiders.model;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 12:54 AM
 * Copyright VMSR
 */
public class SpiderResult {

    private final String url;
    private final Collection<String> nextUrls;

    public SpiderResult(String url, Collection<String> nextUrls) {
        this.url = url;
        this.nextUrls = nextUrls;
    }

    public String getUrl() {
        return url;
    }

    public Collection<String> getNextUrls() {
        return nextUrls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpiderResult that = (SpiderResult) o;
        return Objects.equals(url, that.url) &&
        Objects.equals(nextUrls, that.nextUrls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, nextUrls);
    }

    @Override
    public String toString() {
        return "WorkerResults{" +
        ", nextUrls=" + nextUrls +
        '}';
    }
}
