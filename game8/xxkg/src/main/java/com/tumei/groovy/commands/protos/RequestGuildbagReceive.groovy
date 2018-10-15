package com.tumei.groovy.commands.protos

import com.google.common.base.Strings
import com.google.common.eventbus.DeadEvent
import com.tumei.common.RemoteService
import com.tumei.dto.guild.GuildbagDetailDto
import com.tumei.dto.guild.GuildbagRoleDto
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
class RequestGuildbagReceive extends BaseProtocol {
    public int seq
    /**
     * 红包的id
     * **/
    public String bagId

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result = ""
        // 红包列表
        public HashMap<Long,GuildbagRoleDto> ids
        // 领取的数量
        public int count
        // 红包还可领取的人数
        public int remaining
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        try {
            String[] strs = bagId.split('-')
            int mode = Integer.parseInt(strs[1])
            if (mode < 1 || mode > 3) {
                r.result = "参数不合法"
            }
        } catch (Exception ex) {
            r.result = "参数不合法"
        }

        if (!Strings.isNullOrEmpty(r.result)) {
            user.send(r)
            return
        }

        // 远程请求领取红包
        GroupBean gb = user.getDao().findGroup(user.getUid())
        GuildbagDetailDto detail = RemoteService.instance.askGuildBagReceive(gb.gid, user.uid, user.name, bagId)

        if (detail != null) {
            // 获取红包的奖励
            if (detail.count > 0) {
                user.addItem(detail.id, detail.count, false, "领取公会红包")
            }
            r.ids = detail.ids
            r.count = detail.count
            r.remaining = detail.remaining
        }

        user.send(r)
    }
}

