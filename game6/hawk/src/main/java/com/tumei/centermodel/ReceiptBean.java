package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

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
	public int accid;
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

	/**
	 * 特殊双倍标记, 具有特殊双倍标记的充值单据，在充值的时候，使用特殊次数判定法则
	 *
	 * buff默认为 0，标识不参与
	 * game服务器查询到充值单据之后，检查每个人本地记录的buff是否相同，以判定是否在
	 * 同一个活动内，跨越活动，则清除之前的活动次数记录，从新开始双倍判定
	 *
	 * */
	public int buff;

	public ReceiptBean() { }

}
