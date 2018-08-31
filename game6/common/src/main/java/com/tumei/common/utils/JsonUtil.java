package com.tumei.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Administrator on 2016/11/24 0024.
 */
public class JsonUtil {
	private static ObjectMapper mapper = new ObjectMapper();

	static {
//		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	class FakeRequest {
		public int seq;
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

	/**
	 * 收到奇怪的协议获取，无法分解的协议，尽量将协议中的seq提取出来，让客户端不要长久等待, fast-fail.
	 *
	 * @param data
	 * @return
	 */
	public static int forceGetSeq(String data) {
		try {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			FakeRequest fr = mapper.readValue(data, FakeRequest.class);
			if (fr != null) {
				return fr.seq;
			}
		} catch (Exception ex) {

		}

		return 0;
	}

	public static <T> T Unmarshal(String data, Class<T> cls) throws IOException {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper.readValue(data, cls);
	}

	/**
	 * 将协议序列化成字节数组
	 *
	 * @param protocol
	 * @return
	 */
	public static String Marshal(Object protocol) throws JsonProcessingException {
		return mapper.writeValueAsString(protocol);
	}
}
