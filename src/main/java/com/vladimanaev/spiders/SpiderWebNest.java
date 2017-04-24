package com.vladimanaev.spiders;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 1:40 AM
 * Copyright VMSR
 */
public interface SpiderWebNest extends AutoCloseable {

    void crawl(String url) throws Exception;

}
