package com.tumei.centermodel;

import com.tumei.centermodel.struct.UserRoleBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leon on 2016/11/5.
 * <p>
 * 玩家与服务器之间的映射关系
 */
@Document(collection = "Receipts")
public class ReceiptBean {
	@Id
	public String id;
	// 唯一订单号, 苹果和云顶都是使用苹果的唯一订单号
	@Indexed(unique = true)
	public String tid;
	// 充值 人民币元
	public int rmb;
	// 游戏角色id
	@Indexed
	public long uid;
	// 服务器id
	public int zone;
	// 支付时间
	public Date time;
	public int ts;
	// 商品代码, 苹果上有苹果的
	public String good;
	/**
	 * 支付渠道
	 * 1 苹果
	 *
	 */
	public int channel;

	/**
	 * 0: 玩家未领取充值
	 * 1: 玩家已经领取
	 */
	public int status;

	// 渠道
	public String source;

	// 渠道玩家身份
	public String sourceUser;

	public String bundle;

	/**
	 * 0 非沙盒
	 * 1 沙盒
	 */
	public int sandbox;

	public int buff;

	public ReceiptBean() { }

}
