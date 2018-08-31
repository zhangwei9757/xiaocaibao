package com.tumei.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Administrator on 2016/12/28 0028.
 */
public abstract class BaseRemoteService {
    @Autowired
    @Qualifier(value = "balance")
    protected RestTemplate restTemplate;

    @Autowired
    @Qualifier(value = "simple")
    protected RestTemplate simpleTemplate;
}
