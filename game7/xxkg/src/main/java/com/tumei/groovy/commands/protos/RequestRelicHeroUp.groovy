package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.beans.RelicBean
import com.tumei.modelconf.LeheupConf
import com.tumei.modelconf.LhwcostConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 传奇英雄升级
 */
class RequestRelicHeroUp extends BaseProtocol {
    public int seq

    // 圣物id
    public int relic

    /**
     * 0: 普通升级
     * 1: 钻石升级
     * 2: 觉醒英雄
     *
     */
    public int mode

    class Return extends BaseProtocol {
        public int seq

        public int success

        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq
        HerosBean hsb = DaoGame.getInstance().findHeros(user.uid)
        RelicBean rb = hsb.getRelics().getOrDefault(relic, null)
        if (rb == null) {
            rci.result = ErrCode.对应的圣物不存在
        } else if (rb.hero == 0) {
            rci.result = ErrCode.传奇英雄未激活
        } else {
            if (mode == 2) { // 英雄觉醒
                LhwcostConf conf = Readonly.instance.findLhwcost(rb.hwlvl)

                PackBean pb = DaoGame.instance.findPack(user.uid)
                if (!pb.contains(rb.id + 1, conf.cost1)) {
                    rci.result = "圣物碎片不足"
                    user.send(rci)
                    return
                }
                if (!pb.contains(Defs.圣灵石, conf.cost2)) {
                    rci.result = ErrCode.圣灵石不足
                    user.send(rci)
                    return
                }
                if (!pb.contains(160 + rb.hero, conf.cost3)) {
                    rci.result = "卷轴不足"
                    user.send(rci)
                    return
                }

                user.payItem(rb.id + 1, conf.cost1, "传奇升级")
                user.payItem(Defs.圣灵石, conf.cost2, "传奇升级")
                user.payItem(160 + rb.hero, conf.cost3, "传奇升级")

                ++rb.hwlvl
            } else { // 英雄升级
                LeheupConf conf = Readonly.instance.findLeheup(rb.hlvl)
                if (conf.cost1 < 0) {
                    rci.result = "传奇英雄等级已达到上限"
                    user.send(rci)
                    return
                }

                PackBean pb = DaoGame.instance.findPack(user.uid)
                if (!pb.contains(Defs.金币, conf.cost1)) {
                    rci.result = ErrCode.金币不足
                    user.send(rci)
                    return
                }
                if (!pb.contains(rb.id + 1, conf.cost2)) {
                    rci.result = "圣物碎片不足"
                    user.send(rci)
                    return
                }
                if (!pb.contains(Defs.圣灵石, conf.cost3)) {
                    rci.result = ErrCode.圣灵石不足
                    user.send(rci)
                    return
                }

                int rate = conf.rate
                if (mode == 1) {
                    rate = 100

                    if (!pb.contains(Defs.钻石, conf.cost4)) {
                        rci.result = ErrCode.钻石不足
                        user.send(rci)
                        return
                    }
                    user.payItem(Defs.钻石, conf.cost4, "传奇升级")
                }

                user.payItem(Defs.金币, conf.cost1, "传奇升级")
                user.payItem(rb.id + 1, conf.cost2, "传奇升级")
                user.payItem(Defs.圣灵石, conf.cost3, "传奇升级")

                if (RandomUtil.getBetween(1, 100) <= rate) {
                    ++rb.hlvl
                    rci.success = 1
                } else {
                    rci.result = "传奇英雄升级失败"
                }
            }
        }

        user.send(rci)
    }
}

