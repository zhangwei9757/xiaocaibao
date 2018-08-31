package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.common.RemoteService;
import com.tumei.game.GameUser;
import com.tumei.common.group.GroupSceneRoleStruct;
import com.tumei.common.group.GroupSceneStruct;
import com.tumei.model.GroupBean;
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
public class RequestGroupSceneInfo extends BaseProtocol {
	public int seq;

	class ReturnGroupSceneInfo extends BaseProtocol {
		public int seq;
		public String result = "";
		// 当前关卡，从1开始
		public int scene;
		// 当前进度, [0,100]
		public List<Integer> progress;
		public int[] fetch = new int[4];
		// 剩余挑战次数
		public int count;
		// 下次恢复的剩余时间，单位秒
		public long time;
		public int buyCount;
		public List<GroupSceneRoleStruct> roles = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser)session;

		ReturnGroupSceneInfo rl = new ReturnGroupSceneInfo();
		rl.seq = seq;

		GroupBean gb = user.getDao().findGroup(user.getUid());
		if (gb.getGid() > 0) {
			try {
				// 本地控制攻击次数和恢复时间
				gb.flush(user.getVip());
				rl.count = gb.getSceneCount();
				rl.time = gb.getSceneTime().getTime() / 1000;
				rl.buyCount = gb.getBuyCount();

				// 远程控制副本进度和当前副本关卡
				GroupSceneStruct gss = RemoteService.getInstance().askGroupSceneInfo(gb.getGid(), user.getUid());
				if (gss != null) {
					if (!Strings.isNullOrEmpty(gss.result)) {
						rl.result = "你已经不在当前公会,无法挑战副本!";
					} else {
						rl.scene = gss.scene;
						if (gss.scene != gb.getScene()) {
							gb.setScene(gss.scene);
							int[] fetch = gb.getFetch();
							for (int i = 0; i < 4; ++i) {
								fetch[i] = 0;
							}
						}
						rl.fetch = gb.getFetch();

						user.tmpGuildScene = rl.scene;
						rl.progress = new ArrayList<>();
						Arrays.stream(gss.progress).forEach(ii -> rl.progress.add(ii));
						user.tmpGuildProgress = gss.progress;

						gss.roles.forEach(r -> {
							rl.roles.add(r);
						});
					}
				} else {
					rl.result = "你已经不在当前公会,无法挑战副本!";
				}
			} catch (Exception ex) {
				user.error("请求公会副本信息失败:" + ex.getMessage());
			}
		} else {
			rl.result = "你已经不在当前公会,无法挑战副本!";
		}

		user.send(rl);
	}
}
