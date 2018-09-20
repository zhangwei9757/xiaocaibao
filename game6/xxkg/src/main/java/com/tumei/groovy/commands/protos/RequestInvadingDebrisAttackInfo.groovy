package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.common.webio.AwardStruct
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.beans.AwardBean
import com.tumei.model.limit.InvadingBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/12
 * 怪兽入侵活动碎片攻击信息
 */
class RequestInvadingDebrisAttackInfo extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        public String result = ""
        // 碎片攻击历史所有实际所得奖励
        public List<AwardBean> debrisList = new ArrayList<>();
        // 碎片击杀怪兽所有奖励,下标对应次数的奖励
        public List<List<AwardBean>> killList = new ArrayList<>();
        // 当前血量
        public int blood
        // 怪兽血量上限
        public int maxBlood
        // kill次数
        public int kill
        // 复活最终时间
        public long resurgence;
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        LimitRankService lrs = LimitRankService.instance

        if (!lrs.isActive()) {
            rci.result = ErrCode.限时活动暂未开启
        } else {
            InvadingBean invading = DaoService.instance.findInvading(user.uid)
            if (invading != null) {
                invading.flushDebris();
                rci.debrisList = invading.debrisList
                rci.killList = invading.killList
                rci.blood = invading.blood
                rci.maxBlood = Defs.怪兽入侵血量上限
                rci.kill = invading.kill
                rci.resurgence = invading.resurgence
            } else {
                rci.result = "获取碎片攻击信息错误"
            }
        }

        user.send(rci)
    }
}

