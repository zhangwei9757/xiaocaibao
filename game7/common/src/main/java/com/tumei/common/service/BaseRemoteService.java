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


    class _StatisticInfo {
    	public String text;

    	public _StatisticInfo() {}
        public _StatisticInfo(String msg) {
    	    text = msg;
        }
    }

    /**
     * 发送统计信息到特殊点进行收集
     * @param text
     */
    public void sendStatistic(String text) {
        _StatisticInfo si = new _StatisticInfo(text);
        simpleTemplate.postForObject("https://hook.bearychat.com/=bw636/incoming/0c7d3c007237ddd07f56761c0d0a3634", si, String.class);
    }
}
