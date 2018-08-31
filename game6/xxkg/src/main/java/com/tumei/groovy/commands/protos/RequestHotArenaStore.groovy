package com.tumei.groovy.commands.protos

import com.tumei.game.GameUser
import com.tumei.game.protos.structs.StoreStruct
import com.tumei.model.ActivityBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
class RequestHotArenaStore extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        /**
         * 商店购买
         */
        public List<StoreStruct> items = new ArrayList<>()
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        ActivityBean ab = user.getDao().findActivity(user.getUid())
        ab.flushHotArenas()
        rci.items = ab.getHotItems()

        user.send(rci)
    }
}

