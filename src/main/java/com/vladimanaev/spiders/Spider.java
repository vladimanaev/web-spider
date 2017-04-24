package com.vladimanaev.spiders;

import com.vladimanaev.spiders.logic.SpiderLogic;
import com.vladimanaev.spiders.model.SpiderResult;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 2:04 AM
 * Copyright VMSR
 */
public class Spider implements Callable<SpiderResult> {

    private static final Logger LOGGER = Logger.getLogger(Spider.class);

    private final String url;
    private final String rootDomainName;
    private final SpiderLogic logic;

    public Spider(String rootDomainName,
                  String url, SpiderLogic logic) {

        this.url = url;
        this.rootDomainName = rootDomainName;
        this.logic = logic;
    }

    @Override
    public SpiderResult call() throws Exception {
        LOGGER.debug("Spider starting work with url [" + url + "]");
        return logic.execute(rootDomainName, url);
    }
}
