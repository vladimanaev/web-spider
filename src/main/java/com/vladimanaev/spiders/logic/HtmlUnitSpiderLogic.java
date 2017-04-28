package com.vladimanaev.spiders.logic;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.vladimanaev.spiders.model.SpiderResult;
import com.vladimanaev.spiders.search.HtmlUnitSpiderSearchLogic;
import com.vladimanaev.spiders.util.SpidersUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 4:38 PM
 * Copyright VMSR
 */
public class HtmlUnitSpiderLogic implements SpiderLogic {

    private static final Logger LOGGER = Logger.getLogger(HtmlUnitSpiderLogic.class);
    private final HtmlUnitSpiderSearchLogic spiderSearchLogic;

    public HtmlUnitSpiderLogic(HtmlUnitSpiderSearchLogic spiderSearchLogic) {
        this.spiderSearchLogic = spiderSearchLogic;
    }

    @Override
    public SpiderResult execute(String rootDomainName, String url) {
        long startTime = SpidersUtils.currentTimeMillis();
        try (WebClient webClient = createWebClient(url)) {
            LOGGER.debug("Executing logic for [" + rootDomainName + "] and url [" + url + "]");

            final WebConnection webConnection = webClient.getWebConnection();
            final List<String> nextUrls = new LinkedList<>();

            webClient.setWebConnection(new WebConnection() {
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
                HtmlPage page = webClient.getPage(url);
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

            return new SpiderResult(url, nextUrls);
        } finally {
            LOGGER.info("Spider done working [" + url + "], took [" + ((SpidersUtils.currentTimeMillis() - startTime) / 1000) + "s]");
        }
    }

    private WebClient createWebClient(String url) {
        LOGGER.debug("creating web client for [" + url + "]");

        final WebClient webClient = new WebClient();

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
