package com.tumei.websocket;

import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/3/13 0013.
 * <p>
 * 通知进入视野的物体
 *
 */
@Component
public class ErrorInfo extends BaseProtocol {
	public int seq;

	// 错误的描述, 客户端可以选择是否加工提示文字
	public String result = "";

	// 对应收到这个错误的客户端处理逻辑, 因为是服务器发送的，客户端选择是否处理
	public int command;
}
