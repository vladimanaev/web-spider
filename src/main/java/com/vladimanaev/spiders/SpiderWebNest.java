package com.vladimanaev.spiders;

import com.vladimanaev.spiders.model.NestResult;
import com.vladimanaev.spiders.model.SpiderResultsDetails;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 1:40 AM
 * Copyright VMSR
 */
public interface SpiderWebNest<D> extends AutoCloseable {

    NestResult<SpiderResultsDetails<D>> crawl(String url) throws Exception;

}
