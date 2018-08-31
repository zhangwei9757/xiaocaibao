package com.tumei.groovy.contract;

import com.tumei.websocket.SessionUser;

/**
 * Created by Leon on 2017/9/1 0001.
 */
public interface IProtocolDispatcher {

    void process(String cmd, String data, SessionUser session);

}
