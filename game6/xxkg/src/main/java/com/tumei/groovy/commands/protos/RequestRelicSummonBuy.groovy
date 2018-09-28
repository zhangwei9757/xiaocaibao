package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/26
 * 圣物之魂购买
 */
class RequestRelicSummonBuy extends BaseProtocol {
    public int seq

    // 10个圣物魂为一组，这里的数量是多少组, 个数是乘以10, 每组28钻
    public int count

    class Return extends BaseProtocol {
        public int seq

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        PackBean pb = DaoService.instance.findPack(user.uid)

        int gem = count * 28

        // 不论是什么模式，自动识别翻倍
        gem *= Defs.圣物之魂钻石倍数;

        if (gem <= 0) {
            rci.result = ErrCode.未知参数
        } else {

            if (!pb.contains(Defs.钻石, gem)) {
                rci.result = ErrCode.钻石不足
            } else {
                user.payItem(Defs.钻石, gem, "买圣物魂")
                user.addItem(Defs.圣物之魂, count * 10, false, "买")
            }
        }

        user.send(rci)
    }
}

