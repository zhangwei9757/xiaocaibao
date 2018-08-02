package com.tumei.game.protos.structs;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
public class ChargeStruct {
	public String name = "";
	/**
	 * 人民币价格
	 */
	public int rmb;
	/**
	 * 购买后可以获得的钻石 (当属于非一次性的充值,gem表示购买的时候立刻获得的钻石）
	 */
	public int gem;
	/**
	 * 额外可以获得的钻石,一般是首次充值会有赠送。(当属于非一次性的充值，extra表示每日可以领取的钻石）
	 */
	public int extra;
	/**
	 * 卡的 购买的时间, 0 表示未购买
	 */
	public long ts;
	/**
	 * 倍率 1倍还是2倍
	 */
	public float ratio = 1.0f;

	public ChargeStruct() {}

	public ChargeStruct(String _name, int _rmb, int _gem, int _extra, float _ratio) {
		name = _name;
		rmb = _rmb;
		gem = _gem;
		extra = _extra;
		ratio = _ratio;
	}
}
