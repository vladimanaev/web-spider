package com.vladimanaev.spiders.util;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 5:09 PM
 * Copyright VMSR
 */
public class SpidersUtils {

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String getDomainNameNoException(String url) {
        try {
            return getDomainName(url);
        } catch (URISyntaxException ignored) { }

        return null;
    }

    public static String getDomainName(String url) throws URISyntaxException {
        return getDomainName(new URI(url));
    }

    public static String getDomainName(URI uri) throws URISyntaxException {
        String domain = uri.getHost();
        return domain != null && domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static boolean isSameDomain(String domain1, String domain2) {
        return StringUtils.containsIgnoreCase(domain1, domain2) || StringUtils.containsIgnoreCase(domain2, domain1);
    }

    public static String fixUrl(String domain, String url) {
        if(url.startsWith("//")) {
            //relative protocol
            url = "http:" + url;
        } else if(url.startsWith("/")) {
            //relative url from root
            url = "http://" + domain + (url.startsWith("/") ? url : ("/" + url));
        } else if(!url.contains(".")) {
            //relative url
            url = "http://" + domain + "/" + url;
        } else if (!url.startsWith("http")) {
            //url without protocol
            url = "http://" + url;
        }

        return url;
    }
}
