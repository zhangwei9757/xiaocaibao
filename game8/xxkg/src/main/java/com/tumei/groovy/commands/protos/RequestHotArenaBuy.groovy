package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.game.GameUser
import com.tumei.game.protos.structs.StoreStruct
import com.tumei.model.ActivityBean
import com.tumei.model.PackBean
import com.tumei.model.beans.AwardBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 对应宝物碎片，获取抢劫的玩家和npc信息
 */
class RequestHotArenaBuy extends BaseProtocol {
    public int seq

    // [0, ...]  第几个
    public int index

    // 一次买几份
    public int count

    class Return extends BaseProtocol {
        public int seq
        public String result = ""
        /**
         * 购买的物品
         */
        public List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        ActivityBean ab = user.getDao().findActivity(user.getUid())
        ab.flushHotArenas()
        List<StoreStruct> sss = ab.getHotItems()
        if (index < 0 || index >= sss.size() || count <= 0) {
            rci.result = "错误的商品参数index(${index})"
            user.send(rci)
            return
        }

        StoreStruct ss = sss.get(index)
        if ((ss.used + count) > ss.limit) {
            rci.result = "超出今日的购买上限(${ss.limit})"
            user.send(rci)
            return
        }

        PackBean pb = DaoGame.getInstance().findPack(user.getUid())
        if (!pb.contains(ss.price, count)) {
            rci.result = "没有足够的勇士币"
            user.send(rci)
            return
        }

        user.payItem(ss.price, count, "跨服竞技场商店")

        rci.awards.addAll(user.addItem(ss.id, ss.count * count, true, "跨服竞技场商店"))
        ss.used += count

        user.send(rci)
    }
}

