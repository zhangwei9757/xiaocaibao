package com.tumei.groovy.commands.protos

import com.tumei.common.utils.ErrCode
import com.tumei.dto.db2proto.NameValue
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.beans.AwardBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 限时活动 达到一定的完成次数，可以领取个人奖励
 *
 * 1. 注灵狂欢
 * 2. 终极抽奖
 *
 */
class RequestLimitAward extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        public List<AwardBean> awards = new ArrayList<>()

        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        LimitRankService lrs = LimitRankService.instance

        if (!lrs.isActive()) {
            rci.result = ErrCode.限时活动暂未开启
        } else {
            List<Long> awds = lrs.requireAward(user.uid)
            if (awds == null || awds.size() <= 0) {
                rci.result = "条件未达成，无法领取奖励"
            } else {
                rci.awards.addAll(user.addItems(awds, true, "限时活动"))
            }
        }

        user.send(rci)
    }
}

