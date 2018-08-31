package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.dto.boss.BossDto
import com.tumei.game.GameUser
import com.tumei.model.BossBean
import com.tumei.modelconf.BossConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 进入Boss战界面
 */
class RequestBossLife extends BaseProtocol {
    int seq

    class Return extends BaseProtocol {
        int seq

        long life

        String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        long life = RemoteService.instance.askBossLife()
        if (life < 0) {
            rci.result = "世界BOSS正在休息中,暂时无法挑战."
        } else {
            rci.life = life
        }

        user.send(rci)
    }
}
