package com.tumei.controller;

import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.webio.AwardStruct;
import com.tumei.dto.db2proto.NameValue;
import com.tumei.dto.limit.InvadingAwardDto;
import com.tumei.dto.limit.InvadingLoginDto;
import com.tumei.game.services.InvadingRankService;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.limit.InvadingBean;
import com.tumei.modelconf.limit.InvrankConf;
import com.tumei.modelconf.limit.InvtotalConf;
import com.tumei.websocket.BaseProtocol;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zw on 2018/09/28
 */
@RestController
@RequestMapping("/invading")
public class InvadingRequestController {
    static final Log log = LogFactory.getLog(InvadingRequestController.class);

    @Autowired
    private InvadingRankService instance;

    @ApiOperation(value = "购买碎片")
    @RequestMapping(value = "/requestBuyDebris", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "购买的碎片数量", required = true, dataType = "int", paramType = "query")})
    public int requestBuyDebris(long uid, int count) {
        if (!instance.isActive()) {
            return -999;
        } else {
            // 判断参数是否合法
            if (count < 0) {
                return -1;
            } else {
                InvadingBean ib = DaoGame.getInstance().findInvading(uid);
                if (ib != null) {
                    return ib.buyDebris(uid, count);
                } else {
                    return -999;
                }
            }
        }
    }

    @ApiOperation(value = "购买怪兽复活")
    @RequestMapping(value = "/requestBuyResurgence", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
    })
    public int requestBuyResurgence(long uid) {
        if (!instance.isActive()) {
            return -999;
        } else {
            InvadingBean ib = DaoGame.getInstance().findInvading(uid);
            if (ib != null) {
                return ib.buyResurgence(uid);
            } else {
                return -999;
            }
        }
    }

    @ApiOperation(value = "领取累计充值奖励")
    @RequestMapping(value = "/requestInvadingChargeAward", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "rmb", value = "玩家充值金额", required = true, dataType = "int", paramType = "query")})
    public int[] requestInvadingChargeAward(long uid, int rmb) {
        if (!instance.isActive()) {
            return new int[]{-999};
        } else {
            // 判断参数是否合法
            if (rmb <= 0) {
                return new int[]{-1};
            } else {
                InvadingBean ib = DaoGame.getInstance().findInvading(uid);
                if (ib != null) {
                    // 获取指定奖励,左id ,右count
                    int[] awds = ib.getChargeAward(rmb);
                    if (awds[0] == -1) {
                        return new int[]{-1};
                    } else if (awds[0] == -2) {
                        return new int[]{-2};
                    } else {
                        return awds;
                    }
                } else {
                    return new int[]{-999};
                }
            }
        }
    }

    @ApiOperation(value = "获取累计充值奖励列表信息")
    @RequestMapping(value = "/requestInvadingChargeAwardInfo", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query")})
    public List<InvadingAwardDto> requestInvadingChargeAwardInfo(long uid) {
        if (!instance.isActive()) {
            return null;
        } else {
            List<InvadingAwardDto> chargesList = new ArrayList<>();
            InvadingBean ib = DaoGame.getInstance().findInvading(uid);
            if (ib != null) {
                List<InvtotalConf> confs = Readonly.getInstance().getInvtotalConfs();
                int index = 0;
                for (InvtotalConf ic : confs) {
                    InvadingAwardDto iad = new InvadingAwardDto();
                    List<AwardStruct> list = new ArrayList<>();
                    int[] reward = ic.reward;
                    for (int i = 0; i < reward.length; ++i) {
                        list.add(new AwardStruct(reward[i], reward[++i]));
                    }
                    iad.awards = list;
                    iad.cost = ic.cost;
                    iad.status = ib.getReceiveStatu(ic.cost * 100);
                    chargesList.add(iad);
                    ++index;
                }
                return chargesList;
            } else {
                return null;
            }
        }
    }

    @ApiOperation(value = "碎片攻击")
    @RequestMapping(value = "/requestInvadingDebrisAttack", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "使用的碎片数量", required = true, dataType = "int", paramType = "query")})
    public int[][] requestInvadingDebrisAttack(long uid, int count) {
        if (!instance.isActive()) {
            return new int[][]{{-999}};
        } else {
            // 判断参数是否合法
            if (count < 0) {
                return new int[][]{{-1}};
            } else {
                InvadingBean ib = DaoGame.getInstance().findInvading(uid);
                if (ib != null) {
                    // 刷新碎片
                    ib.flushDebris();
                    // 刷新复活
                    ib.flushResurgence();
                    // 获取指定奖励,左id ,右count
                    int[] awds = ib.getAttackAward(count);
                    if (awds[0] < 0 || awds[1] < 0) {
                        return new int[][]{{-999}};
                    }
                    int[][] receive = new int[2][2];
                    receive[0] = awds;
                    // 额外获取击杀奖励
                    awds = ib.getKillAward(uid, "测试");
                    if (awds[0] != -1) {
                        receive[1] = awds;
                    }
                    return receive;

                } else {
                    return new int[][]{{-999}};
                }
            }
        }
    }

    class Return {
        public int seq;
        public String result = "";
        // 碎片攻击历史所有奖励
        public List<AwardBean> debrisList = new ArrayList<>();
        // 碎片击杀怪兽所有奖励,下标对应次数的奖励
        public List<List<AwardBean>> killList = new ArrayList<>();
        // 当前血量
        public int blood;
        // 怪兽血量上限
        public int maxBlood;
        // kill次数
        public int kill;
        // 复活最终时间
        public long resurgence;
    }

    @ApiOperation(value = "碎片攻击信息")
    @RequestMapping(value = "/requestInvadingDebrisAttackInfo", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query")})
    public Return requestInvadingDebrisAttackInfo(long uid) {
        Return rci = new Return();
        if (!instance.isActive()) {
            return rci;
        } else {
            InvadingBean invading = DaoGame.getInstance().findInvading(uid);
            if (invading != null) {
                rci.debrisList = invading.getDebrisList();
                rci.killList = invading.getKillList();
                rci.blood = invading.getBlood();
                rci.maxBlood = Defs.怪兽入侵血量上限;
                rci.kill = invading.getKill();
                rci.resurgence = invading.getResurgence();
            } else {
                rci.result = "获取碎片攻击信息错误";
            }
            return rci;
        }
    }

    class Return2 {
        public String result = "";
        public int currentDay;
        // 次元碎片数量
        public int debris;
        // 次元碎片数量上限
        public int maxDebris;
        // 生成碎片最终时间
        public long lastFlushDebris;
        // 登陆奖励列表 第1-N天
        // 登陆状态: -1表示已过期需要补签 0表示未领取 1表示已领取 2表示已购买 3未到领取日期状态、下标对应活动第？天
        public List<InvadingLoginDto> loginList = new ArrayList<>();
        // 购买总次数（新的一天会跟随单价一起重置）
        public int buyTotal;
    }

    @ApiOperation(value = "获取怪兽入侵信息")
    @RequestMapping(value = "/requestInvadingInfo", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query")})
    public Return2 requestInvadingInfo(long uid) {
        Return2 rci = new Return2();
        InvadingRankService lrs = InvadingRankService.getInstance();
        if (!lrs.isActive()) {
            return rci;
        } else {
            InvadingBean ib = DaoGame.getInstance().findInvading(uid);
            ib.flushDebris();
            ib.flushDebris();
            rci.currentDay = ib.getCurrentDay();
            rci.debris = ib.getDebris();
            if (ib.getLastFlushDebris() == 0) {
                rci.lastFlushDebris = 0;
            } else {
                rci.lastFlushDebris = ib.getLastFlushDebris() + Defs.怪兽入侵碎片生成时间;
            }
            rci.loginList = ib.createInvadingLoginDto();
            rci.maxDebris = Defs.怪兽入侵碎片上限;
            rci.buyTotal = ib.getBuyTotal();

        }
        return rci;
    }


    class Return3 extends BaseProtocol {
        public String result = "";
        /**
         * 领取怪兽入侵登陆奖励
         */
        int[] receive = new int[2];
        /**
         * 负数表示是领取
         * 正数表示是购买,返回钻石消费数量
         */
        public int gem = -1;
    }

    @ApiOperation(value = "奖励登陆领取/购买/补签")
    @RequestMapping(value = "/requestInvadingLoginAward", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "position", value = "活动第？天", required = true, dataType = "int", paramType = "query")})
    public Return3 requestInvadingLoginAward(long uid,int position) {
        Return3 rci = new Return3();
        InvadingRankService lrs = InvadingRankService.getInstance();
        if (!lrs.isActive()) {
            return null;
        } else {
            if (!lrs.isActive()) {
                return null;
            } else {
                InvadingBean invading = DaoGame.getInstance().findInvading(uid);
                if (invading != null) {
                    int[][] receive = invading.getLoginddAward(uid, position - 1);
                    if (receive == null) {
                        rci.result = "不符合条件";
                    } else if (receive[0][0] == 0) {
                        rci.result = "操作失败";
                    } else if (receive[0][0] < 0) {
                        rci.result = "钻石不足";
                        rci.gem = receive[1][0];
                    } else {
                        rci.gem = receive[1][0];
                        rci.receive = receive[0];
                    }
                } else {
                    rci.result = "操作失败";
                }
            }
            return rci;
        }
    }

    @ApiOperation(value = "获取活动排行榜信息")
    @RequestMapping(value = "/requestInvadingRanksInfo", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id", required = true, dataType = "long", paramType = "query")
    })
    public List<NameValue> requestInvadingRanksInfo(long uid) {
        InvadingRankService lrs = InvadingRankService.getInstance();
        // 活动击杀排行榜
        List<NameValue> ranks = new ArrayList<>();
        if (!lrs.isActive()) {
            return ranks;
        } else {
            ranks = lrs.getRanks(uid);
            List<InvrankConf> ics = Readonly.getInstance().getInvrankConfs();

            // 排行填充对应排名奖励
            for (int i = 0; i < ics.size(); ++i) {
                List<AwardStruct> ass = new ArrayList<>();
                for (int j = 0; j < ics.get(i).reward.length; ++j) {
                    ass.add(new AwardStruct(ics.get(i).reward[j], ics.get(i).reward[++j]));
                }
                ranks.get(i).setAwards(ass);
            }
        }
        return ranks;
    }

    @ApiOperation(value = "模拟结算")
    @RequestMapping(value = "/force", method = RequestMethod.GET)
    public String force() {
        try {
            InvadingRankService.getInstance().flushLimitTask(true);
            return "SUCCESS";
        } catch (Exception ex) {
            return "FAIL";
        }
    }
}
