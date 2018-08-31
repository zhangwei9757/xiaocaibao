package com.tumei.groovy.commands.protos

import com.google.common.base.Strings
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.utils.ErrCode
import com.tumei.dto.arena.ArenaFightResult
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.model.beans.AwardBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by leon on 2016/12/31.
 */
class RequestHotArenaFight extends BaseProtocol {
    public int seq

    /**
     * 对手的排行[0,...
     */
    public int rank

    /**
     * 0: 挑战
     * 1: 快速挑战
     */
    public int mode

    class Return extends BaseProtocol {
        public int seq
        public String result = ""
        /**
         * 战报
         */
        public String data = ""
        /**
         * win = -1 的时候，表示当前挑战的角色所属的排名与实际不符，需要客户端重新刷新挑战角色，就是发送RequestArenaInfo协议
         * win = 1 表示本次挑战胜利
         */
        public int win

        // 当前战斗后活力
        public int spirit

        /**
         * 战斗结束后，自己的rank排名
         */
        public int rank

        /**
         * 奖励
         */
        public List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser)session

        Return rl = new Return()
        rl.seq = seq
        rl.rank = -1

        PackBean pb = user.getDao().findPack(user.getUid())
        int spirit = pb.flushSpirit(0)

        if (mode == 0) {
            if (spirit < 2) {
                rl.result = ErrCode.活力不足.name()
                user.send(rl)
                return
            }

            ArenaFightResult arf = RemoteService.getInstance().arenaFight(user.getUid(), rank)

            if (arf == null) {
                rl.result = "跨服竞技场维护中"
                user.send(rl)
                return
            } else if (!Strings.isNullOrEmpty(arf.reason)) {
                rl.win = -1
                rl.result = arf.reason
                user.send(rl)
                return
            }

            rl.data = arf.data
            rl.rank = arf.rank
            if (arf.up == 0) {
                rl.rank = -1
            }
            if (arf.win) {
                rl.win = 1

                int[] awards = Readonly.getInstance().getSarenarewardConfs().get(0).win
                rl.awards.addAll(user.addItems(awards, 1, true, "跨服竞技场一键挑战"))

            } else {
                rl.win = 2
            }

            rl.spirit = pb.flushSpirit(-2)
        } else {
            if (spirit < 2 * mode) {
                rl.result = ErrCode.活力不足.name()
                user.send(rl)
                return
            }

            // 需要判断等级就是要小
            rl.win = 1

            // 挑战奖励
            int[] awards = Readonly.getInstance().getSarenarewardConfs().get(0).win
            rl.awards.addAll(user.addItems(awards, mode, true, "跨服竞技场一键挑战"))
            rl.spirit = pb.flushSpirit(-2 * mode)
        }

        user.send(rl)
    }
}

