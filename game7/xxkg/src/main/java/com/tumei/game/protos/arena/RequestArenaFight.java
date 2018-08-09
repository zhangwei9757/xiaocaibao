package com.tumei.game.protos.arena;

import com.tumei.common.Readonly;
import com.tumei.dto.battle.FightResult;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.dto.battle.HerosStruct;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.RankStruct;
import com.tumei.game.services.LocalArenaService;
import com.tumei.model.*;
import com.tumei.model.beans.AwardBean;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.荣誉;
import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 *
 * 竞技场挑战
 *
 */
@Component
public class RequestArenaFight extends BaseProtocol {
    public int seq;

    /**
     * 拉取的本人对手序号[0, ..]
     */
    public int index;

    /**
     * 0: 挑战
     * 1: 快速挑战
     */
    public int mode;

    class ReturnArenaFight extends BaseProtocol {
		public int seq;
		public String result = "";
        /**
         * 战报
         */
        public String data = "";
        /**
		 * win = -1 的时候，表示当前挑战的角色所属的排名与实际不符，需要客户端重新刷新挑战角色，就是发送RequestArenaInfo协议
         * win = 1 表示本次挑战胜利
         */
        public int win;
		// 当前战斗后活力
        public int spirit;
        /**
         * 战斗结束后，自己的rank排名
         */
        public int rank;
        /**
         * 荣誉奖励
         */
        public List<AwardBean> awards = new ArrayList<>();
        /**
         * 挑战奖励，第一个是抽卡得到的，后两个是展示的，不论他抽的哪个卡
         */
        public List<AwardBean> rewards = new ArrayList<>();
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session;

        ReturnArenaFight rl = new ReturnArenaFight();
		rl.seq = seq;
        rl.rank = -1;

        if (user.tmpPeers == null || index < 0 || index >= user.tmpPeers.size()) {
            rl.result = "竞技场对手不存在，请重新刷新竞技场";
            user.send(rl);
            return;
        }

        PackBean pb = user.getDao().findPack(user.getUid());
        int spirit = pb.flushSpirit(0);

        RankStruct rs = user.tmpPeers.get(index);
        if (rs == null) {
            rl.result = "不存在的竞技场对手";
            user.send(rl);
            return;
        }

        if (mode == 0) {
            if (spirit < 2) {
                rl.result = ErrCode.活力不足.name();
                user.send(rl);
                return;
            }

            // 2. 对手
            FightResult r = null;
            HerosBean hsb = user.getDao().findHeros(user.getUid());

            if (rs.id < 100000) { // npc
                HerosStruct hss = new HerosStruct();
				RankBean rb = user.getRankPeer(rs.id);
				rb.fillHeros(hss.heros);
                r = GameServer.getInstance().getBattleSystem().doBattle(hsb.createHerosStruct(), hss);
            } else {
                HerosBean osb = user.getDao().findHeros(rs.id);
                r = GameServer.getInstance().getBattleSystem().doBattle(hsb.createHerosStruct(), osb.createHerosStruct());
            }

            rl.data = r.data;

            if (r.win == 1) { // 只有胜利才需要根据排名变化计算钻石
                rl.win = 1;

                // 1. 交换2个人的排名
				int rtn = LocalArenaService.getInstance().exchange(user.getUid(), rs.id, rs.rank);

                // 2. 根据排名差，奖励该玩家钻石
				if (rtn < 0) {
					rl.win = rtn;
					rl.result = "对手的排名发生变更";
                    user.send(rl);
                    return;
                } else if (rtn > 0) { // 只有超过最高名次才进入
                    GameServer.getInstance().sendAwardMail(user.getUid(), "竞技场奖励", "最高名次提升到" + (rs.rank + 1) + ".", 钻石 + "," + rtn);

                    if (rs.id > 100000) { // 非npc,通知降低名次
                        RankBean peer = user.getRankPeer(rs.id);
                        RoleBean roleBean = user.getDao().findRole(user.getUid());
                        GameServer.getInstance().sendInfoMail(rs.id, "竞技场", "玩家【" + Defs.getColorString(roleBean.getGrade(), roleBean.getNickname()) + "】在竞技场中轻松击败了你,你的竞技场排名降至第" + Defs.getColorString(5, (peer.getRank() + 1) + "") + "名.");
                    }
                }


                RankBean rb = user.getRank();
                rl.rank = rb.getRank();

                rl.awards.addAll(user.addItem(荣誉, 50, false, "竞技场"));

                // 挑战奖励
                int[] awards = Readonly.getInstance().getArenaRewards(0).win;
                int idx = RandomUtil.randomByWeight(awards);
                rl.rewards.addAll(user.addItem(idx, 1, true, "竞技场"));

                ActivityBean ab = user.getDao().findActivity(user.getUid());
                ab.flushCampaign();
                ab.incCampaign1(1);
            } else {
                rl.awards.addAll(user.addItem(荣誉, 30, false, "竞技场"));
            }

            rl.spirit = pb.flushSpirit(-2);
            user.pushDailyTask(9, 1);
        } else {

            if (spirit < 2 * mode) {
                rl.result = ErrCode.活力不足.name();
                user.send(rl);
                return;
            }

            // 需要判断等级就是要小
            rl.win = 1;
            rl.awards.addAll(user.addItem(荣誉, 50 * mode, false, "竞技场"));
            // 挑战奖励
            int[] awards = Readonly.getInstance().getArenaRewards(0).win;
			for (int i = 0; i < mode; ++i) {
                int idx = RandomUtil.randomByWeight(awards);
                rl.awards.addAll(user.addItem(idx, 1, true, "竞技场"));
            }
            rl.spirit = pb.flushSpirit(-2 * mode);

            ActivityBean ab = user.getDao().findActivity(user.getUid());
            ab.flushCampaign();
            ab.incCampaign1(mode);
            user.pushDailyTask(9, mode);
        }

        user.send(rl);
    }
}
