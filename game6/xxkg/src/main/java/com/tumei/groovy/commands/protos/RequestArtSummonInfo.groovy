package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.game.protos.structs.StoreStruct
import com.tumei.model.ActivityBean
import com.tumei.model.PackBean
import com.tumei.model.StoreBean
import com.tumei.modelconf.VipConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 神器召唤信息页面
 */
class RequestArtSummonInfo extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        // 今日可进行召唤的次数
        public int count

        // 本次是否免费, 0:不免费，1:免费
        public int free

        // 总召唤次数，自己判定是否满足10次，20次规则
        public int total

        public List<StoreStruct> store;

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq
        if (user.level < Defs.神器等级) {
            rci.result = "领主等级不足"
            user.send(rci)
            return
        }

        ActivityBean ab = DaoService.instance.findActivity(user.uid)
        ab.flushArtSummon()

        VipConf vc = Readonly.instance.findVip(user.vip)
        rci.count = vc.artcall - ab.getArtToday()

        rci.free = ab.getArtFree()

        rci.total = ab.getArtTotal()

        user.send(rci)
    }
}

