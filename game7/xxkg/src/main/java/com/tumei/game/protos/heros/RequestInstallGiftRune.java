package com.tumei.game.protos.heros;

import com.tumei.game.GameUser;
import com.tumei.model.beans.HeroBean;
import com.tumei.websocket.WebSocketUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.modelconf.AwakenConf;
import com.tumei.modelconf.HeroConf;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.tumei.common.utils.Defs.觉醒丹;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class RequestInstallGiftRune extends BaseProtocol {
	public int seq;

	/**
	 * 英雄序号
	 */
	public int index;

	/**
	 * rune >0时表示添加一个符文，是符文id
	 * rune =0时表示本级符文已经完整，此时准备提升觉醒等级
	 */
	public int rune;

	class ReturnInstallGiftRune extends BaseProtocol {
		public int seq;
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;
		ReturnInstallGiftRune rl = new ReturnInstallGiftRune();
		rl.seq = seq;

		HerosBean hsb = user.getDao().findHeros(user.getUid());
		HeroBean hb = hsb.getHeros()[index];
		if (hb == null) {
			rl.result = ErrCode.英雄不存在.name();
			user.send(rl);
			return;
		}

		HeroConf hc = Readonly.getInstance().findHero(hb.getId());
		if (hc.wake <= 0) {
			rl.result = ErrCode.英雄不可觉醒.name();
			user.send(rl);
			return;
		}

		AwakenConf ac = Readonly.getInstance().findAwaken(hb.getGift());
		if (ac == null && rune == 0) {
			rl.result = ErrCode.此英雄已经达到最高觉醒等级.name();
			user.send(rl);
			return;
		}

		if (ac.require.length > 0) {
			if (hb.getGrade() < ac.require[0]) {
				rl.result = ErrCode.英雄突破等级不足.name();
				user.send(rl);
				return;
			}
			if (hb.getLevel() < ac.require[1]) {
				rl.result = ErrCode.英雄等级不足.name();
				user.send(rl);
				return;
			}
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (rune == 0) { // 提升觉醒等级
			if (Arrays.stream(hb.getGiftrunes()).anyMatch(a -> a == 0)) {
				rl.result = ErrCode.觉醒符文缺失.name();
				user.send(rl);
				return;
			}

			if (!pb.contains(觉醒丹, ac.gradecost)) {
				rl.result = ErrCode.觉醒丹不足.name();
				user.send(rl);
				return;
			}
			user.payItem(觉醒丹, ac.gradecost, "觉醒");

			hb.setGift(hb.getGift() + 1);
		} else { // 镶嵌
			int[][] cost = null;
			if (hc.type % 2 == 1) { // 物理攻击
				cost = ac.fwcost1;
			} else { // 法术攻击
				cost = ac.fwcost2;
			}

			/**
			 * 按照顺序查看符文是否存在，是否在对应的位置已经镶嵌了符文
			 *
			 */
			boolean ok = false;
			for (int i = 0; i < 4; ++i) {
				if (rune == cost[0][i]) {
					if (hb.getGiftrunes()[i] == 0) {
						hb.getGiftrunes()[i] = rune;
						ok = true;
					}
				}
			}

			if (!ok) {
				rl.result = ErrCode.觉醒符文不正确.name();
				user.send(rl);
				return;
			}
		}

		user.send(rl);
	}
}
