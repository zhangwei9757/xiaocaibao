package com.tumei.websocket;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by leon on 2016/12/31.
 */
public class BaseProtocol {

	public BaseProtocol() {
	}

	/**
	 * 预处理
	 *
	 * @param WebSocketUser
	 */
	protected void preProcess(WebSocketUser WebSocketUser) {
	}

	/**
	 * 后处理
	 *
	 * @param WebSocketUser
	 */
	protected void postProcess(WebSocketUser WebSocketUser) {
	}

	/**
	 * 协议接收后的处理逻辑
	 */
	public void process(WebSocketUser WebSocketUser) {
		preProcess(WebSocketUser);
		onProcess(WebSocketUser);
		postProcess(WebSocketUser);
	}

	public void onProcess(WebSocketUser WebSocketUser) {
	}

	@JsonIgnore
	public String getProtoType() {
		return getClass().getSimpleName();
	}
}
