package com.tumei.groovy.commands.protos

import com.google.common.base.Strings
import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.RemoteService
import com.tumei.common.utils.Defs
import com.tumei.common.webio.BattleResultStruct
import com.tumei.dto.battle.HerosStruct
import com.tumei.game.GameUser
import com.tumei.model.BossBean
import com.tumei.model.HerosBean
import com.tumei.model.beans.AwardBean
import com.tumei.modelconf.BossConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 进入Boss战界面
 */
class RequestBossFight extends BaseProtocol {
    int seq

    class Return extends BaseProtocol {
        int seq
        String result = ""
        // 战斗数据
        String data = ""
        // 本次伤害
        long harm
        // 是否击杀 0标识没有击杀, 一定用其他标识击杀 !=0 标识击杀 不要用 == 1
        int kill

        // 攻击后都有奖励
        List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        BossBean boss = DaoGame.instance.findBoss(user.uid)
        boss.refresh()
        if (boss.count <= 0) {
            rci.result = "今日挑战次数已经用完"
            user.send(rci)
            return
        }

        long now = System.currentTimeMillis()/1000
        if (now <= boss.getNext()) {
            rci.result = "挑战冷却中"
            user.send(rci)
            return
        }

        HerosBean hsb = user.getDao().findHeros(user.getUid())

        HerosStruct hss = hsb.createHerosStruct()

        BossConf bc = Readonly.instance.getBossConf(boss.level)

        int bmax = boss.courageIdx
        if (bmax > 5) {
            bmax = 5
        }

        for (int i = 0; i < bmax; ++i) {
            int idx = boss.courage[i]
            int key = bc.upatt[idx*2]
            int val = bc.upatt[idx*2+1]
            hss.buffs.merge(key, val, {s -> s + val})
        }

        BattleResultStruct rtn = RemoteService.instance.askBossFight(hss)
        if (rtn == null) {
            rci.result = "首领战维护中，请稍后再战."
        } else {
            if (!Strings.isNullOrEmpty(rtn.result)) {
                rci.result = rtn.result
            } else {
                rci.harm = rtn.harm
                rci.kill = rtn.kill

                --boss.count
                boss.clearCourage()
                boss.setNext((long)(System.currentTimeMillis()/1000) + 110)

                // 2. 发送攻击奖励
                rci.awards.addAll(user.addItem(Defs.金币, bc.reward, false, "boss攻击"))

                // 3. 判断并发送击杀奖励
                if (rtn.kill > 0) {
                    rci.awards.addAll(user.addItems(bc.reward3, false, "boss击杀"))
                }
            }
        }

        user.send(rci)
    }
}
