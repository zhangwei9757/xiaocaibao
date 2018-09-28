package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.beans.RelicBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/26
 * 改变正在使用的圣物
 */
class RequestRelicChange extends BaseProtocol {
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
        HerosBean hsb = DaoService.getInstance().findHeros(user.uid)
        RelicBean rb = hsb.getRelics().getOrDefault(relic, null)
        if (rb == null) {
            rci.result = ErrCode.对应的圣物不存在
        } else {
            hsb.setRelicid(rb.id)
        }

        user.send(rci)
    }
}

