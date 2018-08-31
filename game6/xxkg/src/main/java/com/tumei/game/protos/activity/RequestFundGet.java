package com.tumei.game.protos.activity;

import com.tumei.common.LocalService;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.FundConf;
import com.tumei.common.Readonly;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by leon on 2016/12/31.
 * 请求月卡信息
 *
 */
@Component
public class RequestFundGet extends BaseProtocol {
    public int seq;
	//[0....] 顺序
	public int index;
	/**
	 * 0:
	 * 1: 第二页
	 */
	public int mode;

    class ReturnFundGet extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
    }

    @Override
    public void onProcess(SessionUser session) {
        GameUser user = (GameUser)session;
		ReturnFundGet rl = new ReturnFundGet();
		rl.seq = seq;

		ActivityBean ab = user.getDao().findActivity(user.getUid());
		if (ab.getFund() == 0) {
			rl.result = "尚未购买开服基金";
			user.send(rl);
			return;
		}

		ab.flushFunds();

		if (mode == 0) {
			int[] funds = ab.getFundStates();
			if (index < 0 || index >= funds.length || funds[index] > 1) {
				rl.result = "已经领取该基金";
				user.send(rl);
				return;
			}

			if (funds[index] == 0) {
				rl.result = "没有满足条件";
				user.send(rl);
				return;
			}

			FundConf fc = Readonly.getInstance().getFundConf(index);
			funds[index] = 2;
			rl.awards.addAll(user.addItem(钻石, fc.reward1[1], false, "基金一"));
		} else {
			int[] funds = ab.getFundStates2();
			if (index < 0 || index >= funds.length || funds[index] > 1) {
				rl.result = "已经领取该基金";
				user.send(rl);
				return;
			}

			if (funds[index] == 0) {
				rl.result = "没有满足条件";
				user.send(rl);
				return;
			}

			FundConf fc = Readonly.getInstance().getFundConf(index);
			funds[index] = 2;
			rl.awards.addAll(user.addItem(fc.reward2[1], fc.reward2[2], false, "基金二"));
		}

        user.send(rl);
    }
}
