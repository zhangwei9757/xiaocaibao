package com.tumei.groovy.commands.protos

import com.tumei.game.GameUser
import com.tumei.model.beans.AwardBean
import com.tumei.common.Readonly
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Created by leon on 2016/12/31.
 * 兑换码
 *
 */
class RequestCodeGet extends BaseProtocol {
    public int seq
    public String code


    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser)session
        ReturnCodeGet rl = new ReturnCodeGet()
        rl.seq = seq

        int[] awards = Readonly.getInstance().pressCode(user.getUid(), code)
        if (awards == null) {
            rl.result = "优惠码已使用过或不存在"
            user.send(rl)
            return
        }

        rl.awards.addAll(user.addItems(awards, "兑换码:" + code))
        user.send(rl)
    }
}

class ReturnCodeGet extends BaseProtocol {
    public int seq
    public String result = ""
    public List<AwardBean> awards = new ArrayList<>()
}
