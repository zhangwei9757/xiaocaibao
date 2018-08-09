package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.game.GameUser
import com.tumei.model.LimitReceiveBean
import com.tumei.model.beans.AwardBean
import com.tumei.modelconf.DailysupplyConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

import java.time.LocalDateTime
/**
 * Created by Administrator on 2018-7-24
 *
 * 12-13
 * 18-19
 * 21-22
 * 均领取一次奖励
 *
 */
class RequestLimitReceive extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        // 返回结果
        public String result = ""

        // 限时奖励
        public List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        // 可领取奖励的时间段
        List<Integer> times = Arrays.asList(12,18,21)
        LocalDateTime ldt = LocalDateTime.now()

        // 当前时间小时数
        Integer now = ldt.getHour()

        if (times.contains(now)) {

            LimitReceiveBean limitReceiveBean  = DaoGame.instance.findLimitReceive(user.getUid())
            // 刷新时间
            limitReceiveBean.flush()

            int index = times.indexOf(now)
            int[] count = limitReceiveBean.counts
            if (count[index] == 1) {
                r.result = "今日已领取"
            } else {
                DailysupplyConf dc = Readonly.instance.findDailysuppy(index + 1)
                r.awards.addAll(user.addItems(dc.rewards,"每日限时奖励领取"))
                limitReceiveBean.counts[index] = 1
            }
        } else {
            r.result = "未在领取时间段"
        }

        user.send(r)
    }
}

