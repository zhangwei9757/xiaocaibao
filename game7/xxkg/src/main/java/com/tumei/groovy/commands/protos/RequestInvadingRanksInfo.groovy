package com.tumei.groovy.commands.protos

import com.tumei.common.Readonly
import com.tumei.common.utils.ErrCode
import com.tumei.common.webio.AwardStruct
import com.tumei.dto.db2proto.NameValue
import com.tumei.game.GameUser
import com.tumei.game.services.InvadingRankService
import com.tumei.modelconf.limit.InvrankConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by zw on 2018/09/28
 * <p>
 * 怪兽入侵活动拉取击杀次数排行榜
 */
class RequestInvadingRanksInfo extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 结果
        public String result = ""
        // 活动击杀排行榜
        public List<NameValue> ranks = new ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        InvadingRankService lrs = InvadingRankService.instance

        if (!lrs.isActive()) {
            rci.result = ErrCode.限时活动暂未开启
        } else {
            rci.ranks = lrs.getRanks(user.uid)
            List<InvrankConf> ics = Readonly.instance.getInvrankConfs()

            for (int i = 0; i < ics.size(); ++i) {
                List<AwardStruct> ass = new ArrayList<>()
                int[] receive = ics.get(i).reward
                for (int j = 0; j < receive.length; ++j) {
                    ass.add(new AwardStruct(receive[j], receive[++j]))
                }
                rci.ranks.get(i).setAwards(ass)
            }
        }
        user.send(rci)
    }
}

