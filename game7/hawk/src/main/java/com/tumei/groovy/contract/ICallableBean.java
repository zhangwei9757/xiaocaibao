package com.tumei.groovy.contract;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;

/**
 * Created by Leon on 2017/9/1 0001.
 */
public interface ICallableBean {

	String fuck();

	Object httpCall(HttpServletRequest req);
}
