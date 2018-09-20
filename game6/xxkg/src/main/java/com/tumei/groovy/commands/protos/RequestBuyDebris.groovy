package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.limit.InvadingBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/12
 * 怪兽入侵活动购买次元碎片
 */
class RequestBuyDebris extends BaseProtocol {
    public int seq
    /**
     * 购买的次元碎片数量
     * **/
    public int count

    class Return extends BaseProtocol {
        public int seq
        // 结果
        public String result = ""
        // 消费钻石
        public int gem
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
            // 判断参数是否合法
            if (count < 0) {
                rci.result = ErrCode.未知参数
            } else {

                InvadingBean ib = DaoService.instance.findInvading(user.uid)
                if (ib != null) {
                    int buy = ib.buyDebris(user.uid, count)
                    if (buy == -1) {
                        rci.result = "钻石不足"
                    } else {
                        rci.gem = buy
                    }
                } else {
                    rci.result = "购买碎片错误"
                }

            }
        }

        user.send(rci)
    }
}

