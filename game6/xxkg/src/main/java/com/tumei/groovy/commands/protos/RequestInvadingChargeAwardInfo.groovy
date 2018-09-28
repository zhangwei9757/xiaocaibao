package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.ErrCode
import com.tumei.common.webio.AwardStruct
import com.tumei.dto.limit.InvadingAwardDto
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.limit.InvadingBean
import com.tumei.modelconf.limit.InvtotalConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/12
 * 怪兽入侵活动拉取累计充值奖励列表
 */
class RequestInvadingChargeAwardInfo extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 结果
        public String result = ""
        // 累计充值奖励列表 依次为6、30……
        public List<InvadingAwardDto> chargesList = new ArrayList<>();
        // 活动期间累计充值金额 单位：分
        public int chargeTotal

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
            InvadingBean ib = DaoService.instance.findInvading(user.uid)
            if (ib != null) {
                ib.flushDebris()
                List<InvtotalConf> confs = Readonly.instance.getInvtotalConfs()

                for (InvtotalConf ic : confs) {
                    InvadingAwardDto iad = new InvadingAwardDto()
                    List<AwardStruct> list = new ArrayList<>()
                    int[] reward = ic.reward
                    for (int i = 0; i < reward.length; ++i) {
                        list.add(new AwardStruct(reward[i], reward[++i]))
                    }
                    // 些处获取充值信息，在活动启动时就读取了，不能动态更改配置文件，会导致异常
                    iad.status = ib.getReceiveStatu(ic.cost * 100);// 配置为元，转化为分
                    iad.awards = list
                    iad.cost = ic.cost
                    rci.chargesList.add(iad)
                }
                rci.chargeTotal = ib.chargeTotal
            } else {
                rci.result = "获取累计充值奖励错误"
            }
        }

        user.send(rci)
    }
}

