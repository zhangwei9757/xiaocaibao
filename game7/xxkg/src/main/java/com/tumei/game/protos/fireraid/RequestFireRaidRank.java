package com.tumei.game.protos.fireraid;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.RaidRankStruct;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 * 燃烧远征信息读取
 *
 */
@Component
public class RequestFireRaidRank extends BaseProtocol {
    public int seq;

    class ReturnFireRaidRank extends BaseProtocol {
		public int seq;
		public String result = "";
		/**
		 * 自己的排名和星数在最后一条数据
		 */
		public List<RaidRankStruct> ranks = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnFireRaidRank rl = new ReturnFireRaidRank();
		rl.seq = seq;

		rl.ranks = user.getRaidRanks();

        user.send(rl);
    }
}
