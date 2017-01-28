package com.vladimanaev.spiders.model;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 12:54 AM
 * Copyright VMSR
 */
public class SpiderResult<R> {

    private final String url;
    private final R findings;
    private final Collection<String> nextUrls;

    public SpiderResult(String url, R findings, Collection<String> nextUrls) {
        this.url = url;
        this.findings = findings;
        this.nextUrls = nextUrls;
    }

    public String getUrl() {
        return url;
    }

    public R getFindings() {
        return findings;
    }

    public Collection<String> getNextUrls() {
        return nextUrls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpiderResult<?> that = (SpiderResult<?>) o;
        return Objects.equals(url, that.url) &&
        Objects.equals(findings, that.findings) &&
        Objects.equals(nextUrls, that.nextUrls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, findings, nextUrls);
    }

    @Override
    public String toString() {
        return "WorkerResults{" +
        "findings=" + findings +
        ", nextUrls=" + nextUrls +
        '}';
    }
}
