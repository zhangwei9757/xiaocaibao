package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.modelconf.ArtifactConf
import com.tumei.modelconf.ArtpartConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 重新绑定账号
 */
class RequestBindAccount extends BaseProtocol {
    public int seq

    public String account;

    public String password;

    class Return extends BaseProtocol {
        public int seq
        public int gem
        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        rci.result = DaoGame.instance.bindAccount(user.getUid(), account, password)

        if (rci.result == null) {
            user.addItem(Defs.钻石, 50, false, "绑定账号")
            rci.gem = 50
        }

        user.send(rci)
    }
}

