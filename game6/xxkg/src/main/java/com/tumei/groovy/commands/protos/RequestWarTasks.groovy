package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.game.GameUser
import com.tumei.model.WarBean
import com.tumei.model.beans.war.WarTask
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by zw on 2018/09/17
 *
 * 请求战争学院的所有任务，前端作为进入界面的初始化 协议
 *
 */
class RequestWarTasks extends BaseProtocol {
    public int seq

    class Return extends BaseProtocol {
        public int seq

        public List<WarTask> tasks = new ArrayList<>()

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        WarBean wb = DaoService.instance.findWar(user.uid)
        wb.flush(user.level)
        rci.tasks.addAll(wb.tasks.values())
        user.send(rci)
    }
}

