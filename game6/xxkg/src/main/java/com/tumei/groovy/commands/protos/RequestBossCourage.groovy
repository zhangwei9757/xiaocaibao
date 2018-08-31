package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.dto.boss.BossDto
import com.tumei.game.GameUser
import com.tumei.model.BossBean
import com.tumei.model.PackBean
import com.tumei.modelconf.BossConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 进入Boss战界面
 */
class RequestBossCourage extends BaseProtocol {
    int seq

    class Return extends BaseProtocol {
        int seq

        // 鼓舞的属性
        int key
        int val

        String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        BossBean boss = DaoService.instance.findBoss(user.uid)
        boss.refresh()

        if (!boss.canCourage()) {
            rci.result = "今日鼓舞次数已达到上限"
        } else {
            PackBean pb = DaoService.instance.findPack(user.uid)
            if (!pb.contains(Defs.钻石, 50)) {
                rci.result = ErrCode.钻石不足.name()
            } else {
                BossConf bc = Readonly.instance.getBossConf(boss.level)
                int idx = boss.courage[boss.courageIdx]
                rci.key = bc.upatt[idx*2]
                rci.val = bc.upatt[idx*2 + 1]

                pb.payItem(Defs.钻石, 50, "鼓励")
                boss.doCourage()
            }
        }

        user.send(rci)
    }
}
