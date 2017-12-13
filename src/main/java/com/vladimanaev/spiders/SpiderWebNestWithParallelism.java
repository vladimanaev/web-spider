package com.vladimanaev.spiders;

import com.vladimanaev.spiders.logic.HtmlUnitSpiderLogic;
import com.vladimanaev.spiders.logic.SpiderLogic;
import com.vladimanaev.spiders.model.SpiderWork;
import com.vladimanaev.spiders.model.SpiderResult;
import com.vladimanaev.spiders.search.HtmlUnitSpiderSearchLogic;
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
public class SpiderWebNestWithParallelism implements SpiderWebNest {

    private static final Logger LOGGER = Logger.getLogger(SpiderWebNestWithParallelism.class);

    private final ExecutorService nestExecutorService;
    private final Queue<String> urlsQueue;
    private final Queue<SpiderWork<SpiderResult>> spidersAtWork;
    private final Set<Integer> visitedUrls;
    private final Set<Integer> urlsQueueReplica;

    private final int nestSize;
    private final long nestQueenRestMillis;
    private final int numOfMaxCrawledURL;
    private final AtomicInteger numOfCrawledURL;

    private final SpiderLogic spiderLogic;

    /**
     * Use builder to construct spiders nest
     */
    private SpiderWebNestWithParallelism(int nestSize, long nestQueenRestMillis,
                                         int numOfMaxCrawledURL, SpiderLogic spiderLogic) {

        this.nestExecutorService = Executors.newFixedThreadPool(nestSize);
        this.urlsQueue = new ConcurrentLinkedQueue<>();
        this.urlsQueueReplica = new ConcurrentHashSet<>();
        this.nestSize = nestSize;
        this.nestQueenRestMillis = nestQueenRestMillis;
        this.visitedUrls = new ConcurrentHashSet<>();
        this.spidersAtWork = new ConcurrentLinkedQueue<>();
        this.numOfMaxCrawledURL = numOfMaxCrawledURL;
        this.spiderLogic = spiderLogic;
        this.numOfCrawledURL = new AtomicInteger(0);
    }

    @Override
    public void crawl(String rootUrl) throws Exception {
        String originalThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(originalThreadName + " | Queen Nest");
        try {
            final long startTime = SpidersUtils.currentTimeMillis();
            numOfCrawledURL.set(0);
            LOGGER.info("Crawling root url [" + rootUrl + "]");
            String rootDomainName = SpidersUtils.getDomainName(rootUrl);
            if (StringUtils.isEmpty(rootDomainName)) {
                LOGGER.warn("Unable to extract domain from given root URL");
                return;
            }

            nestLoop(rootUrl, rootDomainName);

            LOGGER.info("Done crawling [" + rootUrl + "], took [" + ((SpidersUtils.currentTimeMillis() - startTime) / 1000) + "s]");
        } finally {
            Thread.currentThread().setName(originalThreadName);
            nestExecutorService.shutdownNow();
        }
    }

    private void nestLoop(String rootUrl, String rootDomainName) {
        urlsQueue.add(rootUrl);

        while(!urlsQueue.isEmpty() || !spidersAtWork.isEmpty()) {

            if (nestExecutorService.isShutdown()) {
                LOGGER.info("Received request to shutdown the nest");
                break;
            }

            if(spidersAtWork.isEmpty() && numOfCrawledURL.get() >= numOfMaxCrawledURL) {
                LOGGER.info("Nest work is done, halting.");
                break;
            }

            for(int i = 0; i < Math.min(numUnemployedSpiders(), urlsQueue.size()); i++) {
                String nextUpURL = peekNextUpURL();
                trySendSpider(rootDomainName, nextUpURL);
            }

            nestQueenResting();

            for(SpiderWork<SpiderResult> spiderWork : spidersAtWork) {
                //TODO support timeout for a specific SpiderWork in order to be able to give up on specific URL(Future.Cancel not working properly).

                if(spiderWork.isDone()) {
                    handleDoneWork(spiderWork);
                    removeSpiderWork(spiderWork);
                }
            }
        }
    }

    private int numUnemployedSpiders() {
        return nestSize - spidersAtWork.size();
    }

    private void trySendSpider(String rootDomainName, String nextUpURL) {
        if(nextUpURL != null &&
            wasNotCrawled(nextUpURL) &&
            hasAvailableSpiderWorkers() &&
            didNotCrawlEnough()) {

            int nextUpURLCode = nextUpURL.hashCode();
            urlsQueue.remove(nextUpURL);
            urlsQueueReplica.remove(nextUpURLCode);
            visitedUrls.add(nextUpURLCode);
            addSpiderWork(rootDomainName, nextUpURL);
        }
    }

    private String peekNextUpURL() {
        return urlsQueue.peek();
    }

    private boolean didNotCrawlEnough() {
        return numOfCrawledURL.get() + spidersAtWork.size() < numOfMaxCrawledURL;
    }

    private boolean hasAvailableSpiderWorkers() {
        return spidersAtWork.size() < nestSize;
    }

    private boolean wasNotCrawled(String nextUpURL) {
        return !visitedUrls.contains(nextUpURL.hashCode());
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

    private void removeSpiderWork(SpiderWork<SpiderResult> spiderWork) {
        spidersAtWork.remove(spiderWork);
    }

    private void handleDoneWork(SpiderWork<SpiderResult> spiderWork) {
        LOGGER.debug(String.format("Spider done its work [%s]", spiderWork));
        try {
            SpiderResult futureResult = spiderWork.get();
            if(futureResult != null) {
                for(String potentialNextUrl : futureResult.getNextUrls()) {
                    potentialNextUrl = StringUtils.removeEnd(potentialNextUrl, "/");
                    if(wasNotCrawled(potentialNextUrl) && notInTheQueue(potentialNextUrl)) {
                        urlsQueue.add(potentialNextUrl);
                        urlsQueueReplica.add(potentialNextUrl.hashCode());
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to handle spider's work [%s], error message [%s]", spiderWork, e.getMessage()));
        }

        LOGGER.info("Done crawling URL #" + numOfCrawledURL.get() + 1);
        numOfCrawledURL.incrementAndGet();
    }

    private boolean notInTheQueue(String potentialNextUrl) {
        return !urlsQueueReplica.contains(potentialNextUrl.hashCode());
    }

    private SpiderWork<SpiderResult> sendSpiderToWork(String rootDomainName, String nextUpURL) {
        return new SpiderWork<>(SpidersUtils.currentTimeMillis(), nextUpURL, nestExecutorService.submit(new Spider(rootDomainName, nextUpURL, spiderLogic)));
    }

    @Override
    public void close() {
        nestExecutorService.shutdown();
    }

    /**
     * BUILDER ----->
     */
    public static SpiderWebNestWithParallelismBuilder builder() {
        return new SpiderWebNestWithParallelismBuilder();
    }

    /**
     * Parallelism spider nest builder
     */
    public static class SpiderWebNestWithParallelismBuilder {

        private int nestSize;
        private long nestQueenRestMillis;
        private int maxNumOfCrawledURL;
        private HtmlUnitSpiderSearchLogic searchLogic;

        public SpiderWebNestWithParallelismBuilder setNestSize(int nestSize) {
            this.nestSize = nestSize;
            return this;
        }

        public SpiderWebNestWithParallelismBuilder setNestQueenRestMillis(long nestQueenRestMillis) {
            this.nestQueenRestMillis = nestQueenRestMillis;
            return this;
        }

        public SpiderWebNestWithParallelismBuilder setMaxNumOfCrawledURL(int maxNumOfCrawledURL) {
            this.maxNumOfCrawledURL = maxNumOfCrawledURL;
            return this;
        }

        public SpiderWebNestWithParallelismBuilder setLogic(HtmlUnitSpiderSearchLogic searchLogic) {
            this.searchLogic = searchLogic;
            return this;
        }

        public SpiderWebNestWithParallelism build() {
            validateState();
            return new SpiderWebNestWithParallelism(nestSize, nestQueenRestMillis, maxNumOfCrawledURL, new HtmlUnitSpiderLogic(searchLogic));
        }

        private void validateState() {
            if(nestSize <= 0) {
                throw new IllegalStateException("nestSize must be positive");
            }

            if(nestQueenRestMillis <= 0) {
                throw new IllegalStateException("nestQueenRestMillis must be positive");
            }

            if(maxNumOfCrawledURL <= 0) {
                throw new IllegalStateException("maxNumOfCrawledURL must be positive");
            }

            if(searchLogic == null) {
                throw new IllegalStateException("searchLogic is null");
            }
        }
    }
}
