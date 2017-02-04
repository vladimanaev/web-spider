package com.vladimanaev.spiders.preparations;

import com.gargoylesoftware.htmlunit.*;
import org.apache.log4j.Logger;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 4:37 PM
 * Copyright VMSR
 */
public class HtmlUnitSpiderPreparations implements SpiderPreparations<String, WebClient> {

    private static final Logger LOGGER = Logger.getLogger(HtmlUnitSpiderPreparations.class);
    private final BrowserVersion browserVersion;

    public HtmlUnitSpiderPreparations() {
        this.browserVersion = BrowserVersion.BEST_SUPPORTED;
    }

    public HtmlUnitSpiderPreparations(String userAgent) {
        this.browserVersion = BrowserVersion.BEST_SUPPORTED;
        this.browserVersion.setUserAgent(userAgent);
    }

    public HtmlUnitSpiderPreparations(BrowserVersion browserVersion) {
        this.browserVersion = browserVersion;
    }

    @Override
    public WebClient execute(String url) {
        LOGGER.debug("Executing preparation for [" + url + "]");

        final WebClient webClient = new WebClient(browserVersion);

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
