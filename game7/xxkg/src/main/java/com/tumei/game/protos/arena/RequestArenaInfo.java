package com.tumei.game.protos.arena;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.RankStruct;
import com.tumei.model.PackBean;
import com.tumei.model.RankBean;
import com.tumei.websocket.WebSocketUser;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.荣誉;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
@Component
public class RequestArenaInfo extends BaseProtocol {
	public int seq;

	class ReturnArenaInfo extends BaseProtocol {
		public int seq;
		/**
		 * 排名[0,...]
		 */
		public int rank;
		/**
		 * 当前荣誉
		 */
		public int honor;
		/**
		 * 活力同步
		 */
		public int spirit;
		/**
		 * 随机出来的对手
		 */
		public List<RankStruct> peers = new ArrayList<>();
	}

	@Override
	public void onProcess(WebSocketUser session) {
		GameUser user = (GameUser) session;

		ReturnArenaInfo rci = new ReturnArenaInfo();
		rci.seq = seq;

		RankBean rb = user.getRank();
		rci.rank = rb.getRank();
		rci.peers = user.getArenaPeers(rci.rank);

		PackBean pb = user.getDao().findPack(user.getUid());
		rci.honor = pb.getItemCount(荣誉);
		rci.spirit = pb.flushSpirit(0);
		user.send(rci);
	}
}
