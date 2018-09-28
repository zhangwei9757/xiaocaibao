package com.tumei.controller;

import com.google.common.base.Strings;
import com.tumei.common.DaoService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.model.ActivityBean;
import com.tumei.model.HerosBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.model.beans.RelicBean;
import com.tumei.modelconf.*;
import com.tumei.websocket.BaseProtocol;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zw on 2018/09/26
 */
@RestController
@RequestMapping("/relic")
public class RelicRequestController {
    static final Log log = LogFactory.getLog(RelicRequestController.class);

    class Return extends BaseProtocol {
        /**
         * 0: 没有激活传奇英雄
         * 其他: 激活了传奇英雄
         */
        public int hero;
        /**
         * 0: 表示圣物的经验增加10点
         * [1-12]: 表示附加的属性哪个提升了一点
         */
        public int up;
        public String result = "";
        public int success;
        // 获得的具体物品
        public List<AwardBean> awards = new ArrayList<>();
        public int level;
        public int exp;
    }

    @ApiOperation(value = "圣物碎片召唤")
    @RequestMapping(value = "/requestRelicSummon", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id ", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "mode", value = "0:表示单抽 1:表示十连抽", required = true, dataType = "int", paramType = "query")})
    public Return requestRelicSummon(long uid, int mode) {
        Return rci = new Return();
        PackBean pb = DaoService.getInstance().findPack(uid);

        int gem = 100;

        if (mode == 1) {
            gem = 980;
        }

        if (!pb.contains(Defs.圣物之魂, gem)) {
            rci.result = ErrCode.圣物之魂不足.name();
        } else {
            //user.payItem(Defs.圣物之魂, gem, "抽圣物")
            List<GlorychestConf> gcs = Readonly.getInstance().getGlorychests();

            // 按照概率进行抽
            if (mode == 1) { // 十连
                for (int i = 0; i < 9; ++i) {
                    int[] reward = null;
                    int total = 0;
                    int r = RandomUtil.getBetween(1, 100);
                    for (GlorychestConf gc : gcs) {
                        total += gc.rate2;
                        if (r <= total) {
                            reward = gc.rewards;
                            break;
                        }
                    }

                    if (reward != null) {
                        // 测试获取物品
                        rci.awards.add(new AwardBean(reward[0], reward[1]));
                    }
                }
                // 测试获取物品
                rci.awards.add(new AwardBean(5555, 1));
            } else { // 单抽
                int[] reward = null;
                int total = 0;
                int r = RandomUtil.getBetween(1, 100);
                for (GlorychestConf gc : gcs) {
                    total += gc.rate1;
                    if (r <= total) {
                        reward = gc.rewards;
                        break;
                    }
                }

                if (reward != null) {
                    // 测试获取物品
                    rci.awards.add(new AwardBean(reward[0], reward[1]));
                }
            }
        }
        return rci;
    }

    @ApiOperation(value = "圣物之魂购买")
    @RequestMapping(value = "/requestRelicSummonBuy", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "玩家id ", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "10个圣物魂为一组，count表示？组, 个数是乘以10, 每组28钻 ", required = true, dataType = "int", paramType = "query")})
    public Return requestRelicSummonBuy(long uid, int count) {
        Return rci = new Return();
        PackBean pb = DaoService.getInstance().findPack(uid);

        int gem = count * 28;
        gem *= Defs.圣物之魂钻石倍数;

        if (gem <= 0) {
            rci.result = ErrCode.未知参数.name();
        } else {
            if (!pb.contains(Defs.钻石, gem)) {
                rci.result = ErrCode.钻石不足.name();
            } else {
                //user.payItem(Defs.钻石, gem, "买圣物魂");
                pb.addItem(Defs.圣物之魂, count * 10, "测试购买圣物之魂");
                rci.awards.add(new AwardBean(Defs.圣物之魂, count * 10));
            }
        }
        return rci;
    }
}