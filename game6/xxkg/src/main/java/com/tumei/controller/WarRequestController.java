package com.tumei.controller;

import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.RandomUtil;
import com.tumei.model.PackBean;
import com.tumei.model.WarBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.war.WarTask;
import com.tumei.modelconf.MissionConf;
import com.tumei.modelconf.VipConf;
import com.tumei.websocket.BaseProtocol;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zw on 2018/09/21
 */
@RestController
@RequestMapping("/war")
public class WarRequestController {

    class Return1 {
        public List<WarTask> tasks = new ArrayList<>();
        public String result = "";
    }

    @ApiOperation(value = "战争学院入口")
    @RequestMapping(value = "/requestWarTasks", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "玩家的等级", required = true, dataType = "int", paramType = "query")})
    public Return1 requestWarTasks(long uid, int level) {
        Return1 rci = new Return1();
        WarBean wb = DaoService.getInstance().findWar(uid);
        wb.flush(level);
        rci.tasks.addAll(wb.getTasks().values());
        return rci;
    }

    class Return2 {
        public String result = "";
        // 如果成功开始一个任务，则返回这个任务完成的时间，不用查表了
        public long complete;
    }

    @ApiOperation(value = "开始一个任务")
    @RequestMapping(value = "/requestWarStart", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "玩家的等级", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "tid", value = "任务的递增id", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "vip", value = "玩家VIP等级", required = true, dataType = "int", paramType = "query")})
    public Return2 requestWarStart(long uid, int level, int tid, int vip) {
        Return2 rci = new Return2();

        WarBean wb = DaoService.getInstance().findWar(uid);
        wb.flush(level);
        WarTask wt = wb.getTasks().getOrDefault(tid, null);
        if (wt == null) {
            rci.result = "任务不存在";
            return rci;
        }
        if (wt.complete > 0) {
            rci.result = "任务已经开始";
            return rci;
        }
        long now = System.currentTimeMillis() / 1000;
        if (wt.expire > 0 && now >= wt.expire) {
            wb.getTasks().remove(tid);

            rci.result = "任务已经超时";
            return rci;
        }
        VipConf vc = Readonly.getInstance().findVip(vip);
        if (wb.runningTask() >= vc.wardev) {
            rci.result = "同时进行的任务数量到达上限";
            return rci;
        }
        MissionConf mc = Readonly.getInstance().findMission(wt.task);
        PackBean pb = DaoService.getInstance().findPack(uid);
        int spirit = pb.flushSpirit(0);
        if (spirit >= mc.cost) {
            pb.flushSpirit(-mc.cost);
            wt.complete = now + mc.timeneed;
            rci.complete = wt.complete;
        } else {
            rci.result = "活力不足";
        }
        return rci;
    }

    class Return3 {
       public int[] awards;
        // 如果完成一个常驻任务是会自动再刷新一条的
        public WarTask task;
        public String result = "";
    }

    @ApiOperation(value = "完成一个任务")
    @RequestMapping(value = "/requestWarComplete", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "tid", value = "任务的递增id", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "玩家的等级", required = true, dataType = "int", paramType = "query")})
    public Return3 requestWarComplete(long uid, int level, int tid) {
        Return3 rci = new Return3();

        WarBean wb = DaoService.getInstance().findWar(uid);
        wb.flush(level);

        WarTask wt = wb.getTasks().getOrDefault(tid, null);
        if (wt == null) {
            rci.result = "任务不存在";
            return rci;
        }
        long now = System.currentTimeMillis() / 1000;
        if (wt.complete == 0 || wt.complete > now) {
            rci.result = "任务未开始或者没有到完成时间";
            return rci;
        }
        wb.getTasks().remove(tid);
        MissionConf mc = Readonly.getInstance().findMission(wt.task);
        if (mc.reward1.length > 0) {
            rci.awards = mc.reward1;
        }
        if (mc.reward2.length > 0) {
            rci.awards = mc.reward2;
        }
        if (mc.reward3.length > 0) {
            rci.awards = mc.reward3;
        }

        if (mc.reward4.length > 0) {
            int[] a4 = mc.reward4;
            if (RandomUtil.getBetween(1, 100) < a4[0]) {
                int c = RandomUtil.getBetween(a4[2], a4[3]);
                rci.awards = new int[2];
                rci.awards[0] = a4[1];
                rci.awards[1] = c;
            }
        }

        if (wt.mode <= 2) {
            // 完成常驻任务，会自动刷新一条
            rci.task = wb.flushConstTasks(level, false);
        }
        return rci;
    }

    class Return4 {
        public String result = "";
        /**
         *
         * 如果取消的任务是一个限时任务，但是取消后，当前时间已经大于expire了，则这个任务直接被删除，实际客户端是可以知道这个逻辑的，
         * 服务器为了简化客户端，返回del如果大于0，表示该任务的取消即可删除。
         *
         */
        public int del;
    }

    @ApiOperation(value = "取消一个任务")
    @RequestMapping(value = "/requestWarCancel", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "tid", value = "任务的递增id", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "玩家的等级", required = true, dataType = "int", paramType = "query")})
    public Return4 requestWarCancel(long uid, int level, int tid) {
        Return4 rci = new Return4();

        WarBean wb = DaoService.getInstance().findWar(uid);
        wb.flush(level);

        WarTask wt = wb.getTasks().getOrDefault(tid, null);
        if (wt == null) {
            rci.result = "任务不存在";
            return rci;
        }

        if (wt.complete <= 0) {
            rci.result = "任务还没有开始";
            return rci;
        }

        wt.complete = 0;
        long now = System.currentTimeMillis() / 1000;
        if (wt.expire > 0 && now >= wt.expire) {
            wb.getTasks().remove(tid);
            rci.del = 1;
        }
        return rci;
    }
}
