package com.tumei.groovy;

/**
 * Created by Leon on 2017/10/25 0025.
 */
public class ScriptInfo {
	public String name;
	public String location;
	public long lastModified;
	public String module;

	public ScriptInfo(String module, String name, String location, long ts) {
		this.module = module;
		this.name = name;
		this.location = location;
		this.lastModified = ts;
	}

	@Override
	public String toString() {
		return "ScriptInfo{" + "name='" + name + '\'' + ", location='" + location + '\'' + ", lastModified=" + lastModified + '}';
	}
}
