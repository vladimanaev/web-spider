package com.vladimanaev.util;

import com.vladimanaev.spiders.util.SpidersUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 11:09 PM
 * Copyright VMSR
 */
public class SpidersUtilsTest {

    @Test
    public void testExtractDomainFromUrlNoException() {
        assertEquals("Extracted invalid domain from URL", "vladimanaev.com", SpidersUtils.getDomainNameNoException("http://www.vladimanaev.com"));
        assertEquals("Extracted invalid domain from URL", "vladimanaev.com", SpidersUtils.getDomainNameNoException("https://www.vladimanaev.com"));
        assertEquals("Extracted invalid domain from URL", "vladimanaev.com", SpidersUtils.getDomainNameNoException("http://vladimanaev.com"));
        assertEquals("Extracted invalid domain from URL", "vladimanaev.com", SpidersUtils.getDomainNameNoException("https://vladimanaev.com"));
        assertNull("Extracted invalid domain from URL", SpidersUtils.getDomainNameNoException("vladimanaev.com"));
        assertNull("Extracted invalid domain from URL", SpidersUtils.getDomainNameNoException("asfasfasg"));
    }

    @Test
    public void testFixUrlProtocol() {
        assertEquals("Invalid fixed URL", "http://example.com", SpidersUtils.fixUrl("example.com", "example.com"));

        //different domain
        assertEquals("Invalid fixed URL", "http://example.com/vladi/test/path", SpidersUtils.fixUrl("example1.com", "//example.com/vladi/test/path"));
        assertEquals("Invalid fixed URL", "http://example.com/vladi/test/path", SpidersUtils.fixUrl("example1.com", "example.com/vladi/test/path"));
        assertEquals("Invalid fixed URL", "http://www.example.com/vladi/test/path", SpidersUtils.fixUrl("example1.com", "www.example.com/vladi/test/path"));

        //same domain
        assertEquals("Invalid fixed URL", "http://example.com/vladi/test/path", SpidersUtils.fixUrl("example.com", "//example.com/vladi/test/path"));
        assertEquals("Invalid fixed URL", "http://example.com/vladi/test/path", SpidersUtils.fixUrl("example.com", "example.com/vladi/test/path"));

        //same domain relative paths
        assertEquals("Invalid fixed URL", "http://example.com/vladi/test/path", SpidersUtils.fixUrl("example.com", "/vladi/test/path"));
        assertEquals("Invalid fixed URL", "http://example.com/vladi/test/path", SpidersUtils.fixUrl("example.com", "vladi/test/path"));
        assertEquals("Invalid fixed URL", "http://www.example.com/vladi/test/path", SpidersUtils.fixUrl("example.com", "//www.example.com/vladi/test/path"));
        assertEquals("Invalid fixed URL", "http://www.example.com/vladi/test/path", SpidersUtils.fixUrl("example.com", "www.example.com/vladi/test/path"));
    }
}
