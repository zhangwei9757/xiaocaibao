package com.tumei.game.protos.rune;

import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.model.RuneBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.FuwenraidConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 测试充值协议
 */
@Component
public class RequestRuneDig extends BaseProtocol {
	public int seq;
	// [1,9] 九个位置
	public int index;

	class ReturnRuneDig extends BaseProtocol {
		public int seq;
		public String result = "";
		// 返回结果
		// 与objects上的标识含义一致 [1,9]
		public int effect;
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnRuneDig rl = new ReturnRuneDig();
		rl.seq = seq;

		if (index < 1 || index > 9) {
			rl.result = "参数错误";
			user.send(rl);
			return;
		}

		--index;
		RuneBean rb = user.getDao().findRune(user.getUid());
		int mark = rb.getObjects()[index];
		if (mark != 0 && mark != 9) {
			rl.result = "已经打开此处的宝箱，请选择其他位置";
			user.send(rl);
			return;
		}

		rl.effect = rb.dig(index);
		if (rl.effect < 0) {
			rl.result = "没有足够的锤子";
			user.send(rl);
			return;
		}

		FuwenraidConf fc = Readonly.getInstance().findFuwen(rb.getLevel());
		switch (rl.effect) {
			case 1: // 物品随机一个
			{
				rl.awards.addAll(user.addItem(fc.item, 1, true, "符文挖:" + rb.getLevel()));
			}
				break;
			case 2: // 打折物品随机一个
			{
				int idx = (RandomUtil.getRandom() % fc.salegood.length);
				int[] item = fc.salegood[idx];
				StoreStruct ss = new StoreStruct();
				ss.id = item[0];
				ss.count = item[1];
				ss.price = new int[]{钻石, item[2]};
				ss.limit = 1;
				rb.getStores().add(ss);

				rl.awards.add(new AwardBean(ss.id, ss.count, 0));
			}
				break;
			case 5: // 第一个最好的
			{
				AwardBean ab = rb.getBest().get(0);
				rl.awards.addAll(user.addItem(ab.id, -ab.count, false, "dig best1:" + rb.getLevel()));
			}
				break;
			case 6: // 第二个最好的
			{
				AwardBean ab = rb.getBest().get(1);
				rl.awards.addAll(user.addItem(ab.id, -ab.count, false, "dig best2:" + rb.getLevel()));
			}
				break;
		}

		user.send(rl);
	}
}
