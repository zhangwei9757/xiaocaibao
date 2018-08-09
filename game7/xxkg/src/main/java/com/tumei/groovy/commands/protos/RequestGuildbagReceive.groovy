package com.tumei.groovy.commands.protos

import com.google.common.base.Strings
import com.tumei.common.RemoteService
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
        int[] result = RemoteService.instance.askGuildBagReceive(gb.gid, user.uid, user.name, bagId)

        if (result != null) {
            // 获取红包的奖励
            user.addItem(result[0], result[1], true, "领取公会红包")
        } else {
            r.result = "领取红包失败"
        }

        user.send(r)
    }
}

