package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.model.beans.AwardBean
import com.tumei.model.festival.FestivalBean
import com.tumei.model.festival.FestivalSale
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 获取 节日活动的中的单充奖励
 */
class RequestFestivalBuy extends BaseProtocol {
    public int seq
    // [0，n] 顺序发送过来即可
    public int index;

    // 一次性兑换多个
    public int count;

    class Return extends BaseProtocol {
        public int seq

        public List<AwardBean> awards = new ArrayList<>();

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        FestivalBean fb = DaoService.getInstance().findFestival(user.uid)
        fb.flush()

        if (fb.mode < 0 || fb.getFlag() != 0) {
            rci.result = "节日活动已经结束,请下次再来"
        } else {
            if (count <= 0 || index < 0 || index >= fb.festSales.size()) {
                rci.result = "参数错误"
            } else {
                FestivalSale fs = fb.festSales.get(index);
                if ((fs.used + count) > fs.limit) {
                    rci.result = "节日兑换次数达到上限"
                } else {
                    PackBean pb = DaoService.getInstance().findPack(user.uid)
                    if (!pb.contains(fs.price, count)) {
                        rci.result = "兑换的货币不足"
                    } else {
                        fs.used += count
                        user.payItem(fs.price, count, "节日兑换(${fs.goods}")
                        rci.awards.addAll(user.addItems(fs.goods, count, false, "节日兑换"))
                    }
                }
            }
        }

        user.send(rci)
    }
}
