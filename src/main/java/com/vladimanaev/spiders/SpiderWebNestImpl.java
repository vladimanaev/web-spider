package com.vladimanaev.spiders;

import com.vladimanaev.spiders.logic.SpiderLogic;
import com.vladimanaev.spiders.preparations.SpiderPreparations;
import com.vladimanaev.spiders.model.Results;
import com.vladimanaev.spiders.model.SpiderResult;
import com.vladimanaev.spiders.util.SpidersUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.List;
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
@NotThreadSafe
public class SpiderWebNestImpl<R, P extends AutoCloseable> implements SpiderWebNest<R> {

    private static final Logger LOGGER = Logger.getLogger(SpiderWebNestImpl.class);

    private final ExecutorService nestExecutorService;
    private final Queue<String> urlsQueue;
    private final Set<String> visitedUrls;

    private final int nestSize;
    private final int maxCrawlIterations;
    private final int spiderWorkTimeout;
    private final TimeUnit spiderWorkTimeoutUnit;

    private final SpiderPreparations<String, P> spiderPreparations;
    private final SpiderLogic<String, P, SpiderResult<R>> spiderLogic;

    private Results<R> results;
    private String rootDomainName;

    public SpiderWebNestImpl(int nestSize, int maxCrawlIterations, int spiderWorkTimeout, TimeUnit spiderWorkTimeoutUnit,
                             SpiderPreparations<String, P> spiderPreparations,
                             SpiderLogic<String, P, SpiderResult<R>> spiderLogic) {

        this.results = new Results<>();
        this.nestExecutorService = Executors.newFixedThreadPool(nestSize);
        this.urlsQueue = new ConcurrentLinkedQueue<>();
        this.visitedUrls = new ConcurrentHashSet<>();
        this.nestSize = nestSize;
        this.maxCrawlIterations = maxCrawlIterations;
        this.spiderWorkTimeout = spiderWorkTimeout;
        this.spiderWorkTimeoutUnit = spiderWorkTimeoutUnit;
        this.spiderPreparations = spiderPreparations;
        this.spiderLogic = spiderLogic;
    }

    @Override
    public Results<R> crawl(String rootUrl) throws Exception {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("Crawling root url [" + rootUrl + "]");
        rootDomainName = SpidersUtils.getDomainName(rootUrl);
        if(StringUtils.isEmpty(rootDomainName)) {
            LOGGER.warn("Unable to extract domain from given root URL");
            return results;
        }

        final AtomicInteger crawlIterations = new AtomicInteger(0);
        urlsQueue.add(rootUrl);

        while(!urlsQueue.isEmpty()) {
            LOGGER.info("Crawling stage #" + crawlIterations.get());
            LOGGER.debug("Queue [" + urlsQueue + "]");

            if(crawlIterations.get() == maxCrawlIterations) {
                LOGGER.info("Reached defined crawl iterations [" + maxCrawlIterations + "]");
                break;
            }

            if (nestExecutorService.isShutdown()) {
                LOGGER.info("Received request to shutdown the nest");
                break;
            }

            List<Spider<P, R>> spiders = prepareSpidersForWork();
            sendSpidersToWork(spiders);
            crawlIterations.incrementAndGet();
        }

        LOGGER.info("Done crawling [" + rootUrl + "], took [" + ((System.currentTimeMillis() - startTime) / 1000) + "s]");
        return results;
    }

    private List<Spider<P, R>> prepareSpidersForWork() {
        LOGGER.debug("Preparing spiders for the work");
        List<Spider<P, R>> spiders = new ArrayList<>();
        int i = 0;
        while(i < nestSize) {
            String nextUrl = urlsQueue.poll();
            if(nextUrl == null) {
                break;
            }

            if(visitedUrls.contains(nextUrl)) {
                continue;
            }

            visitedUrls.add(nextUrl);
            LOGGER.info("Creating spider with url [" + nextUrl + "]");
            spiders.add(new Spider<>(rootDomainName, nextUrl, spiderPreparations, spiderLogic));
            i++;
        }

        LOGGER.debug("Prepared [" + spiders.size() + "] spiders for the work");
        return spiders;
    }

    private void sendSpidersToWork(List<Spider<P, R>> spiders) {
        try {
            LOGGER.debug("Sending spiders to work");
            nestExecutorService.invokeAll(spiders).stream().map(future -> {

                try {
                    return future.get(spiderWorkTimeout, spiderWorkTimeoutUnit);
                } catch (Exception e) {
                    throw new IllegalStateException("Failure during future", e);
                }

            }).forEach(result -> {
                result.getNextUrls().stream()
                                .map(curr-> StringUtils.removeEnd(curr, "/"))
                                .forEach(urlsQueue::add);

                R findings = result.getFindings();
                if(findings != null) {
                    results.update(result.getUrl(), findings);
                }
            });

        } catch (Exception e) {
            LOGGER.error("Failed to force spiders to go to work", e);
        }
    }

    @Override
    public void close() throws Exception {
        nestExecutorService.shutdown();
    }
}
