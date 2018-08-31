package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.common.RemoteService;
import com.tumei.common.webio.BattleResultStruct;
import com.tumei.common.webio.BattleStruct;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.model.HerosBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.GuildraidConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.公会贡献;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupSceneFight extends BaseProtocol {
	public int seq;
	//[1,4],四个不同的关卡
	public int index;

	class ReturnGroupSceneFight extends BaseProtocol {
		public int seq;
		public String result = "";
		public String data = "";
		// 是否最后击杀，1表示击杀
		public int kill;
		// 本次攻击的伤害
		public long harm;
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupSceneFight rl = new ReturnGroupSceneFight();
		rl.seq = seq;

		if (index < 1 || index > 4) {
			rl.result = "错误的关卡数，应在1~4之间";
			user.send(rl);
			return;
		}

		LocalDateTime ldt = LocalDateTime.now();
		if (ldt.getHour() < 10) {
			rl.result = "天体赛场正在休整中，请8:00后发起挑战.";
			user.send(rl);
			return;
		}

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			try {
				// 本地控制攻击次数和恢复时间
				gb.flush(user.getVip());
				if (gb.getSceneCount() <= 0) {
					rl.result = "没有剩余的挑战次数";
					user.send(rl);
					return;
				}

				GuildraidConf grc = Readonly.getInstance().findGuildraid(user.tmpGuildScene);
				if (grc == null) {
					rl.result = "请重新进入公会界面，刷新当前副本关卡";
					user.send(rl);
					return;
				}

				HerosBean hsb = user.getDao().findHeros(user.getUid());
				BattleStruct arg = new BattleStruct();

				// 1. 填充左边
				hsb.fill(null, null, arg.roles, arg.arts);
				arg.skin = hsb.getSkin();

				BattleResultStruct rtn = RemoteService.getInstance().askGroupSceneFight(arg, gb.getGid(), user.getUid(), index);
				if (rtn == null) {
					rl.result = "公会副本战斗服务维护中，请稍后再战.";
				} else {
					if (!Strings.isNullOrEmpty(rtn.result)) {
						rl.result = rtn.result;
					} else {
						rl.data = rtn.data;
						rl.harm = rtn.harm;
						// 1. 扣除次数
						gb.setSceneCount(gb.getSceneCount() - 1);
						// 2. 发送战斗奖励
						rl.awards.addAll(user.addItem(公会贡献, rtn.rCon, false, "公会副本"));

						// 3. 判断并发送击杀奖励
						if (rtn.kill > 0) {
							rl.kill = 1;
							rl.awards.addAll(user.addItem(公会贡献, grc.reward2, false, "公会副本击杀"));
							if (rtn.kill == 2) { // 首杀
//								rl.rewards.add(公会经验, grc.reward5);
							}
						}
					}
				}
			} catch (Exception ex) {
				rl.result = "公会副本战斗服务维护中，请稍后再战.";
			}
		} else {
			rl.result = "您不在公会中,请先选择一个公会加入.";
		}

		user.send(rl);
	}
}
