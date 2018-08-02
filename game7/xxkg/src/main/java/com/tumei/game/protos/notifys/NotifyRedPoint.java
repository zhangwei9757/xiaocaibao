package com.tumei.game.protos.notifys;

import com.tumei.common.DaoGame;
import com.tumei.game.GameUser;
import com.tumei.model.ChargeBean;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by leon on 2016/12/31.
 * 红点通知协议, 规定了下列通知类型:
 * <p>
 * 1. 大小月卡年卡是否未购买，只要一个未购买就有红
 * 2. 是否首充
 * 3. 签到
 * 4. 单冲
 * 5. 累计充值
 * 6. 征战奖励
 * 7. 地精宝藏
 * 8. 开服基金是否购买
 * 9. 7日登录活动是否有奖励未领取
 * <p>
 * 10. 会员每日礼包
 * 11. 会员每周礼包
 * 12. 英雄福利
 * 13. 神秘宝藏
 * <p>
 * <p>
 * <p>
 * <p>
 * 100. 好友申请红点
 * 101. 有好友赠送活力
 *
 * 1000. 邮件通知, 只要有奖励邮件存在，或者信息邮件存在都通知
 * 9999. 服务器检查红点, 上线
 * 10000. 隔天
 */
@Component
public class NotifyRedPoint extends BaseProtocol {
	public HashMap<Integer, Integer> infos = new HashMap<>();

	public static void report(GameUser user) {
		NotifyRedPoint rl = new NotifyRedPoint();
		rl.infos.put(9999, 1);

		long uid = user.getUid();
		DaoGame dao = user.getDao();

		long now = System.currentTimeMillis() / 1000;
		// 1. 大小月卡 年卡 通知,
		{
			ChargeBean cb = dao.findCharge(uid);
//			if (now >= cb.getYear() || now >= cb.getMonth() || now >= cb.getBigmonth()) {
//				rl.infos.put(1, 1);
//			}

			// 2. 首充通知
			if (cb.getTotal() <= 0) {
				rl.infos.put(2, 1);
			}
		}

		// 3. 签到通知，包含未完整签到，签到礼包未领取
//		{
//			ActivityBean ab = dao.findActivity(uid);
//			ab.flush();
//			if (ab.getSignState() < 2) {
//				rl.infos.put(3, 1);
//			}
//
//			// 4. 单冲送礼 未领取
//			ab.flushSingle();
//			List<SingleStruct> sss = ab.getSingleChargeAwards();
//			for (SingleStruct ss : sss) {
//				if (ss.count > ss.used) {
//					rl.infos.put(4, 1);
//					break;
//				}
//			}
//
//			// 5. 累冲
//			ab.flushCum();
//			List<Integer> css = ab.getCumChargeAwards();
//			for (int cs : css) {
//				if (cs == -1) {
//					rl.infos.put(5, 1);
//					break;
//				}
//			}
//
//			// 6. 征战奖励
//			ab.flushCampaign();
//			List<Integer> st = ab.getCampaignStatus();
//			if (st.stream().anyMatch((s) -> s == -1)) {
//				rl.infos.put(6, 1);
//			}
//
//			// 7. 地精宝藏 摇钱树
//			if (ab.getGoldIndex() == 0) {
//				rl.infos.put(7, 1);
//			}
//
//			// 8. 开服基金，没有购买和可以领取都要红点
//			if (ab.getFund() == 0) {
//				rl.infos.put(8, 1);
//			} else {
//				if (ab.checkFundsNotGet()) {
//					rl.infos.put(8, 1);
//				}
//			}
//
//			// 9. 七日登录
//			int[] sevenDays = ab.getSevenDays();
//			for (int s : sevenDays) {
//				if (s == 0) {
//					rl.infos.put(9, 1);
//					break;
//				}
//			}
//
//			if (ab.getVipDailyBag() == 0) {
//				rl.infos.put(10, 1);
//			}
//			if (ab.getVipWeekBag() == 0) {
//				rl.infos.put(11, 1);
//			}
//
//			if (ab.getLogoFuli().values().stream().anyMatch((v) -> v == 1) ||
//				ab.getLevelFuli().values().stream().anyMatch((v) -> v == 1)) {
//				rl.infos.put(12, 1);
//			}
//
//			TreasureBean tb = user.getDao().findTreasure(user.getUid());
//			tb.flush();
//			if (tb.getDigCount() < 3) {
//				rl.infos.put(13, 1);
//			}
//
//			MailsBean msb = dao.findMails(uid);
//			if (msb.getAwards().size() > 0 || msb.getInfos().size() > 0) {
//				rl.infos.put(1000, 1);
//			}
//		}
		user.send(rl);
	}
}
