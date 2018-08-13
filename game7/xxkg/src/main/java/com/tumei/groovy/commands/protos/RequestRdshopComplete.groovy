package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.RdshopBean
import com.tumei.model.beans.rdshop.RdshopStruct
import com.tumei.modelconf.RdshopConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

import java.util.stream.Collectors

/**
 * Created by Administrator on 2018-7-27
 * 神秘商店事件完成
 *
 */
class RequestRdshopComplete extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq
        // 返回结果
        public String result = ""
        // 还可生成的事件个数
        public int count
        // 返回新的事件
        public RdshopStruct rs
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return r = new Return()
        r.seq = seq

        RdshopBean rb = DaoGame.instance.findRdshopBean(user.getUid())
        RdshopConf rsc
        if (rb.rs != null) {
            rsc = Readonly.instance.getRdshop().stream().filter { f -> f.key == rb.rs.key }.collect(Collectors.toList())[0]
        }
        if (rb.rs != null) {
            long current = System.currentTimeMillis() / 1000

            // 事件超时，直接进入下一个事件
            if (current >= rb.rs.complete && rb.rs.complete != 0) {
                rb.rs = null
                r.rs = rb.flush(user)
                r.count = rb.count
                r.result = "当前事件已过期"
                user.send(r)
                return
            }

            // 战力值达标类型  1：战力值 2：商品购买
            if (rsc.type == 1) {
                HerosBean hsb = DaoGame.instance.findHeros(user.uid)
                long newPower = (long) (1.0 + rsc.limit / 10000.0) * user.calcPower(hsb)
                if (rb.rs.power > newPower) {
                    r.result = "战力值未达标"
                    user.send(r)
                    return
                }
                user.addItems(rsc.rewards, false, "完成神秘商店战力值达标事件")
            } else {
                // 购买商品类型  1：战力值 2：商品购买
                PackBean pb = DaoGame.instance.findPack(user.getUid())
                if (!pb.contains(Defs.钻石, rsc.cost)) {
                    r.result = "所需钻石不足"
                    user.send(r)
                    return
                }
                user.payItem(Defs.钻石, rsc.cost, "完成神秘商店购买事件")
                user.addItems(rsc.rewards, true, "完成神秘商店购买事件")
            }
            r.rs = rb.complete(user)
            r.count = rb.count
        } else {
            r.result = "当前事件提交失败"
        }

        user.send(r)
    }
}

