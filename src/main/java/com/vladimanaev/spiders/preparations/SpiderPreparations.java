package com.vladimanaev.spiders.preparations;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 3:13 AM
 * Copyright VMSR
 */
public interface SpiderPreparations<U, P extends AutoCloseable> {

    P execute(U url);
}
