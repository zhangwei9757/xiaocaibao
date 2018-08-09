package com.tumei.groovy.commands.protos

import com.tumei.game.GameUser
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Leon on 2017/11/3 0003.
 */
class RequestOpenMatch extends BaseProtocol {
    public int seq


    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser)session

        Return rl = new Return()
        rl.seq = seq

        user.send(rl)
    }
}

class Return extends BaseProtocol {
    public int seq
    public String result = ""
}
