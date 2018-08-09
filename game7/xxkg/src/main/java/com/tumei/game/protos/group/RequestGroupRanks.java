package com.tumei.game.protos.group;

import com.tumei.common.RemoteService;
import com.tumei.common.webio.RankStruct;
import com.tumei.game.GameUser;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求公会推荐列表
 */
@Component
public class RequestGroupRanks extends BaseProtocol {
	public int seq;
	/**
	 * 0 - 等级排行榜，返回总经验
	 * 1 - 副本排行榜
	 */
	public int mode;

	class ReturnGroupRanks extends BaseProtocol {
		public int seq;
		public List<RankStruct> ranks;
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnGroupRanks rl = new ReturnGroupRanks();
		rl.seq = seq;

		try {
			// 远程控制副本进度和当前副本关卡
			List<RankStruct> ranks = RemoteService.getInstance().askGroupRanks(mode);
			if (ranks != null) {
			    rl.ranks = ranks;
			}

		} catch (Exception ex) {
		    user.error("error:" + ex.getMessage());
		}

		user.send(rl);
	}
}
