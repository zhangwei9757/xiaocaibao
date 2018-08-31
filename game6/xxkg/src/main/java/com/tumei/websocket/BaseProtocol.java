package com.tumei.websocket;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by leon on 2016/12/31.
 */
public class BaseProtocol implements IProtocol {

    public BaseProtocol() {
    }

    /**
     * 预处理
     * @param session
     */
    protected void preProcess(SessionUser session) {
    }

    /**
     * 后处理
     * @param session
     */
    protected void postProcess(SessionUser session) {
    }

    /**
     * 协议接收后的处理逻辑
     */
    public void process(SessionUser session) {
        preProcess(session);
        onProcess(session);
        postProcess(session);
    }

    public void onProcess(SessionUser session) {

    }

    @JsonIgnore
    public String getProtoType() {
        return getClass().getSimpleName();
    }
}
