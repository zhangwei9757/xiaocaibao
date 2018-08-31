package com.tumei.game.protos.misc;

import com.google.common.base.Strings;
import com.tumei.common.Readonly;
import com.tumei.common.utils.ErrCode;
import com.tumei.game.GameUser;
import com.tumei.model.ActivityBean;
import com.tumei.model.PackBean;
import com.tumei.model.beans.AwardBean;
import com.tumei.modelconf.ChestConf;
import com.tumei.websocket.BaseProtocol;
import com.tumei.websocket.SessionUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求升级装备
 */
@Component
public class RequestUseItem extends BaseProtocol {
	public int seq;

	public int item;
	public int count;
	/**
	 * 可选的宝箱，必须带一个这个参数传递过来
	 */
	public int param;

	class ReturnUseItem extends BaseProtocol {
		public int seq;
		public String result = "";
		public List<AwardBean> awards = new ArrayList<>();
	}

	@Override
	public void onProcess(SessionUser session) {
		GameUser user = (GameUser) session;

		ReturnUseItem rci = new ReturnUseItem();
		rci.seq = seq;

		if (count <= 0) {
			rci.result = ErrCode.未知参数.name();
			user.send(rci);
			return;
		}

		PackBean pb = user.getDao().findPack(user.getUid());
		if (!pb.contains(item, count)) {
			rci.result = "没有那么多的物品可使用";
			user.send(rci);
			return;
		}

		ChestConf cc = Readonly.getInstance().findChest(item);
		if (cc != null) {
			if (cc.mode == 0) { // 自动打开
				// 多次自动帮助打开宝箱
				for (int i = 0; i < count; ++i) {
					rci.awards.addAll(user.addItem(item, 1, true, "开宝箱"));
				}
			} else { // 手动打开，则通过客户端传入的param来判断是否属于这个宝箱的选择之一
				boolean ok = false;
				int c = 0;
				for (int[] pp : cc.box1) {
					if (pp[0] == param) {
						//ok.
						ok = true;
						c = pp[1];
						break;
					}
				}

				if (!ok) {
					rci.result = "您选择的奖励不存在于宝箱列表中";
				} else {
					for (int i = 0; i < count; ++i) {
						rci.awards.addAll(user.addItem(param, c, false, "开宝箱手动"));
					}
				}
			}
		} else {
			switch (item) {
				case 6001: // 活力药剂
					pb.flushSpirit(10 * count);
					rci.awards.add(new AwardBean(活力, 10 * count, 0));
					break;
				case 八折夺宝令:
				{
					ActivityBean ab = user.getDao().findActivity(user.getUid());
					long ct = ab.flushDbCouple();
					if (ct != 0) {
						rci.result = "正在打折中";
					} else {
						ab.setCoupleTime(System.currentTimeMillis()/1000 + 600);
						ab.setCoupleType(1);
					}
					break;
				}
				case 九折夺宝令:
				{
					ActivityBean ab = user.getDao().findActivity(user.getUid());
					long ct = ab.flushDbCouple();
					if (ct != 0) {
						rci.result = "正在打折中";
					} else {
						ab.setCoupleTime(System.currentTimeMillis()/1000 + 600);
						ab.setCoupleType(2);
					}
					break;
				}
				default:
					rci.result = "该物品不可使用，未加入到使用配置表中。";
					break;
			}
		}

		if (Strings.isNullOrEmpty(rci.result)) {
			user.payItem(item, count, "使用");
		}

		user.send(rci);
	}
}
