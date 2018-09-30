package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.game.services.InvadingRankService
import com.tumei.model.limit.InvadingBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by zw on 2018/09/28
 * <p>
 * 怪兽入侵活动购买怪兽复活
 */
class RequestBuyResurgence extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 结果
        public String result = ""
        // 消费钻石
        public int gem
        // 当前血量
        public int blood
        // 怪兽血量上限
        public int maxBlood
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
            InvadingBean ib = DaoGame.instance.findInvading(user.uid)
            if (ib != null) {
                int buy = ib.buyResurgence(user.uid)
                if (buy == -1) {
                    rci.result = "不符合购买复活的要求"
                } else if (buy == -2) {
                    rci.result = "钻石不足"
                } else {
                    rci.gem = buy
                    rci.blood = ib.blood
                    rci.maxBlood = Defs.怪兽入侵血量上限
                }
            } else {
                rci.result = "购买怪兽复活错误"
            }
        }

        user.send(rci)
    }
}

