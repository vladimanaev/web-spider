package com.vladimanaev.spiders.model;

import java.util.*;

/**
 * Created by Vladi
 * Date: 1/24/2017
 * Time: 11:29 PM
 * Copyright VMSR
 */
public class SpiderResultsDetails<R> {

    private final Collection<R> details;

    public SpiderResultsDetails() {
        details = new LinkedList<>();
    }

    public Collection<R> getDetails() {
        return details;
    }

    public boolean isEmpty() {
        return details.isEmpty();
    }

    public void add(R detail) {
        details.add(detail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpiderResultsDetails<?> that = (SpiderResultsDetails<?>) o;
        return Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(details);
    }

    @Override
    public String toString() {
        return "SpiderResultsDetails{" +
        "details=" + details +
        '}';
    }
}
