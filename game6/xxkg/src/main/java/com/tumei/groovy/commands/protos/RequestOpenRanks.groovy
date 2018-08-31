package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.dto.db2proto.NameValue
import com.tumei.game.GameUser
import com.tumei.game.services.OpenRankService
import com.tumei.model.HerosBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 开服活动各种排名
 */
class RequestOpenRanks extends BaseProtocol {
    int seq

    class Return extends BaseProtocol {
        int seq
        String result = ""
        List<NameValue> powers = new ArrayList<>()
        List<NameValue> scenes = new ArrayList<>()
        List<NameValue> levels = new ArrayList<>()
        List<NameValue> stars = new ArrayList<>()
        List<NameValue> charges = new ArrayList<>()
        List<NameValue> spends = new ArrayList<>()
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        HerosBean hsb = DaoService.getInstance().findHeros(user.uid)
        user.calcPower(hsb)

        if (!OpenRankService.getInstance().copyInfos(user.uid, rci.powers, rci.scenes, rci.levels, rci.stars, rci.charges, rci.spends)) {
            rci.result = "开服活动已结束"
        }

        user.send(rci)
    }
}
