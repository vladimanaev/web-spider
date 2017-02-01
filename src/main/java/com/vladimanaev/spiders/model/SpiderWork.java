package com.vladimanaev.spiders.model;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Vladi
 * Date: 1/28/2017
 * Time: 2:56 AM
 * Copyright VMSR
 */
public class SpiderWork<D extends SpiderResult> {

    private final long startTimeMillis;
    private final String url;
    private final Future<D> result;

    public SpiderWork(long startTimeMillis, String url, Future<D> result) {
        this.result = result;
        this.url = url;
        this.startTimeMillis = startTimeMillis;
    }

    public boolean isDone() {
        return result.isDone();
    }

    public boolean isCancelled() {
        return result.isCancelled();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return result.cancel(mayInterruptIfRunning);
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public D get() throws ExecutionException, InterruptedException {
        return result.get();
    }

    public D get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result.get(timeout, unit);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpiderWork<?> spiderWork = (SpiderWork<?>) o;
        return startTimeMillis == spiderWork.startTimeMillis &&
        Objects.equals(url, spiderWork.url) &&
        Objects.equals(result, spiderWork.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTimeMillis, url, result);
    }

    @Override
    public String toString() {
        return "SpiderWork{" +
        "startTimeMillis=" + startTimeMillis +
        ", url='" + url + '\'' +
        ", result=" + result +
        '}';
    }
}
