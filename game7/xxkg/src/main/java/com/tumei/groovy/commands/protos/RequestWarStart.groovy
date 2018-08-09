package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.model.WarBean
import com.tumei.model.beans.war.WarTask
import com.tumei.modelconf.MissionConf
import com.tumei.modelconf.VipConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 *
 *
 * 前端获取的所有任务中都有tid字段，用于定位一个任务，这里可以使用tid请求完成一个任务，
 * 待完成的任务必须是前端已经确认complete字段不为0，并且当前时间大于complete表示的时间,
 * 完成的任务如果返回的task不为空，证明本次完成的任务是常驻任务，必须保证有4个，所以会增加一条任务，
 * 界面删除原来tid的任务后，应该增加服务器返回的新的task
 *
 *
 * 本地在开始一个任务的时候要根据是否限时任务，判断一下，自己删除掉
 *
 */
class RequestWarStart extends BaseProtocol {
    public int seq
    // 任务的递增id
    public int tid

    class Return extends BaseProtocol {
        public int seq

        public String result = ""
        // 如果成功开始一个任务，则返回这个任务完成的时间，不用查表了
        public long complete
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        WarBean wb = DaoGame.instance.findWar(user.uid)
        wb.flush(user.level)

        WarTask wt = wb.tasks.getOrDefault(tid, null)
        if (wt == null) {
            rci.result = "任务不存在"
            user.send(rci)
            return
        }

        if (wt.complete > 0) {
            rci.result = "任务已经开始"
            user.send(rci)
            return
        }

        long now = System.currentTimeMillis() / 1000
        if (wt.expire > 0 && now >= wt.expire) {
            wb.tasks.remove(tid)

            rci.result = "任务已经超时"
            user.send(rci)
            return
        }

        VipConf vc = Readonly.instance.findVip(user.vip)

        if (wb.runningTask() >= vc.wardev) {
            rci.result = "同时进行的任务数量到达上限"
            user.send(rci)
            return
        }

        MissionConf mc = Readonly.instance.findMission(wt.task)

        PackBean pb = DaoGame.instance.findPack(user.uid)
        int spirit = pb.flushSpirit(0)
        if (spirit >= mc.cost) {
            user.payItem(Defs.活力, mc.cost, "开启战争学院任务")
            wt.complete = now + mc.timeneed
            rci.complete = wt.complete
        } else {
            rci.result = "活力不足"
        }

        user.send(rci)
    }
}

