package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.beans.AwardBean
import com.tumei.model.limit.InvadingBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/11
 * 怪兽入侵活动领取累计充值档位对应奖励
 */
class RequestInvadingChargeAward extends BaseProtocol {
    public int seq
    /**
     * 累计充值档位金额 如：600, 3000, 19800 ...单位分
     * **/
    public int rmb

    class Return extends BaseProtocol {
        public int seq
        // 领取成功的奖励
        public List<AwardBean> awards = new ArrayList<>()

        public String result = ""
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
            if (rmb <= 0) {
                rci.result = "领取失败"
            } else {
                InvadingBean ib = DaoService.instance.findInvading(user.uid)
                if (ib != null) {
                    // 获取指定奖励,左id ,右count
                    int[] awds = ib.getChargeAward(rmb)
                    if (awds[0] == -1) {
                        rci.result = "配置读取失败"
                    } else if (awds[0] == -2) {
                        rci.result = "不符合领取条件"
                    } else {
                        rci.awards.addAll(user.addItems(awds, false, "限时活动累计充值奖励"))
                    }
                } else {
                    rci.result = "获取领取结果错误"
                }
            }
        }

        user.send(rci)
    }
}

