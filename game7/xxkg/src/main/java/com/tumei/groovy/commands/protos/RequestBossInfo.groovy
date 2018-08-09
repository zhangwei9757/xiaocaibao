package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.utils.Defs
import com.tumei.dto.boss.BossDto
import com.tumei.game.GameUser
import com.tumei.model.BossBean
import com.tumei.modelconf.BossConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 进入Boss战界面
 */
class RequestBossInfo extends BaseProtocol {
    int seq

    class Return extends BaseProtocol {
        int seq

        BossDto dto

        List<Integer> courage = new ArrayList<>()

        // 剩余的挑战次数
        int left

        // 下次可攻击的时间
        long next

        String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        if (user.level < Defs.Boss战等级) {
            rci.result = "首领战在领主" + Defs.Boss战等级 + "级开放"
            user.send(rci)
            return
        }

        BossDto dto = RemoteService.instance.askBossInfo(user.uid, user.name)
        if (dto == null) {
            rci.result = "世界BOSS正在休息中,暂时无法挑战."
        } else {
            rci.dto = dto

            BossBean boss = DaoGame.instance.findBoss(user.uid)
            boss.refresh()
            rci.left = boss.count
            rci.next = boss.next
            // 赋值一下挑战的boss的等级, 后续挑战的时候直接使用
            boss.level = dto.level

            BossConf bc = Readonly.instance.getBossConf(boss.level)
            for (int i = 0; i < boss.courageIdx; ++i) {
                int idx = boss.courage[i]
                rci.courage.add(bc.upatt[idx*2])
                rci.courage.add(bc.upatt[idx*2 + 1])
            }

        }

        user.send(rci)
    }
}
