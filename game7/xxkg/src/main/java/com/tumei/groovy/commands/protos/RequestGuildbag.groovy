package com.tumei.groovy.commands.protos

import com.tumei.common.RemoteService
import com.tumei.dto.guildbag.GuildBagDtos
import com.tumei.game.GameUser
import com.tumei.model.GroupBean
import com.tumei.model.beans.guildbag.GuildbagStruct
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser
import org.springframework.stereotype.Component


/**
 * Created by zw on 2018-7-31
 * 公会红包
 *
 */
@Component
class RequestGuildbag extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result = ""
        /**
         * 公会所有红包
         **/
        public List<GuildbagStruct> gbs = new  ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        // 远程拉取玩家所在公会的红包
        GroupBean gb = user.getDao().findGroup(user.getUid())
        GuildBagDtos gbss = RemoteService.instance.askGuildBagList(user.uid, gb.gid)
        if (gbss != null) {
            r.gbs = gbss.guildBags
        } else {
            r.result = "获取公会红包信息失败"
        }

        user.send(r)
    }
}

