package com.tumei.game.protos.group;

import com.google.common.base.Strings;
import com.tumei.common.Readonly;
import com.tumei.common.RemoteService;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.GroupBean;
import com.tumei.model.PackBean;
import com.tumei.modelconf.DonateConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.WebSocketUser;
import org.springframework.stereotype.Component;

import static com.tumei.common.utils.Defs.公会贡献;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 公会捐献
 */
@Component
public class RequestGroupDonate extends BaseProtocol {
    public int seq;
    /**
     * 1: 普通捐献
     * 2: 中极捐献
     * 3: 高级捐献
     */
    public int mode;

    class ReturnGroupDonate extends BaseProtocol {
        public int seq;
        public String result = "";
        /**
         * 捐献一定机率生成红包
         * key > 0 表示生成成功
         * **/
        public int key;
    }

    @Override
    public void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session;

        ReturnGroupDonate rci = new ReturnGroupDonate();
        rci.seq = seq;

        if (mode < 1 || mode > 3) {
            rci.result = ErrCode.未知参数.name();
            user.send(rci);
            return;
        }

        GroupBean gb = user.getDao().findGroup(user.getUid());
        if (gb.getGid() <= 0) {
            rci.result = "请先加入一个公会";
            user.send(rci);
            return;
        }

        gb.flush(user.getVip());

        if (gb.getDonate() != 0) {
            rci.result = "今日公会捐献次数已用完";
            user.send(rci);
            return;
        }

        DonateConf dc = Readonly.getInstance().findDonate(mode);
        PackBean pb = user.getDao().findPack(user.getUid());
        if (!pb.contains(dc.cost[0], dc.cost[1])) {
            rci.result = "捐献所需的物资不足";
            user.send(rci);
            return;
        }

        // 远程拉取公会推荐信息
        int rtn = 0;
        try {
            rtn = RemoteService.getInstance().askGroupDonate(gb.getGid(), user.getUid(), dc.reward[0], dc.reward[1], dc.reward[2]);
        } catch (Exception ex) {
            rci.result = "公会服务维护中";
        }

        if (rtn < 0) {
            switch (rtn) {
                case -98:
                    rci.result = "公会服务维护中";
                    break;
                case -999:
                    rci.result = "今日捐献已经达到上限";
                    break;
                default:
                    rci.result = "公会捐献错误";
                    break;
            }
        } else {
            gb.setDonate(mode);
            user.addItem(公会贡献, dc.reward[2], false, "捐献");
            user.payItem(dc.cost[0], dc.cost[1], "捐献");
            rci.key = rtn; // 如果能刷出红包，返回的大于0的一个值
        }

        user.send(rci);
    }
}
