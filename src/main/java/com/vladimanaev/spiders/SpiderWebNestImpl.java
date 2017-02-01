package com.vladimanaev.spiders;

import com.vladimanaev.spiders.logic.SpiderLogic;
import com.vladimanaev.spiders.model.SpiderResultsDetails;
import com.vladimanaev.spiders.model.SpiderWork;
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
    private final Queue<SpiderWork<SpiderResult<SpiderResultsDetails<D>>>> spidersAtWork;
    private final Set<String> visitedUrls;
    private final Set<String> urlsQueueReplica;

    private final int nestSize;
    private final long nestQueenRestMillis;
    private final int maxCrawledURL;
    private final AtomicInteger numOfCrawledURL;

    private final SpiderPreparations<String, P> spiderPreparations;
    private final SpiderLogic<String, P, SpiderResult<SpiderResultsDetails<D>>> spiderLogic;

    private final NestResult<SpiderResultsDetails<D>> nestResult;

    public SpiderWebNestImpl(int nestSize, long nestQueenRestMillis, int maxCrawledURL, SpiderPreparations<String, P> spiderPreparations,
                             SpiderLogic<String, P, SpiderResult<SpiderResultsDetails<D>>> spiderLogic) {

        this.nestResult = new NestResult<>();
        this.nestExecutorService = Executors.newFixedThreadPool(nestSize);
        this.urlsQueue = new ConcurrentLinkedQueue<>();
        this.urlsQueueReplica = new ConcurrentHashSet<>();
        this.nestSize = nestSize;
        this.nestQueenRestMillis = nestQueenRestMillis;
        this.visitedUrls = new ConcurrentHashSet<>();
        this.spidersAtWork = new ConcurrentLinkedQueue<>();
        this.maxCrawledURL = maxCrawledURL;
        this.spiderPreparations = spiderPreparations;
        this.spiderLogic = spiderLogic;
        this.numOfCrawledURL = new AtomicInteger(0);
    }

    @Override
    public NestResult<SpiderResultsDetails<D>> crawl(String rootUrl) throws Exception {
        final long startTime = SpidersUtils.currentTimeMillis();
        numOfCrawledURL.set(0);
        LOGGER.info("Crawling root url [" + rootUrl + "]");
        String rootDomainName = SpidersUtils.getDomainName(rootUrl);
        if(StringUtils.isEmpty(rootDomainName)) {
            LOGGER.warn("Unable to extract domain from given root URL");
            return nestResult;
        }

        nestLoop(rootUrl, rootDomainName);

        LOGGER.info("Done crawling [" + rootUrl + "], took [" + ((SpidersUtils.currentTimeMillis() - startTime) / 1000) + "s]");
        return nestResult;
    }

    private void nestLoop(String rootUrl, String rootDomainName) {
        urlsQueue.add(rootUrl);

        while(!urlsQueue.isEmpty() || !spidersAtWork.isEmpty()) {

            if (nestExecutorService.isShutdown()) {
                LOGGER.info("Received request to shutdown the nest");
                break;
            }

            if(spidersAtWork.isEmpty() && numOfCrawledURL.get() >= maxCrawledURL) {
                LOGGER.info("Nest work is done, halting.");
                break;
            }

            String nextUpURL = getNextUpURL();
            if(nextUpURL != null &&
                wasNotCrawled(nextUpURL) &&
                hasAvailableSpiderWorkers() &&
                didNotCrawleEnough()) {

                visitedUrls.add(nextUpURL);
                addSpiderWork(rootDomainName, nextUpURL);
            }

            nestQueenResting();

            for(SpiderWork<SpiderResult<SpiderResultsDetails<D>>> spiderWork : spidersAtWork) {
                //TODO support timeout for a specific SpiderWork in order to be able to give up on specific URL(Future.Cancel not working properly).

                if(spiderWork.isDone()) {
                    handleDoneWork(spiderWork);
                    removeSpiderWork(spiderWork);
                }
            }
        }
    }

    private String getNextUpURL() {
        String nextUpURL = urlsQueue.poll();
        if(nextUpURL != null) {
            urlsQueueReplica.remove(nextUpURL);
        }
        return nextUpURL;
    }

    private boolean didNotCrawleEnough() {
        return numOfCrawledURL.get() + spidersAtWork.size() < maxCrawledURL;
    }

    private boolean hasAvailableSpiderWorkers() {
        return spidersAtWork.size() < nestSize;
    }

    private boolean wasNotCrawled(String nextUpURL) {
        return !visitedUrls.contains(nextUpURL);
    }

    private void nestQueenResting() {
        try {
            Thread.sleep(nestQueenRestMillis);
        } catch (InterruptedException e) {
            LOGGER.warn("Nest queen failed to rest");
        }
    }

    private void addSpiderWork(String rootDomainName, String nextUpURL) {
        spidersAtWork.add(sendSpiderToWork(rootDomainName, nextUpURL));
    }

    private void removeSpiderWork(SpiderWork<SpiderResult<SpiderResultsDetails<D>>> spiderWork) {
        spidersAtWork.remove(spiderWork);
    }

    private void handleDoneWork(SpiderWork<SpiderResult<SpiderResultsDetails<D>>> spiderWork) {
        LOGGER.debug(String.format("Spider done its work [%s]", spiderWork));
        try {
            SpiderResult<SpiderResultsDetails<D>> futureResult = spiderWork.get();
            if(futureResult != null) {
                for(String potentialNextUrl : futureResult.getNextUrls()) {
                    potentialNextUrl = StringUtils.removeEnd(potentialNextUrl, "/");
                    if(wasNotCrawled(potentialNextUrl) && notInTheQueue(potentialNextUrl)) {
                        urlsQueue.add(potentialNextUrl);
                        urlsQueueReplica.add(potentialNextUrl);
                    }
                }

                SpiderResultsDetails<D> findings = futureResult.getFindings();
                if(findings != null) {
                    nestResult.update(futureResult.getUrl(), findings);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to handle spider's work [%s]", spiderWork), e);
        }

        LOGGER.info("Done crawling URL #" + numOfCrawledURL.get());
        numOfCrawledURL.incrementAndGet();
    }

    private boolean notInTheQueue(String potentialNextUrl) {
        return !urlsQueueReplica.contains(potentialNextUrl);
    }

    private SpiderWork<SpiderResult<SpiderResultsDetails<D>>> sendSpiderToWork(String rootDomainName, String nextUpURL) {
        return new SpiderWork<>(SpidersUtils.currentTimeMillis(), nextUpURL, nestExecutorService.submit(new Spider<>(rootDomainName, nextUpURL, spiderPreparations, spiderLogic)));
    }

    @Override
    public void close() throws Exception {
        nestExecutorService.shutdown();
    }
}
