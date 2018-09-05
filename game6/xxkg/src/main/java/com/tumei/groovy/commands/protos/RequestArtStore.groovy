package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.game.protos.structs.StoreStruct
import com.tumei.model.PackBean
import com.tumei.model.StoreBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 刷新神器商店
 */
class RequestArtStore extends BaseProtocol {
    public int seq

    /**
     * 0: 正常请求商店
     * 1: 强制刷新商店
     */
    public int mode

    class Return extends BaseProtocol {
        public int seq

        // 今日已经强制刷新的次数
        public int count

        public int gem

        public List<StoreStruct> store;

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        if (user.level < Defs.神器等级) {
            rci.result = "领主等级不足"
            user.send(rci)
            return
        }

        StoreBean sb = DaoService.getInstance().findStore(user.uid)
        rci.count = sb.getArtCount()
        if (mode == 1) {
            if (rci.count > 0) {
                // 12， 神器商店刷新变为500钻
                rci.gem = rci.count * Defs.神器商店刷新
                PackBean pb = DaoService.getInstance().findPack(user.uid)
                if (!pb.contains(Defs.钻石, rci.gem)) {
                    rci.result = ErrCode.钻石不足.name()
                    user.send(rci)
                    return
                }
                user.payItem(Defs.钻石, rci.gem, "刷新神器商店")
            }
            rci.store = sb.flushArtStore(true)
            ++(rci.count)
        } else {
            rci.store = sb.flushArtStore(false)
        }

        user.send(rci)
    }
}

