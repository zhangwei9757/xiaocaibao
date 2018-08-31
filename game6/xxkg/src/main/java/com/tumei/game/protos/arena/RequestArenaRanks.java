package com.tumei.game.protos.arena;

import com.tumei.websocket.SessionUser;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.RankStruct;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestArenaRanks extends BaseProtocol {
	public int seq;

	class ReturnArenaRanks extends BaseProtocol {
		public int seq;
		/**
		 * 随机出来的对手
		 */
		public List<RankStruct> ranks = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnArenaRanks rci = new ReturnArenaRanks();
		rci.seq = seq;

		rci.ranks = user.getTopRanks(20);

		user.send(rci);
	}
}
