package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.GuildraidConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupSceneOverAwards extends BaseProtocol {
	public int seq;
	public int scene;

	class ReturnGroupSceneOverAwards extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupSceneOverAwards rl = new ReturnGroupSceneOverAwards();
		rl.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			try {
				if (scene < gb.getScene()) {
					if (gb.getSceneAward().contains(scene)) {
						rl.result = "已经领取过该章节的奖励";
					}
				} else if (scene > gb.getScene()) {
					rl.result = "本章节未通关，无法获取奖励";
				} else {
					if (user.tmpGuildProgress == null) {
						rl.result = "副本进度不存在，请刷新该页面";
					} else if (Arrays.stream(user.tmpGuildProgress).anyMatch((p) -> (p != 100))) {
						rl.result = "本章节未通关，无法获取奖励";
					}
				}

				if (Strings.isNullOrEmpty(rl.result)){
					gb.getSceneAward().add(scene);
					// 发送奖励
					GuildraidConf gc = Readonly.getInstance().findGuildraid(scene);
					rl.awards.addAll(user.addItems(gc.reward3, "公会副本通关"));
				}
			} catch (Exception ex) {
				rl.result = "无法获取本章副本对应阵营的奖励";
			}
		}

		user.send(rl);
	}
}
