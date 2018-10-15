package com.tumei.websocket;

import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 协议统一错误回复
 */
@Component
public class NotifyError extends BaseProtocol {
	public int seq;

	// 错误的描述, 客户端可以选择是否加工提示文字
	public String result = "";

	// 对应收到这个错误的客户端处理逻辑, 因为是服务器发送的，客户端选择是否处理
	public int command;

	public NotifyError() {}
	public NotifyError(String error) {
		this.result = error;
	}
}
