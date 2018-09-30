package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameUser
import com.tumei.model.ActivityBean
import com.tumei.model.PackBean
import com.tumei.model.beans.AwardBean
import com.tumei.modelconf.ArtsummonConf
import com.tumei.modelconf.VipConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

import java.util.stream.Collector
import java.util.stream.Collectors

/**
 * Created by Administrator on 2017/3/13 0013.
 * 神器召唤信息页面
 */
class RequestArtSummon extends BaseProtocol {
    public int seq

    /**
     * 0 单抽
     * 1 五连抽
     */
    public int mode

    class Return extends BaseProtocol {
        public int seq

        public List<AwardBean> awards = new ArrayList<>()

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        ActivityBean ab = DaoService.instance.findActivity(user.uid)
        ab.flushArtSummon()

        VipConf vc = Readonly.instance.findVip(user.vip)
        if (vc.artcall - ab.getArtToday() <= 0) {
            rci.result = "今日召唤次数已经使用完毕"
            user.send(rci)
            return
        }

        List<ArtsummonConf> ascs = Readonly.instance.getArtsummonConfs()

        PackBean pb = DaoService.instance.findPack(user.uid)
        if (mode == 0) { // 单抽
            if (ab.getArtFree() == 0) {
                // 检查先知之眼
                if (pb.contains(Defs.先知之眼, 1)) {
                    pb.payItem(Defs.先知之眼, 1, "神器召唤")
                } else if (pb.contains(Defs.钻石, Defs.神器召唤)) {
                    // 神器召唤钻石*30,原价480
                    pb.payItem(Defs.钻石, Defs.神器召唤, "神器召唤")
                } else {
                    rci.result = "钻石不足"
                    user.send(rci)
                    return
                }
            }

            ++(ab.artTotal)
            if ((ab.artTotal % 20) == 0) { // 20次必得橙色
                List<ArtsummonConf> list = ascs.stream().filter({asc -> asc.team >= 4}).collect(Collectors.toList())
                int idx = RandomUtil.getRandom() % list.size()
                ArtsummonConf asc = list.get(idx) as ArtsummonConf
                rci.awards.addAll(user.addItems(asc.good, false, "神器召唤"))
            } else {
                // 开始召唤
                int total = 0
                for (def asc : ascs) {
                    total += asc.hev
                }

                float r = RandomUtil.getFloat()
                float cum = 0
                for (def asc : ascs) {
                    cum += asc.hev
                    if (r <= cum / total) {
                        rci.awards.addAll(user.addItems(asc.good, false, "神器召唤"))
                        break
                    }
                }
            }

            ab.artFree = 0
            ++(ab.artToday)
            rci.awards.addAll(user.addItem(Defs.神器精华, 4, false, "神器召唤"))
        } else { // 五连

            if (!pb.contains(Defs.钻石, Defs.神器召唤五连)) {
                rci.result = "钻石不足"
                user.send(rci)
                return
            }

            pb.payItem(Defs.钻石, Defs.神器召唤五连, "神器五连召唤")

            boolean gotgold = false;
            for (int i = 0; i < 5; ++i) {
                ab.artTotal += 1
                if (i == 0) { // 五连抽第一个必定是紫色,如果第一个也满足金色,就标记之后再发
                    def list = ascs.stream().filter({asc -> asc.team >= 3 && asc.team < 4}).collect(Collectors.toList() as Collector<? super ArtsummonConf, Object, Object>)
                    int idx = (int) (RandomUtil.getRandom() % list.size())
                    ArtsummonConf asc = list.get(idx) as ArtsummonConf
                    rci.awards.addAll(user.addItems(asc.good, false, "神器召唤"))
                    if (ab.artTotal % 20 == 0) {
                        gotgold = true
                    }
                } else if (gotgold || (ab.artTotal % 20 == 0)) { // 非第一次,如果满足金色,则发金色
                    def list = ascs.stream().filter({asc -> asc.team >= 4}).collect(Collectors.toList())
                    int idx = RandomUtil.getRandom() % list.size()
                    ArtsummonConf asc = list.get(idx) as ArtsummonConf
                    rci.awards.addAll(user.addItems(asc.good, false, "神器召唤"))
                    gotgold = false
                } else { // 以上都不满足就真概率随机
                    // 开始召唤
                    int total = 0
                    for (ArtsummonConf asc : ascs) {
                        total += asc.hev
                    }

                    float r = RandomUtil.getFloat()
                    float cum = 0
                    for (def asc : ascs) {
                        cum += asc.hev
                        if (r <= cum / total) {
                            rci.awards.addAll(user.addItems(asc.good, false, "神器召唤"))
                            break
                        }
                    }
                }
            }

            rci.awards.addAll(user.addItem(Defs.神器精华, 20, false, "神器召唤"))
            ab.artToday += 5
        }



        user.send(rci)
    }
}

