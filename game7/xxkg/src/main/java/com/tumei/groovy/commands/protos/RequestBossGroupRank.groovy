package com.tumei.groovy.commands.protos

import com.tumei.common.RemoteService
import com.tumei.dto.boss.BossGuildDto
import com.tumei.dto.boss.BossRoleDto
import com.tumei.game.GameUser
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 进入Boss战界面
 */
class RequestBossGroupRank extends BaseProtocol {
    int seq

    class Return extends BaseProtocol {
        int seq

        List<BossGuildDto> guilds

        String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        rci.guilds = RemoteService.instance.askBossGroupRanks()
        if (rci.guilds == null) {
            rci.result = "世界BOSS正在休息中,暂时无法挑战."
        }

        user.send(rci)
    }
}
