package com.vladimanaev.spiders;

import com.vladimanaev.spiders.model.Results;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 1:40 AM
 * Copyright VMSR
 */
public interface SpiderWebNest<R, M> extends AutoCloseable {

    Results<R> crawl(String url) throws Exception;

}
