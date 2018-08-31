package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.game.GameUser
import com.tumei.model.beans.AwardBean
import com.tumei.model.festival.*
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 获取 节日活动的中的登陆奖励
 */
class RequestFestivalGet extends BaseProtocol {
    public int seq
    // [0，n] 顺序发送过来即可
    public int index;
    // 登陆
    // 单充
    // 消费
    // 神器累充
    public int mode;

    class Return extends BaseProtocol {
        public int seq

        public List<AwardBean> awards = new ArrayList<>();

        public String result = ""
    }

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        FestivalBean fb = DaoService.getInstance().findFestival(user.uid)
        fb.flush()

        if (fb.mode < 0) {
            rci.result = "节日活动已经结束,请下次再来"
        } else {
            switch (mode) {
                case 0: // 登录领取的奖励
                    if (fb.getFlag() != 0) {
                        rci.result = "错误的活动参数"
                    } else {
                        if (fb.islog != 0) {
                            rci.result = "今日已经领取登录奖励"
                        } else {
                            for (int i = 0; i < fb.festLogons.size(); ++i) {
                                FestivalLogon fl = fb.festLogons[i]
                                if (fl.used == 0) {
                                    rci.awards.addAll(user.addItems(fl.rewards, false, "节日登录${fl.key}"))
                                    fl.used = 1
                                    fb.islog = 1
                                    break;
                                }
                            }
                        }
                    }
                    break
                case 1: // 单冲奖励
                    if (index < 0 || index >= fb.festSingles.size() || fb.getFlag() != 0) {
                        rci.result = "参数错误"
                    } else {
                        FestivalSingle fs = fb.festSingles.get(index);
                        if (fs.used >= fs.limit) {
                            rci.result = "已经达到最大值,无法再次领取"
                        } else {
                            if (fs.used >= fs.count) {
                                rci.result = "单笔充值满足条件才能领取奖励"
                            } else {
                                ++(fs.used)
                                rci.awards.addAll(user.addItems(fs.reward, false, "节日单充${fs.cost}"))
                            }
                        }
                    }
                    break
                case 2: // 消费
                    if (index < 0 || index >= fb.festSpends.size() || fb.getFlag() != 0) {
                        rci.result = "参数错误"
                    } else {
                        FestivalSpend fs = fb.festSpends.get(index)
                        if (fs.used != 0) {
                            rci.result = "已经领取"
                        } else {
                            if (fb.spend < fs.spend) {
                                rci.result = "节日内消费钻石(${fb.spend})枚,不满足领取条件"
                            } else {
                                fs.used = 1
                                rci.awards.addAll(user.addItems(fs.reward, false, "节日消费${fs.spend}"))
                            }
                        }
                    }
                    break
                case 3: // 神器累充
                    if (index < 0 || index >= fb.festCums.size() || fb.getFlag() == 0) {
                        rci.result = "参数错误"
                    } else {
                        FestivalCum fc = fb.festCums.get(index)
                        if (fc.used != 0) {
                            rci.result = "已经领取"
                        } else {
                            if (fb.cum < fc.cost) {
                                rci.result = "活动期内累计充值不满足条件"
                            } else {
                                fc.used = 1
                                rci.awards.addAll(user.addItems(fc.reward, false, "神器累充${fc.cost}"))
                            }
                        }
                    }
                    break;
                default:
                    rci.result = "参数错误"
                    break
            }
        }

        user.send(rci)
    }
}
