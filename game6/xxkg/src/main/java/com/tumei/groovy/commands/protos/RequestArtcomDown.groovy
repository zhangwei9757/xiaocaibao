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
import com.tumei.model.beans.AwardBean
import com.tumei.modelconf.ArtpartConf
import com.tumei.modelconf.ArtpartstrConf
import com.tumei.modelconf.ArtpartstupConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 神器组件 分解
 */
class RequestArtcomDown extends BaseProtocol {
    public int seq

    // 待分解的神器组件id
    public HashMap<Integer, Integer> coms = new HashMap<>();

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        public List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        PackBean pb = DaoService.getInstance().findPack(user.uid)
        int jinghua = 0
        for (def pair in coms) {
            int k = pair.key
            int v = pair.value

            ArtpartConf ac = Readonly.instance.findArtpart(k)
            if (ac != null && pb.hasArtcom(k, v)) {
                pb.payArtcom(k, v, "神器部件分解")
                jinghua += (ac.reget[1] * v)
            }
        }
        if (jinghua > 0) {
            rci.awards.addAll(user.addItem(Defs.神器精华, jinghua, false, "神器部件分解"))
        }

        user.send(rci)
    }
}

