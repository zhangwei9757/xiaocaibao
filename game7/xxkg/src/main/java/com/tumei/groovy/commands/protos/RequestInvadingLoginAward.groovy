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
 * 怪兽入侵活动碎片攻击信息
 */
class RequestInvadingLoginAward extends BaseProtocol {
    public int seq
    // 登陆的第几天
    public int position

    class Return extends BaseProtocol {
        public int seq

        public String result = ""
        // 领取的奖励
        public List<AwardBean> awards = new ArrayList<>()
        // 所需的钻石
        public int gem
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
            InvadingBean invading = DaoGame.instance.findInvading(user.uid)
            if (invading != null) {
                int[][] receive = invading.getLoginddAward(user.uid, position - 1)
                if (receive == null) {
                    rci.result = "不符合条件"
                } else if (receive[0][0] == 0) {
                    rci.result = "操作失败"
                } else if (receive[0][0] < 0) {
                    rci.result = "钻石不足"
                    rci.gem = receive[1][0]
                } else {
                    rci.gem = receive[1][0]
                    rci.awards.addAll(user.addItems(receive[0], false, "限时活动登陆奖励"))
                }
            } else {
                rci.result = "不符合条件"
            }
        }

        user.send(rci)
    }
}

