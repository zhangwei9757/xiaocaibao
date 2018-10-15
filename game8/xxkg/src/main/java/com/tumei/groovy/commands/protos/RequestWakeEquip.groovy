package com.tumei.groovy.commands.protos

import com.google.common.base.Strings
import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.beans.EquipBean
import com.tumei.model.beans.HeroBean
import com.tumei.model.equips.EquipMat
import com.tumei.modelconf.EquipConf
import com.tumei.modelconf.ItemConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 觉醒装备
 */
class RequestWakeEquip extends BaseProtocol {
    public int seq

    // [0,5] 六个英雄阵型 位置
    public int hero

    // [0,3] 装备的位置
    public int equip

    // 在背包中的装备的唯一编码 notice:不是id
    public List<EquipMat> mats = new ArrayList<>()

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        public int level;
        public int exp;
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        HerosBean hsb = DaoGame.getInstance().findHeros(user.uid)
        HeroBean hb = hsb.getHeros()[hero]
        if (hb == null) {
            rci.result = "该位置的英雄不存在"
            user.send(rci)
            return
        }

        EquipBean eb = hb.equips[equip]
        if (eb == null) {
            rci.result = "该位置的装备不存在"
            user.send(rci)
            return
        }

        EquipConf ec = Readonly.getInstance().findEquip(eb.id)
        ItemConf ic = Readonly.getInstance().findItem(eb.id)
        if (ec == null || ic == null) {
            rci.result = "装备无法查询:" + eb.id
            user.send(rci)
            return
        }

        if (ic.quality < 4) {
            rci.result = "只有金色及以上品质的装备才能觉醒"
            user.send(rci)
            return
        }

        if (eb.wake >= 10) {
            rci.result = "觉醒已经达到顶级"
            user.send(rci)
            return
        }

        int needQuality = ic.quality - 1
        if (eb.wake >= 5) {
            ++needQuality
        }

        int wake = eb.wake
        int wexp = eb.wexp

        PackBean pb = DaoGame.getInstance().findPack(user.uid)
        int need = ec.wakencost[wake] // 需要的经验
        for (EquipMat em : mats) {
            if (em != null) {
                int add = 0
                if (em.count < 0) { // 错误的参数
                    rci.result = "错误的装备个数"
                    break
                } else if (em.count == 0) { // 整个装备
                    EquipBean ee = pb.getEquips().getOrDefault(em.eid, null)
                    if (ee != null) {
                        EquipConf eec = Readonly.getInstance().findEquip(ee.id)
                        if (eec.position != ec.position) {
                            rci.result = "材料装备的部件不符"
                            break
                        }

                        ItemConf eic = Readonly.getInstance().findItem(ee.id)

                        if (eic.quality != needQuality) {
                            rci.result = "物品($ee.id) 对应的品质不正确"
                            break
                        } else {
                            switch (eic.quality) {
                                case 3:
                                    add = 96
                                    break;
                                case 4:
                                    add = 4000
                                    break;
                                case 5:
                                    add = 150000
                                    break;
                            }
                        }
                    } else {
                        rci.result = "物品($em.eid) 不存在"
                        break;
                    }
                } else { // 碎片
                    int has = pb.getItemCount(em.eid)
                    if (has >= em.count) {
                        ItemConf eic = Readonly.getInstance().findItem(em.eid)

                        int eee = em.eid - (em.eid % 10)
                        EquipConf eec = Readonly.getInstance().findEquip(eee)
                        if (eec.position != ec.position) {
                            rci.result = "材料装备的部件不符"
                            break
                        }

                        if (eic.quality != needQuality) {
                            rci.result = "物品($em.eid) 对应的品质不正确"
                            break
                        } else {
                            switch (eic.quality) {
                                case 3:
                                    add = 3
                                    break;
                                case 4:
                                    add = 100
                                    break;
                                case 5:
                                    add = 2500
                                    break;
                            }
                            add *= em.count
                        }
                    } else {
                        rci.result = "没有那么多碎片(" + em.eid + ")"
                        break;
                    }
                }

                wexp += add
                while (wexp >= need && wake <= 9) { // 大于9级不能升级了
                    ++wake
                    wexp -= need
                    if (wake < 10) {
                        need = ec.wakencost[wake] // 需要的经验
                    }
                }
            } else {
                rci.result = "参数错误"
            }
        }

        if (Strings.isNullOrEmpty(rci.result)) {
            for (EquipMat em : mats) {
                if (em != null) {
                    if (em.count == 0) { // 整个装备
                        pb.payEquip(em.eid, "觉醒装备(${ic.good})")
                    } else { // 碎片
                        user.payItem(em.eid, em.count, "觉醒装备(${ic.good})")
                    }
                }
            }
            eb.wake = wake
            eb.wexp = wexp
            rci.level = wake
            rci.exp = wexp
        }

        user.send(rci)
    }
}

