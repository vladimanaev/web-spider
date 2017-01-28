package com.vladimanaev.spiders.search;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.vladimanaev.spiders.model.SpiderResultsDetails;

import java.util.Collection;

/**
 * Created by Vladi
 * Date: 9/19/2016
 * Time: 12:00 AM
 * Copyright VMSR
 */
public abstract class HtmlUnitSpiderSearchLogic<D> implements SpiderSearchLogic<String, HtmlPage, SpiderResultsDetails<D>> {

    protected final Collection<String> searchFor;
    protected SpiderResultsDetails<D> results;

    public HtmlUnitSpiderSearchLogic(Collection<String> searchFor) {
        this.searchFor = searchFor;
        this.results = new SpiderResultsDetails<>();
    }

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
     * @return search logic results
     */
    @Override
    public abstract SpiderResultsDetails<D> results();

    @Override
    public void reset() {
        this.results = new SpiderResultsDetails<>();
    }
}
