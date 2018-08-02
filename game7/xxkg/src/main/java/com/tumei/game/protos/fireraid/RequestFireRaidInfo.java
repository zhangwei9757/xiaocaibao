package com.tumei.game.protos.fireraid;

import com.tumei.game.protos.structs.ChoiceStruct;
import com.tumei.websocket.WebSocketUser;
import com.tumei.game.GameUser;
import com.tumei.model.FireRaidBean;
import com.tumei.model.RoleBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 *
 * 燃烧远征信息读取
 *
 */
@Component
public class RequestFireRaidInfo extends BaseProtocol {
    public int seq;

    class ReturnFireRaidInfo extends BaseProtocol {
		public int seq;
		/**
		 * 当前挂机的关卡，从1开始
		 */
		public int scene;
		/**
		 * 当前总的星数
		 */
		public int stars;
		/**
		 * 历史最高总星数
		 */
		public int peek;
		/**
		 * 最高三星的关卡,可扫荡到这个关卡所在的章节，本章节不能扫荡，从1开始
		 */
		public int peekScene;
		/**
		 * 剩余星数
		 */
		public int leftStar;
		/**
		 * 剩余重置次数
		 */
		public int left;
		/**
		 * 已经失败需要重置
		 */
		public int reset;
		/**
		 * 当前加成
		 */
		public HashMap<Integer, Integer> buffs;
		/**
		 * 每一关的星数
		 */
		public int[] starlist = new int[3];
		/**
		 * 如果当前有没有选择的buff,则展示出来，里面一共有三组数据
		 * key表示属性，value是值，三对数据分为3星，6星，9星
		 */
		public List<ChoiceStruct> choise;
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;
		ReturnFireRaidInfo rl = new ReturnFireRaidInfo();
		rl.seq = seq;
		if (user.getLevel() < 21) {
			user.send(rl);
			return;
		}

		RoleBean rb = user.getDao().findRole(user.getUid());
		FireRaidBean frb = user.getDao().findFireRaid(user.getUid());
		frb.flush();

		rl.scene = frb.getScene();
		rl.stars = frb.getTotalStars();
		rl.peek = frb.getPeekStars();
		rl.peekScene = frb.getPeek3Stars();
		rl.leftStar = frb.getLeftStars();
		rl.left = frb.getResetCount();
		rl.buffs = frb.getBuffs();
		rl.choise = frb.getChoise();
		rl.reset = (frb.isFailed() ? 1 : 0);

		// 根据当前3关卡 返回这3关对应的星数
		int s = (rl.scene - 1) / 3 * 3;
		List<Integer> sss = frb.getStars();
		for (int i = 0; i < 3; ++i) {
			if (s + i < sss.size()) {
				rl.starlist[i] = sss.get(s + i);
			}
		}
        user.send(rl);
    }
}
