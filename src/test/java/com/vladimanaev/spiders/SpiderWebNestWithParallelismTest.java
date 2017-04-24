package com.vladimanaev.spiders;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.vladimanaev.spiders.search.HtmlUnitSpiderSearchLogic;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Vladi
 * Date: 1/7/2017
 * Time: 3:40 PM
 * Copyright VMSR
 */
public class SpiderWebNestWithParallelismTest {

    @Test
    public void testGeneralHtmlUnitHappyFlow() throws Exception {
        String expectedSearchStr = "google";
        TitleAndNetworkSearchLogic titleAndNetworkSearchLogic = new TitleAndNetworkSearchLogic(expectedSearchStr);

        SpiderWebNest spiderWebNest = SpiderWebNestWithParallelism.builder()
                                                                .setNestSize(1)
                                                                .setMaxNumOfCrawledURL(1)
                                                                .setNestQueenRestMillis(200)
                                                                .setLogic(titleAndNetworkSearchLogic)
                                                                .build();

        spiderWebNest.crawl("http://www.google.com");

        // Asserting results
        assertTrue("Invalid page str", ("dom_element|" + expectedSearchStr).equals(titleAndNetworkSearchLogic.getPageFinding()));
        assertTrue("Invalid network sniffer str", ("sniffer|" + expectedSearchStr).equals(titleAndNetworkSearchLogic.getNetworkSnifferFinding()));
    }

    /**
     * Simple searches from resources HTTP calls and string in title
     */
    private class TitleAndNetworkSearchLogic extends HtmlUnitSpiderSearchLogic {

        private String pageFinding;
        private String networkSnifferFinding;

        private String searchStr;

        TitleAndNetworkSearchLogic(String searchStr) {
            this.searchStr = searchStr;
        }

        @Override
        public void apply(String url, HtmlPage page) {
            DomElement element = page.getFirstByXPath("//title");
            if(element != null) {
                if(StringUtils.containsIgnoreCase(element.getTextContent(), searchStr)) {
                    pageFinding = "dom_element|" + searchStr;
                }
            }
        }

        @Override
        public void networkSniffer(WebRequest request) {
            String url = request.getUrl().toExternalForm();
            if (url.contains(searchStr)) {
                networkSnifferFinding = "sniffer|" + searchStr;
            }
        }

        public String getPageFinding() {
            return pageFinding;
        }

        public String getNetworkSnifferFinding() {
            return networkSnifferFinding;
        }
    }
}
