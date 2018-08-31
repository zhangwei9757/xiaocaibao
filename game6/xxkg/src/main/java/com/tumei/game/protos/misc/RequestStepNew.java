package com.tumei.game.protos.misc;

import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.RankStruct;
import com.tumei.model.PackBean;
import com.tumei.model.RankBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.荣誉;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 新手进度记录
 */
@Component
public class RequestStepNew extends BaseProtocol {
	public int seq;
	public int newbie;

	class ReturnStepNew extends BaseProtocol {
		public int seq;
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnStepNew rci = new ReturnStepNew();
		rci.seq = seq;

		RoleBean rb = user.getDao().findRole(user.getUid());
		rb.setNewbie(newbie);

		user.send(rci);
	}
}
