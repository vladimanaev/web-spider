package com.vladimanaev.spiders;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.vladimanaev.spiders.logic.HtmlUnitSpiderLogic;
import com.vladimanaev.spiders.model.NestResult;
import com.vladimanaev.spiders.preparations.HtmlUnitSpiderPreparations;
import com.vladimanaev.spiders.search.HtmlUnitSpiderSearchLogic;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by Vladi
 * Date: 1/7/2017
 * Time: 3:40 PM
 * Copyright VMSR
 */
public class SpiderWebNestImplTest {

    @Test
    public void testGeneralHtmlUnitHappyFlow() throws Exception {
        String expectedSearchStr = "google";
        TitleAndNetworkSearchLogic titleAndNetworkSearchLogic = new TitleAndNetworkSearchLogic(Collections.singleton(expectedSearchStr));
        HtmlUnitSpiderLogic spiderLogic = new HtmlUnitSpiderLogic(titleAndNetworkSearchLogic);
        SpiderWebNestImpl<Collection, WebClient> spiderWebNest = new SpiderWebNestImpl<>(1, 1, 5, TimeUnit.SECONDS, new HtmlUnitSpiderPreparations(), spiderLogic);

        String rootUrl = "http://www.google.com";
        NestResult<Collection> nestResult = spiderWebNest.crawl(rootUrl);
        Map<String, Collection> resultsMap = nestResult.getAll();

        // Asserting results
        assertEquals("Invalid number of URL results", 1, resultsMap.size());
        resultsMap.forEach((url, foundStr) -> {
            assertEquals("Invalid searched URL", rootUrl, url);
            assertNotNull("Couldn't find expected results", foundStr);
            for(Object currStr : foundStr) {
                assertThat("Found invalid strings", currStr, new BaseMatcher<Object>() {
                    @Override
                    public void describeTo(Description description) { }

                    @Override
                    public boolean matches(Object o) {
                        String s = o.toString();
                        return s.contains("dom_element|") || s.contains("network_sniffer|");
                    }
                });
            }
        });
    }

    /**
     * Simple searches from resources HTTP calls and string in title
     */
    private class TitleAndNetworkSearchLogic extends HtmlUnitSpiderSearchLogic {

        private Collection<String> results;

        TitleAndNetworkSearchLogic(Collection<String> searchFor) {
            super(searchFor);
            results = new LinkedList<>();
        }

        @Override
        public void apply(String url, HtmlPage page) {
            DomElement element = page.getFirstByXPath("//title");
            if(element != null) {
                searchFor.forEach(searchStr -> {
                    if(StringUtils.containsIgnoreCase(element.getTextContent(), searchStr)) {
                        results.add("dom_element|" + searchStr);
                    }
                });
            }
        }

        @Override
        public void networkSniffer(WebRequest request) {
            String url = request.getUrl().toExternalForm();
            searchFor.forEach(searchStr -> {
                if (url.contains(searchStr)) {
                    results.add("network_sniffer|" + searchStr);
                }
            });
        }

        @Override
        public Collection<String> results() {
            return results;
        }
    }
}
