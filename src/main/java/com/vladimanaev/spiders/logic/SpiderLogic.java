package com.vladimanaev.spiders.logic;

import com.vladimanaev.spiders.model.SpiderResult;

/**
 * Created by Vladi
 * Date: 9/17/2016
 * Time: 3:09 AM
 * Copyright VMSR
 */
public interface SpiderLogic {

    SpiderResult execute(String rootDomainName, String url);
}
