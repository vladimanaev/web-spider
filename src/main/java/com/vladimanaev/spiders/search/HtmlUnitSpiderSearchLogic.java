package com.vladimanaev.spiders.search;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Created by Vladi
 * Date: 9/19/2016
 * Time: 12:00 AM
 * Copyright VMSR
 */
public abstract class HtmlUnitSpiderSearchLogic implements SpiderSearchLogic<HtmlPage> {

    /**
     * Apply the search logic
     * @param url - page url that is being crawled
     * @param page - page object
     */
    @Override
    public abstract void apply(String url, HtmlPage page);

    /**
     * Network sniffer
     * @param request HtmlPage resource request
     */
    public abstract void networkSniffer(WebRequest request);

    /**
     * @return browser version
     */
    public BrowserVersion getBrowserVersion() {
        return BrowserVersion.getDefault();
    }

    /**
     * Creates web client for each crawling URL
     */
    public WebClient createWebClient() {

        final WebClient webClient = new WebClient(getBrowserVersion());

        webClient.setJavaScriptErrorListener(null);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.setJavaScriptTimeout(500);
        webClient.waitForBackgroundJavaScript(490);

        WebClientOptions options = webClient.getOptions();

        options.setRedirectEnabled(true);
        options.setJavaScriptEnabled(true);
        options.setCssEnabled(true);
        options.setUseInsecureSSL(true);

        options.setThrowExceptionOnScriptError(false);
        options.setThrowExceptionOnFailingStatusCode(false);
        options.setPopupBlockerEnabled(false);
        options.setPrintContentOnFailingStatusCode(false);

        return webClient;
    }
}
