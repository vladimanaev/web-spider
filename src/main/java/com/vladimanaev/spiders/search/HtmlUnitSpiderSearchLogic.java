package com.vladimanaev.spiders.search;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.annotation.ThreadSafe;

import java.util.Collection;

/**
 * Created by Vladi
 * Date: 9/19/2016
 * Time: 12:00 AM
 * Copyright VMSR
 */
@ThreadSafe
public abstract class HtmlUnitSpiderSearchLogic implements SpiderSearchLogic<String, HtmlPage, Collection> {

    protected final Collection<String> searchFor;

    public HtmlUnitSpiderSearchLogic(Collection<String> searchFor) {
        this.searchFor = searchFor;
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
    public abstract Collection<String> results();

}
