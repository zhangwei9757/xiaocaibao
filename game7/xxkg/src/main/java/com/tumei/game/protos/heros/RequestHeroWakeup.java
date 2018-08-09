package com.tumei.game.protos.heros;

import com.tumei.websocket.WebSocketUser;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.modelconf.AwakenConf;
import com.tumei.modelconf.HeroConf;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

import static com.tumei.common.utils.Defs.觉醒丹;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级英雄
 */
@Component
public class RequestHeroWakeup extends BaseProtocol {
	public int seq;
	/**
	 * 英雄唯一id
	 */
	public int hid;

	/**
	 * 符文的位置:[1,5]  5表示升级
	 */
	public int rune;


	class ReturnHeroWakeup extends BaseProtocol {
		public int seq;
		/**
		 * 结果
		 */
		public String result = "";
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnHeroWakeup rl = new ReturnHeroWakeup();
		rl.seq = seq;

		long uid = user.getUid();
		HerosBean hsb = user.getDao().findHeros(uid);

		RoleBean rb = user.getDao().findRole(uid);
		Optional<HeroBean> opt = Arrays.stream(hsb.getHeros()).filter((HeroBean hb) -> hb.getHid() == hid).findFirst();

		opt.ifPresent(hb -> {
			HeroConf hc = Readonly.getInstance().findHero(hb.getId());
			if (hc.wake <= 0) {
				rl.result = "该英雄无法觉醒";
				return;
			}

			AwakenConf ac = Readonly.getInstance().findAwaken(hb.getGift());
			if (ac == null && rune == 0) {
				rl.result = ErrCode.此英雄已经达到最高觉醒等级.name();
				return;
			}

			if (rune == 5 && ac.require.length > 0) {
				if (hb.getGrade() < ac.require[0]) {
					rl.result = ErrCode.英雄突破等级不足.name();
					return;
				}

				if (hb.getLevel() < ac.require[1]) {
					rl.result = ErrCode.英雄等级不足.name();
					return;
				}
			}

			PackBean pb = user.getDao().findPack(uid);

			if (rune == 5) {
				if (Arrays.stream(hb.getGiftrunes()).anyMatch(i -> i == 0)) {
					rl.result = "符文缺失，请先装满符文再觉醒";
					return;
				}

				if (!pb.contains(觉醒丹, ac.gradecost))  {
					rl.result = ErrCode.觉醒丹不足.name();
					return;
				}
				user.payItem(觉醒丹, ac.gradecost, "觉醒英雄");

				int[] runes = hb.getGiftrunes();
				for (int i = 0; i < runes.length; ++i) {
					runes[i] = 0;
				}
				hb.setGift(hb.getGift() + 1);
			}
			else {
				--rune;
				int runes = hb.getGiftrunes()[rune];
				if (runes != 0) {
					rl.result = "存储已经装备符文";
					return;
				}

				int req = 0;
				if (hc.type == 1 || hc.type == 2) { // 物理 法攻
					req = ac.fwcost1[0][rune];
				}
				else {
					req = ac.fwcost2[0][rune];
				}

				if (!pb.contains(req, 1)) {
					rl.result = "没有此类符文";
					return;
				}

				user.payItem(req, 1, "英雄觉醒");
				hb.getGiftrunes()[rune] = req;
			}
		});

		user.send(rl);
	}
}
