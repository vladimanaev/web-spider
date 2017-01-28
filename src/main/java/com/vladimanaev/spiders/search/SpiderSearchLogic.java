package com.vladimanaev.spiders.search;

/**
 * Created by Vladi
 * Date: 9/18/2016
 * Time: 11:53 PM
 * Copyright VMSR
 */
public interface SpiderSearchLogic<U, P, R> {

    void apply(U url, P page);

    R results();

    void reset();
}
