package com.tumei.groovy.commands.protos

import com.google.common.base.Strings
import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameUser
import com.tumei.game.services.LimitRankService
import com.tumei.model.ActivityBean
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.beans.RelicBean
import com.tumei.model.limit.LimitRankBean
import com.tumei.modelconf.HolyConf
import com.tumei.modelconf.VipConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 圣物炼化
 */
class RequestRelicActivate extends BaseProtocol {
    public int seq

    // 圣物id
    public int relic

    /**
     * 0: 普通炼化     血晶石进行炼化 不限制次数 每次消耗1
     * 1: 钻石炼化      第一次20钻 每次递增10 200钻封顶 每日可以炼化的次数由vip决定
     */
    public int mode

    class Return extends BaseProtocol {
        public int seq

        /**
         * 0: 没有激活传奇英雄
         * 其他: 激活了传奇英雄
         */
        public int hero

        /**
         * 0: 表示圣物的经验增加10点
         * [1-12]: 表示附加的属性哪个提升了一点
         */
        public int up

        public String result = ""
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq
        HerosBean hsb = DaoGame.getInstance().findHeros(user.uid)
        RelicBean rb = hsb.getRelics().getOrDefault(relic, null)
        if (rb == null) {
            rci.result = ErrCode.对应的圣物不存在
        } else {
            HolyConf hc = Readonly.instance.findHoly(relic)
            PackBean pb = DaoGame.instance.findPack(user.uid)

            if (!pb.contains(Defs.金币, hc.coincost)) {
                rci.result = ErrCode.金币不足
                user.send(rci)
                return
            }

            if (mode == 0) { // 普通炼化
                if (!pb.contains(Defs.圣灵石, 1)) {
                    rci.result = ErrCode.圣灵石不足
                } else {
                    user.payItem(Defs.圣灵石, 1, "圣物炼化")
                }
            } else { // 钻石炼化
                ActivityBean ab = DaoGame.instance.findActivity(user.uid)
                ab.flush()

                // 获取炼化总次数
                VipConf vc = Readonly.instance.findVip(user.vip)

                if (ab.relicActivate >= vc.dailyglory) {
                    rci.result = ErrCode.今日钻石注灵次数已达上限
                } else {

                    int need = ab.relicActivate * 10 + 20
                    if (need > 200) {
                        need = 200
                    }

                    if (!pb.contains(Defs.钻石, need)) {
                        rci.result = ErrCode.钻石不足
                    } else {
                        user.payItem(Defs.钻石, need, "钻石炼化")
                        ++ab.relicActivate
                        // 钻石注灵，记录一次次数
                        LimitRankService.instance.put(user.uid,user.name,1,2)
                    }
                }
            }


            if (Strings.isNullOrEmpty(rci.result)) {
                user.payItem(Defs.金币, hc.coincost, "圣物炼化")
                ++rb.count
                // 没有错误可以直接判断炼化几率
                int[] rt = hc.aim[mode]

                int limit = hc.addlimit
                if (rb.hlvl >= 5) {
                    limit *= 2
                }

                // 第一组是普通炼化,第二组是钻石炼化
                // 每组第一个是升级概率,第二个是提升属性概率
                boolean flag = false // 判断增加经验是否生效,如果满级了,增加经验没有必要了,直接增加属性

                if (RandomUtil.getBetween(1, 100) <= rt[0]) {
                    if (hsb.addRelicExp(rb, 10)) {
                        flag = true
                    }
                }

                // 没有增加经验,就可以增加属性,属性有4组12个
                if (!flag) {
                    // 随机一个属性进行增益,如果该属性到顶了,则顺序查找下一个
                    int idx = RandomUtil.getBetween(0, 11)
                    for (int j = idx; j < 12; ++j) {
                        if (rb.attrs[j] < limit) {
                            ++rb.attrs[j]
                            rci.up = (j + 1)
                            flag = true
                            break
                        }
                    }
                    if (!flag) {
                        for (int j = 0; j < idx; ++j) {
                            if (rb.attrs[j] < limit) {
                                ++rb.attrs[j]
                                rci.up = (j + 1)
                                flag = true
                                break
                            }
                        }
                    }

                    // 如果传奇英雄还未召唤出来,则判断传奇英雄召唤概率
                    if (rb.hero == 0) {
                        if (!flag) { // 本次属性已经全满了，没有找到升级属性的激活，传奇英雄应该直接激活
                            rb.hero = hc.hero
                            rci.hero = hc.hero
                        } else {
                            int max = hc.rate.size() / 2
                            for (int i = max - 1; i >= 0; --i) {
                                int r = hc.rate[i * 2] // 次数
                                if (rb.count < r) {
                                    continue
                                }
                                int v = hc.rate[i * 2 + 1] // 对应的概率
                                // 满足概率
                                if (RandomUtil.getBetween(1, 1000) <= v) {
                                    // 命中
                                    rb.hero = hc.hero
                                    rci.hero = hc.hero
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }

        user.send(rci)
    }
}

