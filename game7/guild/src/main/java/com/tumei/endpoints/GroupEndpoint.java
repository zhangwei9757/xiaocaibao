package com.tumei.endpoints;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/15 0015.
 */
public class GroupEndpoint extends AbstractEndpoint {
	public GroupEndpoint() {
		super("公会监控", false);
	}

	@Override
	public String getId() {
		return "group";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isSensitive() {
		return false;
	}

	@Override
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>();
		result.put("战斗平均耗时", 11);
		return result;
	}
}
