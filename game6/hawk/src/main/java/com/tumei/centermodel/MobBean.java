package com.tumei.centermodel;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 */
@Data
@Document(collection = "Mobs")
public class MobBean {
	@Id
	private String id;

	/**
	 * 编号
	 */
	private int no;

	/**
	 * 时间
	 */
	private Date ts;

	private String ip;

	private String idfa;

	/**
	 * 唯一标识
	 */
	private String clickid;

	/**
	 * 广告位
	 */
	private String s1;

	private String os;

	private String callback;

	private int hashCode;

	/**
	 * 对应的帐号id
	 */
	private long account;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}

	public String getClickid() {
		return clickid;
	}

	public void setClickid(String clickid) {
		this.clickid = clickid;
	}

	public String getS1() {
		return s1;
	}

	public void setS1(String s1) {
		this.s1 = s1;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public int getHashCode() {
		return hashCode;
	}

	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}

	public long getAccount() {
		return account;
	}

	public void setAccount(long account) {
		this.account = account;
	}

	@Override
	public String toString() {
		return "MobBean{" + "id='" + id + '\'' + ", no=" + no + ", ts=" + ts + ", ip='" + ip + '\'' + ", idfa='" + idfa + '\'' + ", clickid='" + clickid + '\'' + ", s1='" + s1 + '\'' + ", os='" + os + '\'' + ", callback='" + callback + '\'' + ", hashCode=" + hashCode + ", account=" + account + '}';
	}
}
