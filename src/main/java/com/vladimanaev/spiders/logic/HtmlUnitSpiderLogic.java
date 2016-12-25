package com.vladimanaev.spiders.logic;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.vladimanaev.spiders.model.SpiderResult;
import com.vladimanaev.spiders.search.HtmlUnitSpiderSearchLogic;
import com.vladimanaev.spiders.util.SpidersUtils;
import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 4:38 PM
 * Copyright VMSR
 */
@ThreadSafe
public class HtmlUnitSpiderLogic implements SpiderLogic<String, WebClient, SpiderResult<Collection>> {

    private static final Logger LOGGER = Logger.getLogger(HtmlUnitSpiderLogic.class);
    private final HtmlUnitSpiderSearchLogic spiderSearchLogic;

    public HtmlUnitSpiderLogic(HtmlUnitSpiderSearchLogic spiderSearchLogic) {
        this.spiderSearchLogic = spiderSearchLogic;
    }

    @Override
    public SpiderResult<Collection> execute(String rootDomainName, String url, WebClient preparation) {
        LOGGER.debug("Executing logic for [" + rootDomainName + "] and url [" + url + "]");

        final WebConnection webConnection = preparation.getWebConnection();
        final List<String> nextUrls = new ArrayList<>();

        preparation.setWebConnection(new WebConnection() {
            @Override
            public WebResponse getResponse(WebRequest request) throws IOException {
                spiderSearchLogic.networkSniffer(request);
                return webConnection.getResponse(request);
            }

            @Override
            public void close() throws Exception {
                webConnection.close();
            }
        });

        try {
            HtmlPage page = preparation.getPage(url);
            spiderSearchLogic.apply(url, page);

            DomNodeList<DomElement> elements = page.getElementsByTagName("a");
            for(DomElement currEle : elements) {
                String newURL = currEle.getAttribute("href");
                newURL = SpidersUtils.fixUrl(rootDomainName, newURL);
                String newDomain = SpidersUtils.getDomainNameNoException(newURL);
                if(SpidersUtils.isSameDomain(newDomain, rootDomainName)) {
                    nextUrls.add(newURL);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to request page for [" + url + "]");
        }

        return new SpiderResult<>(url, spiderSearchLogic.results(), nextUrls);
    }
}
