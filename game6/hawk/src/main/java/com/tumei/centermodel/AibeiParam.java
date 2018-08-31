package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/4/5 0005.
 */
@Document(collection = "AibeiParam")
public class AibeiParam {
	@Id
	private String id;

	/**
	 * 包名
	 */
	private String bundle;
	/**
	 * 应用id
	 */
	private String appid;
	/**
	 * 私钥
	 */
	private String privatekey;
	/**
	 * 平台公钥
	 */
	private String platkey;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getPrivatekey() {
		return privatekey;
	}

	public void setPrivatekey(String privatekey) {
		this.privatekey = privatekey;
	}

	public String getPlatkey() {
		return platkey;
	}

	public void setPlatkey(String platkey) {
		this.platkey = platkey;
	}

	@Override
	public String toString() {
		return "AibeiParam{" + "bundle='" + bundle + '\'' + ", appid='" + appid + '\'' + ", privatekey='" + privatekey + '\'' + ", platkey='" + platkey + '\'' + '}';
	}
}
