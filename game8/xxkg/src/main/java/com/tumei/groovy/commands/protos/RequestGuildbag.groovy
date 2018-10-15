package com.tumei.groovy.commands.protos

import com.tumei.common.RemoteService
import com.tumei.dto.guild.GuildbagBasicDto
import com.tumei.game.GameUser
import com.tumei.model.GroupBean
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
        public List<GuildbagBasicDto> gbs = new  ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        // 远程拉取玩家所在公会的红包
        GroupBean gb = user.getDao().findGroup(user.getUid())
        if (gb != null) {
            r.gbs = RemoteService.instance.askGuildBagList(gb.gid,user.uid)
        } else {
            r.result = "请先加入一个公会"
        }
        user.send(r)
    }
}

