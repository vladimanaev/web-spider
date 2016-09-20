package com.vladimanaev.spiders;

import com.vladimanaev.spiders.logic.SpiderLogic;
import com.vladimanaev.spiders.preparations.SpiderPreparations;
import com.vladimanaev.spiders.model.SpiderResult;
import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 2:04 AM
 * Copyright VMSR
 */
@ThreadSafe
public class Spider<P extends AutoCloseable, R> implements Callable<SpiderResult<R>> {

    private static final Logger LOGGER = Logger.getLogger(Spider.class);

    private final String url;
    private final String rootDomainName;
    private final SpiderPreparations<String, P> preparation;
    private final SpiderLogic<String, P, SpiderResult<R>> logic;

    public Spider(String rootDomainName,
                  String url,
                  SpiderPreparations<String, P> preparation,
                  SpiderLogic<String, P, SpiderResult<R>> logic) {

        this.url = url;
        this.rootDomainName = rootDomainName;
        this.preparation = preparation;
        this.logic = logic;
    }

    @Override
    public SpiderResult<R> call() throws Exception {
        final long startTime = System.currentTimeMillis();
        LOGGER.debug("Spider starting to work");
        P preparationResult = preparation.execute(url);
        try {
            return logic.execute(rootDomainName, url, preparationResult);
        } finally {
            LOGGER.debug("Spider done working over [" + url + "], took [" + ((System.currentTimeMillis() - startTime)/1000) + "s]");
            preparationResult.close();
        }
    }
}
