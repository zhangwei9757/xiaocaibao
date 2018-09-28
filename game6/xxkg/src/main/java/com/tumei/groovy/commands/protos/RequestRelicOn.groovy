package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.beans.RelicBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/26
 * 激活圣物，只有第一次需要激活，已经激活的圣物不能再次激活
 *
 * 第一个圣物激活，relicid在前端如果为0，需要切换到这个圣物装备状态
 */
class RequestRelicOn extends BaseProtocol {
    public int seq

    // 圣物id
    public int relic

    class Return extends BaseProtocol {
        public int seq
        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        PackBean pb = DaoService.instance.findPack(user.uid)
        if (!pb.contains(relic + 1, 25)) {
            rci.result = ErrCode.圣物碎片不足
        } else {
            HerosBean hsb = DaoService.getInstance().findHeros(user.uid)
            RelicBean rb = hsb.addRelic(relic)
            if (rb == null) {
                rci.result = ErrCode.圣物已经激活
            } else {
                user.payItem(relic + 1, 25, "圣物首次激活")
            }
        }

        user.send(rci)
    }
}

