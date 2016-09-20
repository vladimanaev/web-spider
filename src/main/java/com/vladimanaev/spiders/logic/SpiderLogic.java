package com.vladimanaev.spiders.logic;

import org.apache.http.annotation.ThreadSafe;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 3:09 AM
 * Copyright VMSR
 */
@ThreadSafe
public interface SpiderLogic<U, P extends AutoCloseable, R> {

    R execute(String rootDomainName, U url, P preparation);
}
