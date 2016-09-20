package com.vladimanaev.spiders.search;


import org.apache.http.annotation.ThreadSafe;

/**
 * Created by Vladi
 * Date: 9/18/2016
 * Time: 11:53 PM
 * Copyright VMSR
 */
@ThreadSafe
public interface SpiderSearchLogic<U, P, R> {

    void apply(U url, P page);

    R results();
}
