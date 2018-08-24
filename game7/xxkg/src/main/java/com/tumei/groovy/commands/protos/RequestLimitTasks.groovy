package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.utils.ErrCode
import com.tumei.dto.db2proto.NameValue
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.WarBean
import com.tumei.model.beans.war.WarTask
import com.tumei.model.limit.LimitRankBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

import java.rmi.server.UID

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 * 限时活动:
 *
 * 1. 注灵狂欢
 * 2. 终极抽奖
 *
 */
class RequestLimitTasks extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        /**
         * 最多给出11个排名，最后一个一定是自己的排名信息
         *
         *
         * 排名rank值小于1的 一定是差不到排名,前端自己想怎么展示
         *
         *
         * 最后一个是自己，如果自己曾经参与过注灵狂欢的活动，才会有自己的数据，否则传递过来的NameValue中，name是空字符串
         * value是0，排名是<1的，前端在这里获取的value就是实际在活动期间的注灵次数
         *
         */
        public List<NameValue> ranks = new ArrayList<>()

        // 活动开始时间 秒
        public int begins
        // 活动结束时间 秒
        public int ends
        // 活动次数达标后的奖励，客户端可以不用再查表了
        public int[] rewards
        // 对应位置是否领取 0：未领取 1：已领取
        public int[] receive = new int[5]

        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        LimitRankService lrs = LimitRankService.instance

        if (!lrs.isActive()) {
            rci.result = ErrCode.限时活动暂未开启
        } else {

            rci.begins = lrs.begin
            rci.ends = lrs.end
            //rci.rewards = lrs.rewards
            rci.receive = lrs.getRewardFlag(user.uid)
            rci.ranks = lrs.getRanks(user.uid)
        }

        user.send(rci)
    }
}

