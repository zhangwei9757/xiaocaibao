package com.tumei.game.protos.misc;

import com.tumei.websocket.WebSocketUser;
import com.tumei.game.GameUser;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/3/13 0013.
 * <p>
 * 英雄的 转化
 */
@Component
public class RequestHeroTransfer extends BaseProtocol {
	public int seq;
	/**
	 * 背包中的英雄 hid, 战队中，助战中的英雄不可以进行此操作
	 */
	public int hid;
	/**
	 * 0: 查看, 如果没有刷新过，第一次会免费刷新，否则直接返回之前刷新的4个阵营的橙色英雄
	 * 1: 刷新, 需要扣100钻石进行换人
	 * 2: 确认, 确认转换英雄
	 */
	public int mode;
	/**
	 * 确认转换的英雄的序号 [1,4]
	 */
	public int transfee;

	class ReturnHeroTransfer extends BaseProtocol {
		public int seq;

		public String result = "";

		public int count;
		/**
		 * 4个可选转化的英雄
		 */
		public List<Integer> choise = new ArrayList<>();
		/**
		 * 转换确认后返回的新英雄，客户端自行对应去掉转化前的英雄
		 */
		public HeroBean transfee;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnHeroTransfer rci = new ReturnHeroTransfer();
		rci.seq = seq;

		switch (mode) {
			case 0: // 查看
				查看(user, rci);
				break;
			case 1: // 刷新
				刷新(user, rci, true);
				break;
			case 2: // 确认
				确认(user, rci, transfee);
				刷新(user, rci, true);
				break;
			default:
				rci.result = ErrCode.未知参数.name();
				break;
		}
		user.send(rci);
	}

	private void 查看(GameUser user, ReturnHeroTransfer rci) {
		刷新(user, rci, false);
	}

	private void 确认(GameUser user, ReturnHeroTransfer rci, int transfee) {
		if (transfee <= 0 || transfee > 4) {
			rci.result = ErrCode.未知参数.name();
			return;
		}

		HerosBean hsb = user.getDao().findHeros(user.getUid());
		int id = hsb.getChoise().get(transfee - 1);
		PackBean pb = user.getDao().findPack(user.getUid());

		if (!pb.contains(钻石, 500)) {
			rci.result = ErrCode.钻石不足.name();
			return;
		}

		HeroBean hb = pb.getHeros().remove(hid);
		if (hb == null) {
			rci.result = ErrCode.英雄不存在.name();
			return;
		}

		user.payItem(钻石, 500, "转化英雄");

		HeroBean newguy = pb.addHero(id, "转换");
		newguy.setLevel(hb.getLevel());
		newguy.setGrade(hb.getGrade());
		newguy.setFate(hb.getFate());
		newguy.setFateexp(hb.getFateexp());

		rci.transfee = newguy;

		{
			for (int i = 1; i <= 4; ++i) {
				final int j = i;
				int[] heros = Readonly.getInstance().findHeroByQuality(4).stream().filter(hc -> hc.sect == j).mapToInt(hc -> hc.key).toArray();
				int idx = (RandomUtil.getRandom() % heros.length);
				rci.choise.add(heros[idx]);
			}
			// 更新记录中的选择
			hsb.setChoise(rci.choise);
		}

		rci.choise = hsb.getChoise();
	}

	private void 刷新(GameUser user, ReturnHeroTransfer rci, boolean _force) {
		HerosBean hsb = user.getDao().findHeros(user.getUid());
		boolean needFlush = false;
		if (hsb.getChoise().size() >= 4) {
			if (_force) {
				PackBean pb = user.getDao().findPack(user.getUid());
				if (!pb.contains(钻石, 100)) {
					rci.result = ErrCode.钻石不足.name();
					return;
				}
				user.payItem(钻石, 100, "刷新转化英雄");
				needFlush = true;
			}
		} else {
			needFlush = true;
		}

		if (needFlush) {
			for (int i = 1; i <= 4; ++i) {
				final int j = i;
				int[] heros = Readonly.getInstance().findHeroByQuality(4).stream().filter(hc -> hc.sect == j).mapToInt(hc -> hc.key).toArray();
				int idx = (RandomUtil.getRandom() % heros.length);
				rci.choise.add(heros[idx]);
			}
			// 更新记录中的选择
			hsb.setChoise(rci.choise);
		}

		rci.choise = hsb.getChoise();
	}
}
