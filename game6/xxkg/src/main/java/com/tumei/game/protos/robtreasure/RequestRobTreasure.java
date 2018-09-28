package com.tumei.game.protos.robtreasure;

import com.tumei.common.Readonly;
import com.tumei.common.RemoteService;
import com.tumei.common.fight.FightResult;
import com.tumei.common.fight.FightStruct;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.game.protos.notifys.NotifyRobInfo;
import com.tumei.game.protos.structs.RobStruct;
import com.tumei.game.services.RobService;
import com.tumei.model.*;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.ItemConf;
import com.tumei.modelconf.TresackerConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestRobTreasure extends BaseProtocol {
	public int seq;
	/**
	 * 挑战第几个角色 [0,5)
	 */
	public int index;

	/**
	 * 0: 单次
	 * 1: 五连
	 */
	public int fast;

	class ReturnRobTreasure extends BaseProtocol {
		public int seq;
		public String result = "";
		public int win;
		/**
		 * 抢夺玩家要播放的战斗数据
		 */
		public String data;
		// 当前的精力，已经扣减本次消耗
		public int spirit;
		// 如果是连续5次抢夺，奖励的个数表示抢夺的次数
		public List<AwardBean> rewards = new ArrayList<>();

		public List<AwardBean> awards = new ArrayList<>();

		/**
		 * 获得的碎片, item = 0 表示没有获得碎片，五连扫的时候，不会失败，所以awards对应本次五连扫的奖励，每个对应一次，
		 * 也许上次扫荡就获得了item,那么awards会有三个额外奖励，但是也可能五连扫有五个奖励，item却为0，表示五次都没有获得碎片。
		 * <p>
		 * 同样对于挑战npc,不会有五连扫，是否获得了item就是通过是否为0表示，奖励也是单次的
		 * <p>
		 * rewards:不包含获得的碎片。
		 */
		public int item;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnRobTreasure rci = new ReturnRobTreasure();
		rci.win = 1;
		rci.seq = seq;

		if (user.tmpRobs == null || user.tmpRobItem == 0 || index < 0 || index >= user.tmpRobs.size()) {
			rci.result = "对手不存在，请刷新对手";
			user.send(rci);
			return;
		}


		PackBean pb = user.getDao().findPack(user.getUid());

		if (pb.contains(user.tmpRobItem, 1)) {
			rci.result = "背包中已经存在该碎片，不能再进行抢夺";
			user.send(rci);
			return;
		}

		rci.spirit = pb.flushSpirit(0);
		if (rci.spirit < 2) {
			rci.result = ErrCode.活力不足.name();
			user.send(rci);
			return;
		}

		ItemConf ic = Readonly.getInstance().findItem(user.tmpRobItem);
		TresackerConf tc = Readonly.getInstance().findTresacker();

		RobStruct rs = user.tmpRobs.get(index);

		if (fast == 0) {
			if (rs.getUid() == 0) { // Npc 战斗一定胜利, 只需要判断机率

				int ratio = tc.rate[5 - ic.quality][1];
				if (RandomUtil.getRandom() % 100 < ratio) {
					rci.item = user.tmpRobItem;
				}

				// 从tc中随机一个奖励出来
				int total = 0;
				int r = RandomUtil.getRandom() % 100;
				int awd = tc.reward[0];
				for (int i = 0; i < tc.reward.length; i += 2) {
					total += tc.reward[i + 1];
					if (r < total) {
						awd = tc.reward[i];
						break;
					}
				}

				rci.win = 1;
				// 战胜奖励
				rci.rewards.addAll(user.addItem(awd, 1, true, "抢碎片"));
				ActivityBean ab = user.getDao().findActivity(user.getUid());
				ab.flushCampaign();
				ab.incCampaign2(1);
			}
			else { // 抢夺玩家
				RobBean robBean = user.findRob();
				robBean.notProtect();

				long pid = rs.getUid();

				RoleBean rb = user.getDao().findRole(user.getUid());

				HerosBean hsb = user.getDao().findHeros(user.getUid());
				FightStruct arg = new FightStruct();
				arg.left = hsb.createHerosStruct();

				HerosBean other = user.getDao().findHeros(pid);
				arg.right = other.createHerosStruct();

				FightResult fr = RemoteService.getInstance().callFight(arg);
				rci.data = fr.data;

				if (fr.win != 1) { // 失败一定不会有奖励
					rci.win = 0;
				}
				else {
					// 2. 根据概率获取是否得到碎片
					int ratio = tc.rate[5 - ic.quality][0];
					if (RandomUtil.getRandom() % 100 < ratio) {
						// 3. 要判断对方是否可以获得碎片
						PackBean peer = user.getDao().findPack(pid);
						if (peer.payItem(user.tmpRobItem, 1, "打劫") >= 1) {
							// 被抢劫玩家碎片的记录减1
							rci.item = user.tmpRobItem;
							user.warn("对方有碎片, 减少需要通知对方.");

							String goodname = Defs.getColorString(ic.quality, ic.good);
							if (GameServer.getInstance().sendInfoMail(pid, "宝物掠夺", "玩家【" + Defs.getColorString(rb.getGrade(), user.getName()) + "】在宝物掠夺中抢走了你的【" + goodname + "】")) {
								GameUser beRob = GameServer.getInstance().find(pid);
								if (beRob != null) {
									NotifyRobInfo nri = new NotifyRobInfo(user.tmpRobItem);
									beRob.send(nri);
								}
							}

						} else {
							user.warn("对方没有碎片, 修改索引.");
							RobService.getInstance().commitFrags(user.getUid(), user.tmpRobItem, -1);
						}
					}

					// 从tc中随机一个奖励出来
					int total = 0;
					int r = RandomUtil.getRandom() % 100;
					int awd = tc.reward[0];
					for (int i = 0; i < tc.reward.length; i += 2) {
						total += tc.reward[i + 1];
						if (r < total) {
							awd = tc.reward[i];
							break;
						}
					}

					// 战胜奖励
					rci.rewards.addAll(user.addItem(awd, 1, true, "抢碎片"));
					ActivityBean ab = user.getDao().findActivity(user.getUid());
					ab.flushCampaign();
					ab.incCampaign2(1);
				}
			}
			rci.spirit = pb.flushSpirit(-2);
			user.pushDailyTask(8, 1);
		}
		else { // 五连扫
			if (rs.getUid() != 0) {
				rci.result = "对方是玩家，不能五连扫";
				user.send(rci);
				return;
			}
			if (fast < 1 && fast > 100) {
				rci.result = "连续扫荡不能大于一百次";
				user.send(rci);
				return;
			}

			int ratio = tc.rate[5 - ic.quality][1];

			int need_spirit = fast * 2;
			int has_spirit = pb.flushSpirit(0);
			if (has_spirit < need_spirit) {
				rci.result = ErrCode.活力不足.name();
				user.send(rci);
				return;
			}

			int num = 0;

			for (int j = 0; j < fast; ++j) {
				if (RandomUtil.getRandom() % 100 < ratio) {
					rci.item = user.tmpRobItem;
				}

				// 从tc中随机一个奖励出来
				int total = 0;
				int r = RandomUtil.getRandom() % 100;
				int awd = tc.reward[0];
				for (int i = 0; i < tc.reward.length; i += 2) {
					total += tc.reward[i + 1];
					if (r < total) {
						awd = tc.reward[i];
						break;
					}
				}

				// 战胜奖励
				rci.rewards.addAll(user.addItem(awd, 1, true, "抢碎片"));
				rci.spirit = pb.flushSpirit(-2);
				++num;

				if (rci.spirit < 2) {
					break;
				}

				if (rci.item != 0) {
					break;
				}
			}

			ActivityBean ab = user.getDao().findActivity(user.getUid());
			ab.flushCampaign();
			ab.incCampaign2(num);
			user.pushDailyTask(8, num);
		}

		if (rci.item != 0) {
			rci.awards.addAll(user.addItem(rci.item, 1, false, "抢碎片"));


			user.tmpRobs = null;
			user.tmpRobItem = 0;
		}

		user.send(rci);
	}
}
