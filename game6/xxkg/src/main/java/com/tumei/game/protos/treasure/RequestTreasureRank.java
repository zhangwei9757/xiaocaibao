package com.tumei.game.protos.treasure;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.TreasureRankStruct;
import com.tumei.game.services.TreasureRankService;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 *
 */
@Component
public class RequestTreasureRank extends BaseProtocol {
	public int seq;

	class ReturnTreasureRank extends BaseProtocol {
		public int seq;

		public String result = "";

		public List<TreasureRankStruct> ranks = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnTreasureRank rl = new ReturnTreasureRank();
		rl.seq = seq;

		long uid = user.getUid();
		RoleBean rb = user.getDao().findRole(uid);

		rl.ranks = TreasureRankService.getInstance().getRanks(uid, rb.getNickname());

		user.send(rl);
	}
}
