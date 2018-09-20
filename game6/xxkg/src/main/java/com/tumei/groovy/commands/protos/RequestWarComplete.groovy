package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameUser
import com.tumei.model.WarBean
import com.tumei.model.beans.AwardBean
import com.tumei.model.beans.war.WarTask
import com.tumei.modelconf.MissionConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser


/**
 * Created by zw on 2018/09/17
 *
 *
 * 前端获取的所有任务中都有tid字段，用于定位一个任务，这里可以使用tid请求完成一个任务，
 * 待完成的任务必须是前端已经确认complete字段不为0，并且当前时间大于complete表示的时间,
 * 完成的任务如果返回的task不为空，证明本次完成的任务是常驻任务，必须保证有4个，所以会增加一条任务，
 * 界面删除原来tid的任务后，应该增加服务器返回的新的task
 *
 */
class RequestWarComplete extends BaseProtocol {
    public int seq
    // 任务的递增id
    public int tid

    class Return extends BaseProtocol {
        public int seq

        public List<AwardBean> awards = new ArrayList<>()

        // 如果完成一个常驻任务是会自动再刷新一条的
        public WarTask task

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        WarBean wb = DaoService.instance.findWar(user.uid)
        wb.flush(user.level)

        WarTask wt = wb.tasks.getOrDefault(tid, null)
        if (wt == null) {
            rci.result = "任务不存在"
            user.send(rci)
            return
        }

        long now = System.currentTimeMillis() / 1000
        if (wt.complete == 0 || wt.complete > now) {
            rci.result = "任务未开始或者没有到完成时间"
            user.send(rci)
            return
        }

        wb.tasks.remove(tid)

        MissionConf mc = Readonly.instance.findMission(wt.task)
        if (mc.reward1.length > 0) {
            rci.awards.addAll(user.addRangeItems(mc.reward1, false, "战争学院奖励1"))
        }
        if (mc.reward2.length > 0) {
            rci.awards.addAll(user.addRangeItems(mc.reward2, false, "战争学院奖励2"))
        }
        if (mc.reward3.length > 0) {
            rci.awards.addAll(user.addRangeItems(mc.reward3, false, "战争学院奖励3"))
        }
        if (mc.reward4.length > 0) {
            int[] a4 = mc.reward4;
            if (RandomUtil.getBetween(1, 100) < a4[0]) {
                int c = RandomUtil.getBetween(a4[2], a4[3]);
                rci.awards.addAll(user.addItem(a4[1], c, false, "战争学院奖励4"))
            }
        }

        if (wt.mode <= 2) {
            // 完成常驻任务，会自动刷新一条
            rci.task = wb.flushConstTasks(user.level, false)
        }

        user.send(rci)
    }
}

