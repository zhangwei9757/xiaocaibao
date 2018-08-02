package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.model.festival.*
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 获取节日活动信息
 */
class RequestFestivalInfo extends BaseProtocol {
    int seq

    class Return extends BaseProtocol {
        int seq

        // 0:标识节日活动  1:标识神器活动
        int flag

        // 活动的两种货币,以及对应的个数
        int[] coins = new int[4]

        String name = ""
        String desc = ""

        // 节日开始日期 形式(20141202)，当天0点开始
        int start
        // 节日结束日期，当天24点结束
        int end

        // 本周期的总消耗
        int spend

        // 本周期的总充值
        int cum

        // 今日是否已经领取
        int islog

        // 登陆
        List<FestivalLogon> festLogons = new ArrayList<>()

        // 单充状态
        List<FestivalSingle> festSingles = new ArrayList<>()

        // 消费状态
        List<FestivalSpend> festSpends = new ArrayList<>()

        // 兑换状态
        List<FestivalSale> festSales = new ArrayList<>()

        // 累计充值状态
        List<FestivalCum> festCums = new ArrayList<>()

        String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        FestivalBean fb = DaoGame.getInstance().findFestival(user.uid)
        fb.flush()

        if (fb.mode < 0) {
            rci.result = "节日活动已经结束,请下次再来"
        } else {
            rci.start = fb.start
            rci.end = fb.end
            def fc = Readonly.getInstance().findFestivalConf(fb.mode)
            if (fc != null) {
                rci.name = fc.name
                rci.desc = fc.des
            }
            rci.flag = fb.flag

            if (fb.flag == 0) {
                PackBean pb = DaoGame.getInstance().findPack(user.uid)
                rci.coins[0] = fb.coins[0]
                rci.coins[1] = pb.getItemCount(fb.coins[0])
                rci.coins[2] = fb.coins[1]
                rci.coins[3] = pb.getItemCount(fb.coins[1])

                rci.spend = fb.spend
                rci.islog = fb.islog
                rci.festLogons = fb.festLogons
                rci.festSales = fb.festSales
                rci.festSingles = fb.festSingles
                rci.festSpends = fb.festSpends
            } else {
                rci.cum = fb.cum
                rci.festCums = fb.festCums
            }
        }

        user.send(rci)
    }
}
