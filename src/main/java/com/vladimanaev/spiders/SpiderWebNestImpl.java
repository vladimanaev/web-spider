package com.vladimanaev.spiders;

import com.vladimanaev.spiders.logic.SpiderLogic;
import com.vladimanaev.spiders.model.SpiderResultsDetails;
import com.vladimanaev.spiders.preparations.SpiderPreparations;
import com.vladimanaev.spiders.model.NestResult;
import com.vladimanaev.spiders.model.SpiderResult;
import com.vladimanaev.spiders.util.SpidersUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Vladi
 * Date: 9/16/2016
 * Time: 1:55 AM
 * Copyright VMSR
 */
public class SpiderWebNestImpl<D, P extends AutoCloseable> implements SpiderWebNest<D> {

    private static final Logger LOGGER = Logger.getLogger(SpiderWebNestImpl.class);

    private final ExecutorService nestExecutorService;
    private final Queue<String> urlsQueue;
    private final Queue<Future<SpiderResult<SpiderResultsDetails<D>>>> spidersAtWork;
    private final Set<String> visitedUrls;

    private final int nestSize;
    private final int maxCrawledURL;
    private final int spiderWorkTimeout;
    private final TimeUnit spiderWorkTimeoutUnit;

    private final SpiderPreparations<String, P> spiderPreparations;
    private final SpiderLogic<String, P, SpiderResult<SpiderResultsDetails<D>>> spiderLogic;

    private final NestResult<SpiderResultsDetails<D>> nestResult;

    //TODO add sleep logic between works
    public SpiderWebNestImpl(int nestSize, int maxCrawledURL, int spiderWorkTimeout, TimeUnit spiderWorkTimeoutUnit,
                             SpiderPreparations<String, P> spiderPreparations,
                             SpiderLogic<String, P, SpiderResult<SpiderResultsDetails<D>>> spiderLogic) {

        this.nestResult = new NestResult<>();
        this.nestExecutorService = Executors.newFixedThreadPool(nestSize);
        this.urlsQueue = new ConcurrentLinkedQueue<>();
        this.nestSize = nestSize;
        this.visitedUrls = new ConcurrentHashSet<>();
        this.spidersAtWork = new ConcurrentLinkedQueue<>();
        this.maxCrawledURL = maxCrawledURL;
        this.spiderWorkTimeout = spiderWorkTimeout;
        this.spiderWorkTimeoutUnit = spiderWorkTimeoutUnit;
        this.spiderPreparations = spiderPreparations;
        this.spiderLogic = spiderLogic;
    }

    @Override
    public NestResult<SpiderResultsDetails<D>> crawl(String rootUrl) throws Exception {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("Crawling root url [" + rootUrl + "]");
        String rootDomainName = SpidersUtils.getDomainName(rootUrl);
        if(StringUtils.isEmpty(rootDomainName)) {
            LOGGER.warn("Unable to extract domain from given root URL");
            return nestResult;
        }

        final AtomicInteger numOfCrawledURL = new AtomicInteger(0);
        urlsQueue.add(rootUrl);

        while(!urlsQueue.isEmpty() || !spidersAtWork.isEmpty()) {

            if (nestExecutorService.isShutdown()) {
                LOGGER.info("Received request to shutdown the nest");
                break;
            }

            String nextUpURL = urlsQueue.poll();
            if(nextUpURL != null && !visitedUrls.contains(nextUpURL) && spidersAtWork.size() < nestSize && numOfCrawledURL.get() < maxCrawledURL) {
                visitedUrls.add(nextUpURL);
                spidersAtWork.add(nestExecutorService.submit(new Spider<>(rootDomainName, nextUpURL, spiderPreparations, spiderLogic)));
            }

            for(Future<SpiderResult<SpiderResultsDetails<D>>> future : spidersAtWork) {

                if(future.isDone()) {
                    SpiderResult<SpiderResultsDetails<D>> futureResult = future.get();
                    if(futureResult != null) {
                        futureResult.getNextUrls().stream().map(curr-> StringUtils.removeEnd(curr, "/")).forEach(urlsQueue::add);
                        SpiderResultsDetails<D> findings = futureResult.getFindings();
                        if(findings != null) {
                            nestResult.update(futureResult.getUrl(), findings);
                        }
                    }

                    LOGGER.info("Done crawling #" + numOfCrawledURL.get());
                    numOfCrawledURL.incrementAndGet();
                    spidersAtWork.remove(future);

                } else if(future.isCancelled()) {
                    spidersAtWork.remove(future);
                }
            }
        }

        interruptSpidersWork();
        LOGGER.info("Done crawling [" + rootUrl + "], took [" + ((System.currentTimeMillis() - startTime) / 1000) + "s]");
        return nestResult;
    }

    private void interruptSpidersWork() {
        spidersAtWork.forEach(spider -> {
            LOGGER.info("Interrupting " + spider.toString());
            spider.cancel(true);
        });
    }

    @Override
    public void close() throws Exception {
        nestExecutorService.shutdown();
    }
}
