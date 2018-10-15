package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.model.ActivityBean
import com.tumei.model.beans.AwardBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * 根据客户端发送的字段内容求签名
 */
class RequestAdsAward extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result
        // 具体奖励
        public List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        ActivityBean ab = DaoGame.instance.findActivity(user.uid)
        ab.flush()

        if (ab.adsCount >= 10) {
            rci.result = ErrCode.今日广告奖励已达上限
        } else {
            ++ab.adsCount

            rci.awards.addAll(user.addItem(Defs.钻石, 10L, false, "广告"))
        }

        user.send(rci)
    }
}

