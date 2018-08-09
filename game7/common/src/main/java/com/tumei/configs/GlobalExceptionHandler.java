package com.tumei.configs;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Leon on 2017/9/15 0015.
 *
 * 全局异常返回一个错误json提示
 *
 */
//@ControllerAdvice
public class GlobalExceptionHandler {

	class ErrorInfo<T> {
		private int code;
		private String msg;
		private String url;
		private T data;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public T getData() {
			return data;
		}

		public void setData(T data) {
			this.data = data;
		}
	}

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public ErrorInfo<String> defaultErrorHandler(HttpServletRequest request, Exception e) throws Exception {
		ErrorInfo<String> ei = new ErrorInfo<>();
		ei.setCode(1);
		ei.setMsg(e.getMessage());

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		ei.setData(sw.toString());
		ei.setUrl(request.getRequestURI());
		return ei;
	}

}
