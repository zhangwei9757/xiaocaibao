package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.game.services.InvadingRankService
import com.tumei.model.beans.AwardBean
import com.tumei.model.limit.InvadingBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by zw on 2018/09/28
 * <p>
 * 怪兽入侵活动使用次元碎片
 */
class RequestInvadingDebrisAttack extends BaseProtocol {
    public int seq
    /**
     * 使用次元碎片数量
     * 1个
     * 10个
     */
    public int count

    class Return extends BaseProtocol {
        public int seq
        // 次元碎片攻击的奖励
        public List<AwardBean> awards = new ArrayList<>()
        // 击杀成功的奖励
        public List<AwardBean> killAwards = new ArrayList<>()

        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        InvadingRankService lrs = InvadingRankService.instance

        if (!lrs.isActive()) {
            rci.result = ErrCode.限时活动暂未开启
        } else {
            // 判断参数是否合法
            if (count != 1 && count != 10) {
                rci.result = ErrCode.未知参数
            } else {

                InvadingBean ib = DaoGame.instance.findInvading(user.uid)
                if (ib != null) {
                    // 获取指定奖励,左id ,右count
                    int[] awds = ib.getAttackAward(count)
                    if (awds[1] < 0) {
                        rci.result = "次元碎片数量不足"
                    } else if (awds[0] == -999) {
                        rci.result = "怪兽已死亡待复活状态中"
                    } else {
                        List<AwardBean> list = user.addItems(awds, true, "次元碎片攻击奖励")
                        ib.addList(list)
                        rci.awards = list
                        // 额外获取击杀奖励
                        int[] kills = ib.getKillAward(user.uid, user.name)
                        if (kills[0] != -1) {
                            rci.killAwards.addAll(user.addItems(kills, false, "怪兽击杀奖励"))
                        }
                    }
                } else {
                    rci.result = "获取攻击结果错误"
                }
            }

        }

        user.send(rci)
    }
}

