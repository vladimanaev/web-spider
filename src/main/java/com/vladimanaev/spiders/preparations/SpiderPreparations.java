package com.vladimanaev.spiders.preparations;

import org.apache.http.annotation.ThreadSafe;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 3:13 AM
 * Copyright VMSR
 */
@ThreadSafe
public interface SpiderPreparations<U, P extends AutoCloseable> {

    P execute(U url);
}
