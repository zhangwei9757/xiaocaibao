package com.tumei.groovy.commands.protos

import com.tumei.common.DaoGame
import com.tumei.common.Readonly
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.common.utils.RandomUtil
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.model.beans.AwardBean
import com.tumei.modelconf.GlorychestConf
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * Created by Administrator on 2017/3/13 0013.
 * 圣物碎片召唤
 */
class RequestRelicSummon extends BaseProtocol {
    public int seq

    /**
     * 0: 表示单抽
     * 1: 表示十连抽
     */
    public int mode

    class Return extends BaseProtocol {
        public int seq
        public String result = ""

        // 获得的具体物品
        public List<AwardBean> awards = new ArrayList<>()
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        PackBean pb = DaoGame.instance.findPack(user.uid)

        int gem = 100

        if (mode == 1) {
            gem = 980
        }

        if (!pb.contains(Defs.圣物之魂, gem)) {
            rci.result = ErrCode.圣物之魂不足
        } else {
            user.payItem(Defs.圣物之魂, gem, "抽圣物")

            List<GlorychestConf> gcs = Readonly.instance.getGlorychests()

            // 按照概率进行抽
            if (mode == 1) { // 十连
                for (int i = 0; i < 9; ++i) {
                    int[] reward
                    int total = 0
                    int r = RandomUtil.getBetween(1, 100)
                    for (GlorychestConf gc : gcs) {
                        total += gc.rate1
                        if (r <= total) {
                            reward = gc.rewards
                            break
                        }
                    }

                    if (reward != null) {
                        rci.awards.addAll(user.addItems(reward, true, "抽圣物"))
                    }
                }

                rci.awards.addAll(user.addItem(5555, 1, true, "抽圣物"))
            } else { // 单抽
                int[] reward
                int total = 0
                int r = RandomUtil.getBetween(1, 100)
                for (GlorychestConf gc : gcs) {
                    total += gc.rate1
                    if (r <= total) {
                        reward = gc.rewards
                        break
                    }
                }

                if (reward != null) {
                    rci.awards.addAll(user.addItems(reward, true, "抽圣物"))
                }
            }
        }

        user.send(rci)
    }
}

