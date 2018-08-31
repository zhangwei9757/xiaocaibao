package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.modelconf.ArtifactConf
import com.tumei.modelconf.ArtpartConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 激活神器组件
 */
class RequestArtEquip extends BaseProtocol {
    public int seq

    // 神器id
    public int artid

    // 神器组件id
    public int comid

    /**
     * 0: 激活 部件
     * 1: 激活 神器
     */
    public int mode;

    class Return extends BaseProtocol {
        public int seq

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

        ArtifactConf ac = Readonly.getInstance().findArtifact(artid)
        if (ac == null) {
            rci.result = "参数错误"
            user.send(rci)
            return
        }

        HerosBean hsb = DaoService.getInstance().findHeros(user.uid)

        if (mode == 1) {
            if (!hsb.promoteArt(artid)) {
                rci.result = "参数错误"
            }
            user.send(rci)
            return
        }

        ArtpartConf apc = Readonly.getInstance().findArtpart(comid)
        if (apc == null) {
            rci.result = "参数错误"
            user.send(rci)
            return
        }


        PackBean pb = DaoService.getInstance().findPack(user.uid)
        if (!pb.hasArtcom(comid, 1)) {
            rci.result = "神器部件不存在"
        } else if (!hsb.equipArt(artid, comid)) {
            rci.result = "神器部件已经激活"
        } else {
            pb.payArtcom(comid, 1, "激活部件")
        }

        user.send(rci)
    }
}

