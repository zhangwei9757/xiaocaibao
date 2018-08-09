package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.beans.ArtifactBean
import com.tumei.model.beans.RelicBean
import com.tumei.modelconf.ArtadvancedConf
import com.tumei.modelconf.ArtifactConf
import com.tumei.modelconf.HolyConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 圣物 提升
 */
class RequestRelicUp extends BaseProtocol {
    public int seq

    // 圣物id
    public int relic

    /**
     * 0: 升星        使用对应的圣物碎片,达到一定的数量即可升星
     * 1: 升级        对应不同的星,可以升的最大等级不同,0星不能升级
     */
    public int mode

    /**
     * 升级的时候,提供的法宝碎片与对应的数量
     */
    public HashMap<Integer, Integer> mats = new HashMap<>()

    class Return extends BaseProtocol {
        public int seq

        // 升级返回的最后等级和经验
        public int level
        public int exp

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

            if (mode == 0) { // 升星,判断当前圣物碎片个数是否满足需求
                if (rb.star >= 5) {
                    rci.result = ErrCode.圣物达到最大星级
                } else {
                    HolyConf hc = Readonly.instance.findHoly(relic)
                    int need = hc.starup[rb.star]
                    PackBean pb = DaoGame.instance.findPack(user.uid)
                    if (!pb.contains(relic + 1, need)) {
                        rci.result = ErrCode.圣物升星碎片不足
                    } else {
                        ++rb.star
                        user.payItem(relic + 1, need, "圣物升星" + rb.star)
                    }
                }
            } else { // 升级
                if (rb.star <= 0) {
                    rci.result = ErrCode.圣物达到最大等级
                } else {
                    // 当前等级升级 所需经验
                    int cost = Readonly.instance.findHolyexp(rb.level)
                    if (cost < 0) {
                        rci.result = ErrCode.圣物达到最大等级
                    } else {
                        // 计算当前贡献的圣物碎片提供的总经验
                        int total = 0
                        PackBean pb = DaoGame.instance.findPack(user.uid)

                        // 计算需要的金币数量
                        long gold_need = 0
                        for (Map.Entry<Integer, Integer> entry : mats) {
                            if (!pb.contains(entry.key, entry.value)) {
                                rci.result = ErrCode.圣物碎片不足
                                user.send(rci)
                                return
                            }
                            gold_need += entry.value * 50_000
                        }
                        if (!pb.contains(Defs.金币, gold_need)) {
                            rci.result = ErrCode.金币不足
                            user.send(rci)
                            return
                        }

                        // 检查物品是否存在,并进行扣减, 如果不存在,则跳过
                        for (Map.Entry<Integer, Integer> entry : mats) {
                            if (!pb.contains(entry.key, entry.value)) {
                                continue
                            }
                            user.payItem(entry.key, entry.value, "升级")
                            total += entry.value * 10
                        }

                        while (total > 0) {
                            int need = cost - rb.exp
                            if (total >= need) {
                                total -= need
                                ++rb.level
                                rb.exp = 0
                                cost = Readonly.instance.findHolyexp(rb.level)
                                if (cost < 0) {//证明之后已经无法升级
                                    break
                                }
                            } else {
                                rb.exp += total
                                break
                            }
                        }

                        rci.level = rb.level
                        rci.exp = rb.exp
                    }
                }
            }
        }

        user.send(rci)
    }
}

