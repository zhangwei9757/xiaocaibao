package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameUser
import com.tumei.model.HerosBean
import com.tumei.model.PackBean
import com.tumei.model.beans.ArtifactBean
import com.tumei.model.beans.ArtifactComBean
import com.tumei.modelconf.ArtifactConf
import com.tumei.modelconf.ArtpartConf
import com.tumei.modelconf.ArtpartstrConf
import com.tumei.modelconf.ArtpartstupConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 神器组件 进阶和升星
 */
class RequestArtcomUp extends BaseProtocol {
    public int seq

    // 神器id
    public int artid

    // 神器组件id
    public int comid

    /**
     * 0 进阶
     * 1 升星
     * 2 十次升星
     */
    public int mode

    class Return extends BaseProtocol {
        public int seq

        public String result = ""

        // 当前的星级
        public int star

        // 如果十连给一个十连的返回每次暴击的情况,1标识加1，2标识暴击加2
        public List<Integer> stars = new ArrayList<>()
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        HerosBean hsb = DaoService.getInstance().findHeros(user.uid)

        ArtifactBean ab = hsb.getArtifacts().getOrDefault(artid, null)
        if (ab == null) {
            rci.result = "神器未激活"
            user.send(rci)
            return
        }

        ArtifactComBean acb = ab.getComs().getOrDefault(comid, null)
        if (acb == null) {
            rci.result = "神器组件未激活"
            user.send(rci)
            return
        }

        ArtpartConf artpart = Readonly.getInstance().findArtpart(comid)

        switch (mode) {
            case 0: // 进阶
                List<ArtpartstrConf> ascs = Readonly.getInstance().getArtpartstrConfs()
                if (acb.level >= ascs.size()) {
                    rci.result = "已经达到最大等级"
                } else {
                    ArtpartstrConf asc = ascs.get(acb.level-1)
                    int[] cost = asc.cost5
                    switch (artpart.quality) {
                        case 1:
                            cost = asc.cost1
                            break;
                        case 2:
                            cost = asc.cost2
                            break;
                        case 3:
                            cost = asc.cost3
                            break;
                        case 4:
                            cost = asc.cost4
                            break;
                    }
                    PackBean pb = DaoService.getInstance().findPack(user.uid)
                    if (!pb.contains(Defs.金币, cost[2])) {
                        rci.result = "金币不足"
                        user.send(rci)
                        return
                    }

                    if (!pb.hasArtcom(comid, cost[0])) {
                        rci.result = "材料不足"
                    } else {
                        if (!pb.contains(Defs.铸造石, cost[1])) {
                            rci.result = "材料不足"
                        } else {
                            pb.payArtcom(comid, cost[0], "神器部件进阶")
                            pb.payItem(Defs.铸造石, cost[1], "神器部件进阶")
                            pb.payItem(Defs.金币, cost[2], "神器部件进阶")
                            ++(acb.level)
                        }
                    }
                }
                break;
            case 1:
                sx(acb, rci, artpart.quality, user.uid, 1)
                break;
            case 2:
                sx(acb, rci, artpart.quality, user.uid, 10)
                break;
            default:
                rci.result = "参数错误"
                break;
        }

        user.send(rci)
    }

    void sx(ArtifactComBean acb, Return rci, int quality, long uid, int count) {
        List<ArtpartstupConf> ascs = Readonly.getInstance().getArtpartstupConfs()
        int grade = acb.star / 10;
        ArtpartstupConf asc = ascs.get(grade)
        if (asc.cost1 == -1) {
            rci.result = "已经达到最大星级"
        } else {
            int cost = asc.cost5
            int rate = asc.rate5
            switch (quality) {
                case 1:
                    cost = asc.cost1
                    rate = asc.rate1
                    break
                case 2:
                    cost = asc.cost2
                    rate = asc.rate2
                    break
                case 3:
                    cost = asc.cost3
                    rate = asc.rate3
                    break
                case 4:
                    cost = asc.cost4
                    rate = asc.rate4
                    break
            }

            PackBean pb = DaoService.getInstance().findPack(uid)
            if (!pb.contains(Defs.玛瑙, cost * count)) {
                rci.result = "玛瑙不足"
            } else {
                pb.payItem(Defs.玛瑙, cost * count, "神器升星")
                for (int i = 0; i < count; ++i) {
                    if (RandomUtil.getBetween(1, 100) <= rate) {
                        acb.star += 2
                        rci.stars.add(2)
                    } else {
                        acb.star += 1
                        rci.stars.add(1)
                    }
                }
                if (acb.star > 480) {
                    acb.star = 480
                }
                rci.star = acb.star
            }
        }
    }
}

