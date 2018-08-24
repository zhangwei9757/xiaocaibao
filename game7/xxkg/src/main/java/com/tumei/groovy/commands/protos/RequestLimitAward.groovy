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
    /**
     * 箱子位置[0...4]
     * **/
    public int index

    class Return extends BaseProtocol {
        public int seq

        public int[] awards = new int[2]

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
            // 判断参数是否合法
            if (index < 0 || index > 4) {
                rci.result = ErrCode.未知参数
            } else {
                // 获取指定奖励,左id ,右count
                int[] awds = lrs.requireAward(user.uid, index)

                if (awds[0] == 0) {
                    rci.result = "条件未达成，无法领取奖励"
                } else if (awds[0] == -1) {
                    rci.result = "已领取过奖励"
                } else {
                    user.addItems(awds, false, "限时活动")
                    rci.awards = awds
                }
            }
        }

        user.send(rci)
    }
}

