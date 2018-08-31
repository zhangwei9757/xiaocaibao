package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.game.protos.structs.StoreStruct
import com.tumei.model.PackBean
import com.tumei.model.StoreBean
import com.tumei.model.beans.AwardBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 购买神器商店
 */
class RequestArtStoreBuy extends BaseProtocol {
    public int seq

    // 位置[0,7]
    public int index

    class Return extends BaseProtocol {
        public int seq

        public List<AwardBean> awards = new ArrayList<>()

        public int[] price

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq
        StoreBean sb = DaoService.getInstance().findStore(user.uid)
        List<StoreStruct> sss = sb.flushArtStore(false)
        StoreStruct ss = sss.get(index)
        if (ss.used != 0) {
            rci.result = "该商品已被购买"
            user.send(rci)
            return
        }

        PackBean pb = DaoService.getInstance().findPack(user.uid)
        if (!pb.contains(ss.price, 1)) {
            rci.result = "货币不足"
            user.send(rci)
            return
        }
        user.payItem(ss.price, 1, "神器商店购买")
        rci.awards.addAll(user.addItem(ss.id, ss.count, false, "神器商店购买"))
        ss.used = 1

        rci.price = ss.price

        user.send(rci)
    }
}

