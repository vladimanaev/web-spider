package com.vladimanaev.spiders.util;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.w3c.dom.Node;

/**
 * Created by Vladi
 * Date: 5/1/2017
 * Time: 11:12 PM
 * Copyright VMSR
 */
public class HtmlUnitUtils {

    public static String getFirstTextContent(HtmlPage page, String querySelector) {
        DomNodeList<DomNode> nodes = page.querySelectorAll(querySelector);
        if(nodes != null && nodes.size() > 0) {
            return nodes.get(0).getTextContent();
        }
        return null;
    }

    public static String getFirstNodeAttribute(HtmlPage page, String attribute, String querySelector) {
        DomNodeList<DomNode> nodes = page.querySelectorAll(querySelector);
        if(nodes != null && nodes.size() > 0) {
            Node namedItem = nodes.get(0).getAttributes().getNamedItem(attribute);
            return namedItem != null ? namedItem.getNodeValue() : null;
        }
        return null;
    }
}
