package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.beans.ArtifactBean
import com.tumei.model.beans.ArtifactComBean
import com.tumei.modelconf.ArtadvancedConf
import com.tumei.modelconf.ArtifactConf
import com.tumei.modelconf.ArtpartConf
import com.tumei.modelconf.ArtpartstrConf
import com.tumei.modelconf.ArtpartstupConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 神器 进阶
 */
class RequestArtUp extends BaseProtocol {
    public int seq

    // 神器id
    public int artid

    class Return extends BaseProtocol {
        public int seq

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        HerosBean hsb = DaoService.getInstance().findHeros(user.uid)

        ArtifactBean ab = hsb.getArtifacts().getOrDefault(artid, null)
        if (ab == null || ab.level < 1) {
            rci.result = "神器未激活"
            user.send(rci)
            return
        }

        List<ArtadvancedConf> adcs = Readonly.instance.getArtadvancedConfs()
        ArtadvancedConf adc = adcs.get(ab.level - 1)
        if (adc == null || adc.cost1a == -1) {
            rci.result = "神器已达最高级"
            user.send(rci)
            return
        }
        // 部件中最低强化等级
        int minc = 9999
        for (def c : ab.coms.values()) {
            if (c.level < minc) {
                minc = c.level
            }
        }
        if (ab.level >= minc) {
            rci.result = "神器进阶等级不能大于组件中最小的强化等级"
            user.send(rci)
            return
        }

        ArtifactConf art = Readonly.getInstance().findArtifact(artid)

        int cost1 = adc.cost5a
        int cost2 = adc.cost5b
        int cost3 = adc.cost5c

        switch (art.quality) {
            case 1:
                cost1 = adc.cost1a
                cost2 = adc.cost1b
                cost3 = adc.cost1c
                break;
            case 2:
                cost1 = adc.cost2a
                cost2 = adc.cost2b
                cost3 = adc.cost2c
                break;
            case 3:
                cost1 = adc.cost3a
                cost2 = adc.cost3b
                cost3 = adc.cost3c
                break;
            case 4:
                cost1 = adc.cost4a
                cost2 = adc.cost4b
                cost3 = adc.cost4c
                break;
            case 5:
                cost1 = adc.cost5a
                cost2 = adc.cost5b
                cost3 = adc.cost5c
                break;
        }

        PackBean pb = DaoService.instance.findPack(user.uid)
        if (!pb.contains(Defs.铸造石, cost1) ||
            !pb.contains(Defs.符文水晶, cost2) ||
            !pb.contains(Defs.金币, cost3)) {
            rci.result = "材料不足"
            user.send(rci)
            return
        }

        if (cost1 > 0) {
            pb.payItem(Defs.铸造石, cost1, "神器进阶")
        }

        if (cost2 > 0) {
            pb.payItem(Defs.符文水晶, cost2, "神器进阶")
        }

        if (cost3 > 0) {
            pb.payItem(Defs.金币, cost3, "神器进阶")
        }

        ++(ab.level)

        user.send(rci)
    }
}

