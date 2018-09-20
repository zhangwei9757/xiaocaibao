package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.dto.limit.InvadingLoginDto
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.limit.InvadingBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/11
 * 怪兽入侵活动
 */
class RequestInvadingInfo extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        public int currentDay
        // 次元碎片数量
        public int debris
        // 次元碎片数量上限
        public int maxDebris
        // 生成碎片最终时间
        public long lastFlushDebris
        // 登陆奖励列表 第1-N天
        // 登陆状态: -1表示已过期需要补签 0表示未领取 1表示已领取 2表示已购买 3未到领取日期状态、下标对应活动第？天
        public List<InvadingLoginDto> loginList = new ArrayList<>()
        // 购买总次数（新的一天会跟随单价一起重置）
        public int buyTotal
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
                ib.flushDebris();
                rci.currentDay = ib.getCurrentDay()
                rci.debris = ib.debris
                if (ib.lastFlushDebris == 0) {
                    rci.lastFlushDebris = 0
                } else {
                    rci.lastFlushDebris = ib.lastFlushDebris + Defs.怪兽入侵碎片生成时间
                }
                rci.loginList = ib.createInvadingLoginDto()
                rci.maxDebris = Defs.怪兽入侵碎片上限
                rci.buyTotal = ib.buyTotal
            } else {
                rci.result = "获取怪兽入侵信息错误"
            }
        }
        user.send(rci)
    }
}

