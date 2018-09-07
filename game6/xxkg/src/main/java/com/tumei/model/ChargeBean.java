package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.GameConfig;
import com.tumei.common.DaoService;
import com.tumei.common.LocalService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameServer;
import com.tumei.game.protos.structs.SingleStruct;
import com.tumei.model.beans.ChargeDayBean;
import com.tumei.modelconf.CumrechargeConf;
import com.tumei.modelconf.RecreturnConf;
import com.tumei.modelconf.SinglerechargeConf;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.tumei.common.utils.Defs.钻石;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Role.Charge")
public class ChargeBean {
	@JsonIgnore
	@Id
	private String ObjectId;
	@Field("id")
	@Indexed(unique = true, name = "i_id")
	private Long id;

	/**
	 * 总充值（单位 分)
	 */
	private int total;

	/**
	 * 月卡
	 */
	private long month;

	/**
	 * 大月卡
	 */
	private long bigmonth;

	/**
	 * 年卡
	 */
	private long year;

	/**
	 * 最近发放各种卡的日期
	 */
	private int sendMonthDay;
	private int sendBigMonthDay;
	private int sendYearDay;

	/**
	 * 每日充值列表
	 */
	private List<ChargeDayBean> dayCharges = new ArrayList<>();

	/**
	 * 各种档位的充值次数, key: 600分， value: 2次，
	 */
	private HashMap<Integer, Integer> counts = new HashMap<>();

	/**
	 * 当前处于的buff周期
	 */
	private int buffDay;

	/**
	 * 最后一次发送充值奖励时间
	 * */
	private int lastDay;

	private HashSet<Integer> buffCounts = new HashSet<>();

	public String getObjectId() {
		return ObjectId;
	}

	public void setObjectId(String objectId) {
		ObjectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<ChargeDayBean> getDayCharges() {
		return dayCharges;
	}

	public void setDayCharges(List<ChargeDayBean> dayCharges) {
		this.dayCharges = dayCharges;
	}

	public long getMonth() {
		return month;
	}

	public void setMonth(long month) {
		this.month = month;
	}

	public long getBigmonth() {
		return bigmonth;
	}

	public void setBigmonth(long bigmonth) {
		this.bigmonth = bigmonth;
	}

	public long getYear() {
		return year;
	}

	public void setYear(long year) {
		this.year = year;
	}

	public HashMap<Integer, Integer> getCounts() {
		return counts;
	}

	public void setCounts(HashMap<Integer, Integer> counts) {
		this.counts = counts;
	}

	public int getBuffDay() {
		return buffDay;
	}

	public void setBuffDay(int buffDay) {
		this.buffDay = buffDay;
	}

	public HashSet<Integer> getBuffCounts() {
		return buffCounts;
	}

	public void setBuffCounts(HashSet<Integer> buffCounts) {
		this.buffCounts = buffCounts;
	}

	/**
	 * 判断某种充值档位是多少分
	 * @param rmb 分
	 * @return
	 */
	public boolean isFirstCharge(int rmb) {
		return (counts.getOrDefault(rmb, 0) == 0);
	}

	public int getSendMonthDay() {
		return sendMonthDay;
	}

	public void setSendMonthDay(int sendMonthDay) {
		this.sendMonthDay = sendMonthDay;
	}

	public int getSendBigMonthDay() {
		return sendBigMonthDay;
	}

	public void setSendBigMonthDay(int sendBigMonthDay) {
		this.sendBigMonthDay = sendBigMonthDay;
	}

	public int getSendYearDay() {
		return sendYearDay;
	}

	public void setSendYearDay(int sendYearDay) {
		this.sendYearDay = sendYearDay;
	}

	public void addDayCharge(int rmb) {
		int today = TimeUtil.getToday();
		Optional<ChargeDayBean> opt = dayCharges.stream().filter(cdb -> cdb.day == today).findFirst();
		ChargeDayBean cdb;
		if (opt.isPresent()) {
			cdb = opt.get();
			cdb.rmb += rmb;
			cdb.rmbs.add(rmb);
		} else {
			cdb = new ChargeDayBean(today, rmb);
			dayCharges.add(cdb);
		}
	}

	/**
	 * 充值, rmb（分）
	 *
	 * @param rmb
	 * @param buff 是否处于buff 奖励中.
	 *
	 * @source 充值来源
	 *
	 * @return 返回充值获得的钻石
	 */
	public int doCharge(int rmb, String source, int buff) {
		int gem = rmb / 10;
		// 增加购买该档次的次数
		int count = getCounts().getOrDefault(rmb, 0);

		switch (rmb) {
			case 2500:
				// 查询月卡
			{
				gem = 250; // 月卡一次性到账
				long now = System.currentTimeMillis() / 1000;
				if (getMonth() < now) {
					setMonth(now + 3600 * 24 * 30);
				}
				else {
					setMonth(getMonth() + 3600 * 24 * 30);
				}
				break;
			}
			case 5000: {
				gem = 500; // 月卡一次性到账
				long now = System.currentTimeMillis() / 1000;
				if (getBigmonth() < now) {
					setBigmonth(now + 3600 * 24 * 30);
				}
				else {
					setBigmonth(getBigmonth() + 3600 * 24 * 30);
				}
				break;
			}
			case 9800: {
				gem = 500; // 年卡一次性到账
				long now = System.currentTimeMillis() / 1000;
				if (getYear() < now) {
					setYear(now + 3600 * 24 * 365);
				}
				else {
					setYear(getYear() + 3600 * 24 * 365);
				}
				break;
			}
			default:
				// 1. 如果非双倍的情况 网页价格另外一套
				// 2. 网页设置一个特殊的标记，当标记开启的时候进行一次网页自己的特殊重置
				//
				if (source.equals("云顶网页") || source.equals("爱贝")) {
					switch (rmb) {
						case 600:
							gem = 66;
							break;
						case 3000:
							gem = 350;
							break;
						case 6800:
							gem = 780;
							break;
						case 19800:
							gem = 2500;
							break;
						case 32800:
							gem = 4200;
							break;
						case 64800:
							gem = 8500;
							break;
						case 100000:
							gem = 13300;
							break;
						case 300000:
							gem = 41000;
							break;
						case 500000:
							gem = 70000;
							break;
					}
				}

				if (rmb < 1000_00) { // 1000元以内的充值会有双倍激励
					if (buff != 0) { // 进入活动的双倍计算中
						// 判断活动日是否重复，清空活动记录
						if (buff != buffDay) {
							buffDay = buff;
							buffCounts.clear();
						}
						// 如果在本次活动是第一次购买，则双倍
						if (!buffCounts.contains(rmb)) {
							gem *= 2;
							buffCounts.add(rmb);
						}
					} else {
						// 统一的双倍计算
						if (count == 0) { // 双倍
							gem *= 2;
						}
					}
				}
				break;
		}

		PackBean pb = DaoService.getInstance().findPack(this.id);
		// 充值所有获得钻石*50 然后增加1588充值档位,注意区是BT别服
		if (Defs.ISBT) {
			gem *= 50;
		}
		pb.addItem(钻石, gem, "充值");
		total += rmb;
		counts.put(rmb, ++count);
		addDayCharge(rmb);

		// 即使伪造的充值，也需要能满足活动
		flushActivity(rmb);
		// 节日活动检查和记录
		DaoService.getInstance().findFestival(this.id).onCharge(rmb);
		return gem;
	}

	/**
	 * 充值完成后，更新活动中要求的充值记录
	 *
	 * @param rmb
	 */
	public void flushActivity(int rmb) {
		LocalService r = LocalService.getInstance();

		ActivityBean ab = DaoService.getInstance().findActivity(this.id);
		ab.flush();
		// 夺宝相关
		ab.putDbcharges(rmb);

		// 单冲
		{
			ab.flushSingle();
			List<SingleStruct> st = ab.getSingleChargeAwards();

			int i = 0;
			int day = r.getSingleCur() % GameConfig.getInstance().getSinglePeriod();

			// 获取今天开始，前day天一共的所有充值记录
			List<ChargeDayBean> cdbs = getChargesFromToday(day);

			for (SingleStruct ss : st) {
				// 充值记录从最后一天开始看起，如果最后一天是今天，则倒数
				int num = 0;
				for (ChargeDayBean cdb : cdbs) {
					num += cdb.rmbs.stream().filter((rr) -> rr == ss.rmb).count();
				}

				// 本档位充值的次数大于可领取的限制后，不增加
				SinglerechargeConf sc = Readonly.getInstance().getSingleConfs().get(r.getSingleBeginIdx() + i);
				if (num > sc.limit) {
					num = sc.limit;
				}
				ss.count = num;
				++i;
			}
		}

		// 累冲
		{
			ab.flushCum();
			int day = r.getCumCur() % GameConfig.getInstance().getCumPeriod();
			// 获取今天开始，前day天一共的所有充值记录
			List<ChargeDayBean> cdbs = getChargesFromToday(day);

			// 计算这几天的总充值记录
			int num = 0;
			for (ChargeDayBean cdb : cdbs) {
				num += cdb.rmb;
			}

			List<CumrechargeConf> ccs = Readonly.getInstance().getCumConfs();
			List<Integer> status = ab.getCumChargeAwards();
			for (int i = r.getCumBeginIdx(), j = 0; i <= r.getCumEndIdx(); ++i, ++j) {
				if (status.get(j) < 0) {
					continue;
				}

				CumrechargeConf cc = ccs.get(i);
				// 检查当前指定的充值记录是否合格
				if (cc.cost * 100 <= num) {
					status.set(j, -1);
				} else {
					status.set(j, num);
				}
			}
		}
	}

	// 获取 从 -day前到今日为止的所有充值记录
	// day == 0,表示今日
	// day == 1,表示昨日和今日
	public List<ChargeDayBean> getChargesFromToday(int day) {
		List<ChargeDayBean> rtn = new ArrayList<>();
		LocalDate today = LocalDate.now();
		for (int i = 0; i <= day; ++i) {
			LocalDate ld = today.minusDays(i);
			int d = TimeUtil.getDay(ld);
			dayCharges.stream().filter(cdb -> {
				return cdb.day == d;
			}).findFirst().ifPresent(cdb -> rtn.add(cdb));
		}
		return rtn;
	}

	/**
	 * 检测各种卡是否发送，然后发送
	 */
	public ChargeBean checkSendCards() {
		long now = System.currentTimeMillis() / 1000;
		int day = TimeUtil.getToday();

		if (now <= getMonth() && day != getSendMonthDay()) {
			// 发送月卡奖励
			GameServer.getInstance().sendAwardMail(this.id, "月卡奖励", "今日月卡奖励", String.format("%s,%d", 钻石, Defs.今日月卡奖励));
			setSendMonthDay(day);
		}
		if (now <= getBigmonth() && day != getSendBigMonthDay()) {
			// 发送大月卡奖励
			GameServer.getInstance().sendAwardMail(this.id, "高级月卡奖励", "今日高级月卡奖励", String.format("%s,%d", 钻石, Defs.今日高级月卡奖励));
			setSendBigMonthDay(day);
		}
		if (now <= getYear() && day != getSendYearDay()) {
			// 发送年卡奖励
			GameServer.getInstance().sendAwardMail(this.id, "年卡奖励", "今日年卡奖励", String.format("%s,%d", 钻石, Defs.今日年卡奖励));
			setSendYearDay(day);
		}
		return this;
	}

	/**
	 * 每次登陆，刷新累计充值奖励邮件发送
	 * */
	public void flushChargeForMail(){
		int today = TimeUtil.getToday();
		if (lastDay < today) {
			// 表示上次最后登陆时间当天累计充值奖励未发送
			List<RecreturnConf> recreturnConfs = Readonly.getInstance().getRecreturnConfs();
			RecreturnConf rc = null;

			if (recreturnConfs.size() > 0) {
				int lastDayCharges = 0;
				Optional<ChargeDayBean> opt = dayCharges.stream().filter(cdb -> cdb.day == lastDay).findFirst();

				if (opt.isPresent()) {
					for (int i : opt.get().rmbs) {
						lastDayCharges += i;
					}
				}

				lastDayCharges /= 100;

				// 匹配对应次数配置表获取信息
				for (RecreturnConf r : recreturnConfs) {
					if (lastDayCharges >= r.total1) {
						rc = r;
						break;
					}
				}
				if (rc != null) {
					int receiveGem = (int)(lastDayCharges * rc.returngem * 5);
					GameServer.getInstance().sendAwardMail(this.id, "单日充值返利", String.format("单日充值<color=red>%d</color>元奖励", lastDayCharges), String.format("%d,%d", 钻石, receiveGem));
				}
			}
			// 奖励发送完以后,重置最后发送时间
			lastDay = today;
		}

	}
}
