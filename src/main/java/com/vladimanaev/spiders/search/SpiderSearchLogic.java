package com.vladimanaev.spiders.search;

/**
 * Created by Vladi
 * Date: 9/18/2016
 * Time: 11:53 PM
 * Copyright VMSR
 */
public interface SpiderSearchLogic<P> {

    void apply(String url, P page);
}
