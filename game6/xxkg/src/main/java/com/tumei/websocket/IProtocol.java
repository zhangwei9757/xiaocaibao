package com.tumei.websocket;

public interface IProtocol {
//    /**
//     * 协议接收后的处理逻辑
//     */
//    void process(SessionUser session);

    void onProcess(SessionUser session);

    String getProtoType();
}
