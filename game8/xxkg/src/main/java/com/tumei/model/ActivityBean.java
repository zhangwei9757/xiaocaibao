package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.GameConfig;
import com.tumei.common.DaoGame;
import com.tumei.game.services.LocalService;
import com.tumei.common.Readonly;
import com.tumei.common.utils.TimeUtil;
import com.tumei.game.GameUser;
import com.tumei.game.protos.structs.SingleStruct;
import com.tumei.game.protos.structs.StoreStruct;
import com.tumei.game.services.LocalArenaService;
import com.tumei.model.beans.EquipBean;
import com.tumei.model.beans.HeroBean;
import com.tumei.model.beans.Open7Bean;
import com.tumei.modelconf.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

import static com.tumei.common.utils.Defs.夺宝积分;
import static com.tumei.common.utils.Defs.夺宝魂币;

/**
 * Created by Administrator on 2017/1/17 0017.
 * <p>
 * 各种相关的活动:
 * <p>
 * 1. 签到
 * 2. 七日登录
 */
@Data
@Document(collection = "Role.Activity")
public class ActivityBean {
	@JsonIgnore
	@Id
	private String objectId;

	@JsonIgnore
	@Field("id")
	@Indexed(unique = true, name = "i_id")
	private Long id;

	/**
	 * 上次签到时间
	 */
	private int lastSignDay;

	/**
	 * 周vip日期 周一
	 */
	private int vipWeekDay;

	/**
	 * 荣誉记录的日期  周一
	 */
	private int honorDay;

	/**
	 * 一共签到了多少天
	 */
	private int signDays;
	/**
	 * 今日签到状态
	 */
	private int signState;

	// 摇钱树今日使用次数
	private int goldTree;
	// 摇钱树奖励领取进度,初始为0，每次进步1下
	private int goldIndex;

	// 今日播放广告的次数
	private int adsCount;

	/**
	 * 7天内登录领取状态，可根据玩家帐号创建时间与当前时间的差值得到经过了多少天
	 * 然后得到一共经过了几次7日登录，刷新当前七日的数组
	 */
	private int[] sevenDays = new int[7];
	// 本次7日登录的类型，分了n组，每次都是随机得到的，全局用一个随机种子的概率即可保证服务器唯一
	private int sevenType;

	// 首充奖励是否领取
	private int firstAward;

	// 单充对应的价格和奖励已经领取次数
	private List<SingleStruct> singleChargeAwards = new ArrayList<>();

	private int lastIndex = -1;

	// 累计充值奖品是否领取
	private List<Integer> cumChargeAwards = new ArrayList<>();

	// 累计充值奖励
	private List<int[]> cumContents = new ArrayList<>();

	private int lastCumIdx = -1;

	// 购买
	private List<Integer> dcStatus = new ArrayList<>();
	// 兑换
	private List<Integer> ecStatus = new ArrayList<>();

	private int lastDcIdx = -1;

	/**
	 * 开服基金
	 */
	private int fund;
	private int[] fundStates = new int[6];
	private int[] fundStates2 = new int[11];

	// 征战活动
	private int campaignRound = -1; // 上次轮回数
	private int campaignType = -1;
	private int campaignBegin = -1; // 本次轮回开始的索引
	private int campaignEnd = -1;
	private int campaign1 = 0; // 竞技场胜利次数
	private int campaign2 = 0; // 抢夺宝物胜利次数
	private List<Integer> campaignStatus = new ArrayList<>(); // 轮回状态

	// 战力提升
	private List<Integer> powerStatus = new ArrayList<>();

	// vip每日礼包, 0表示未领取， 1表示已经领取
	private int vipDailyBag = 0;
	// vip每周礼包
	private int[] vipWeekBags = new int[20];
	// vip礼包
	private int[] vipBundles = new int[20];

	// 今日钻石炼化次数
	private int relicActivate = 0;

	// 英雄福利，等级福利与登录福利
	// key:登录的天数，value = 1表示满足未领取 2表示已经领取
	private HashMap<Integer, Integer> logoFuli = new HashMap<>();
	// 英雄最高等级
	private int maxLevel;
	private HashMap<Integer, Integer> levelFuli = new HashMap<>();
	// 神将福利
	private int heroFuli;

	private Open7Bean[] open7 = new Open7Bean[7];

	/**
	 * ---------------------------------------------   夺宝相关  ----------------------------------------
	 */
	private int dbDay = 0; // 用于记录每天消耗,
	private int dbLevel = 1; // 当前夺宝聚魂的等级，从1到4，4之后变为1
	private int dbLocalRound = -1; // 新的夺宝回合记录，以免影响老玩家
	private int dbSpend = 0;
	private int coupleType = 0;
	private long coupleTime = 0;

	private List<Integer> dbsingles = new ArrayList<>();
	private List<Integer> dbcharges = new ArrayList<>();
	private List<Integer> dbspends = new ArrayList<>();
	private List<Integer> dbscores = new ArrayList<>();
	private List<Integer> dbstores = new ArrayList<>();

	/**
	 * --------------------------------------------- 夺宝相关结束 ----------------------------------------
	 */

	/**
	 * --------------------------------------------- 跨服竞技场商店 ---------------------------------------
	 */
	private int hotArenaDay = 0;
	private List<StoreStruct> hotItems = new ArrayList<>();
	private int honor = 0;
	private int ladderCount = 0;
	// 包含已经点击领取的荣誉奖励
	private List<Integer> honorAwards = new ArrayList<>();

	/**
	 * --------------------------------------------- 跨服竞技场商店结束 ---------------------------------------
	 */

	// 尝试刷新跨服竞技场
	public void flushHotArenas() {
		int today = TimeUtil.getToday();
		if (today != hotArenaDay) {
			hotArenaDay = today;
			hotItems.clear();
			ladderCount = 0;

			List<BattlestoreConf> bcs = Readonly.getInstance().findBattleStores();
			bcs.forEach((bc) -> {
				StoreStruct ss = new StoreStruct();
				ss.key = bc.key;
				ss.id = bc.item[0];
				ss.count = bc.item[1];
				ss.price = bc.price;
				ss.limit = bc.limit;
				ss.used = 0;
				hotItems.add(ss);
			});
		}
		int weekDay = TimeUtil.getNextWeekDay();
		if (weekDay != honorDay) { // 每次周一就清理荣誉，曾经领取的荣誉奖励
			honor = 0;
			honorAwards.clear();
			honorDay = weekDay;
		}
	}

	public void flush() {
		int today = TimeUtil.getToday();
		if (today != lastSignDay) {
			if (signState != 0) {
				++signDays;
			}
			lastSignDay = today;
			signState = 0;
			goldTree = 0;
			goldIndex = 0;
			vipDailyBag = 0;
			relicActivate = 0;
			adsCount = 0;

			flushLogDays();
		}

		int weekDay = TimeUtil.getNextWeekDay();
		if (weekDay != vipWeekDay) { // 每次周一就刷新
			vipWeekDay = weekDay;
			for (int i = 0; i < vipWeekBags.length; ++i) {
				vipWeekBags[i] = 0;
			}
		}
	}


	/**
	 * 征战活动
	 */
	public void flushCampaign() {
		Date open = LocalService.getInstance().getOpenDate();
		int days = TimeUtil.pastDays(open) + 1;
		// 根据距离的天数和 间隔时间, 算出本次应该在第几个轮回, 并且本轮回已经经过了几天
		int round = days * 2 + (((days % 2) == 0) ? 0 : 1);

		List<CampaignactConf> ccs = Readonly.getInstance().getCampaignactConfs();

		// 根据轮回数比较上次轮回数，如果不同，则更新当前的状态
		if (campaignRound != round) {
			campaignRound = round;
			campaignStatus.clear();
			campaign1 = campaign2 = 0;
			campaignBegin = -1;

			// 获取配置的类型有多少种, 根据轮回得到当前应该在类型多少
			campaignType = round % Readonly.getInstance().getCampaignactTypes() + 1;

			for (CampaignactConf cc : ccs) {
				if (cc.type == campaignType) {
					campaignStatus.add(0);
					if (campaignBegin == -1) {
						campaignBegin = cc.key - 1;
					}
					campaignEnd = cc.key - 1;
				}
			}
		}

		for (int i = 0; i < campaignStatus.size(); ++i) {
			int s = campaignStatus.get(i);
			if (s >= 0) {
				CampaignactConf cc = ccs.get(campaignBegin + i);
				if (cc.how == 1) {
					if (cc.num <= campaign1) {
						campaignStatus.set(i, -1);
					}
					else {
						campaignStatus.set(i, campaign1);
					}
				}
				else if (cc.how == 2) {
					if (cc.num <= campaign2) {
						campaignStatus.set(i, -1);
					}
					else {
						campaignStatus.set(i, campaign2);
					}
				}
			}
		}
	}

	public void incCampaign1(int c) {
		campaign1 += c;
	}

	public void incCampaign2(int c) {
		campaign2 += c;
	}

	// 七日内登录有效
	public boolean flushOpen7(Date create) {
		int day = TimeUtil.pastDays(create);
		if (day > 6) {
			return false;
		}

		Readonly r = Readonly.getInstance();
		for (int i = 0; i <= day; ++i) {
			FirstsevenConf fc = r.findFirstSevenConf(i);
			Open7Bean ob = open7[i];
			if (ob == null) {
				ob = new Open7Bean();
				open7[i] = ob;
			}

			if (ob.day == 0) { // 未初始化
				ob.day = i + 1;
				// 根据fc task1进行构建任务
				for (int[] p : fc.task1) {
					List<Integer> t = new ArrayList<>();
					for (int ii = 1; ii < p.length; ++ii) {
						t.add(0);
					}
					ob.task1.add(t);
				}

				for (int[] p : fc.task2) {
					List<Integer> t = new ArrayList<>();
					for (int ii = 1; ii < p.length; ++ii) {
						t.add(0);
					}
					ob.task2.add(t);
				}
			}

			{ // 已经初始化则进行一次更新,看是否有新的奖励可领取
				int j = 0;
				for (int[] p : fc.task1) {
					int val = checkOpen7Task(p[0], this.id);
					List<Integer> ls = ob.task1.get(j);
					for (int ii = 1; ii < p.length; ++ii) {
						if (ls.get(ii - 1) >= 0) {
							if (val < 0) {
								if (p[ii] >= -val) {
									ls.set(ii - 1, -1);
								}
								else {
									ls.set(ii - 1, -val);
								}
							}
							else {
								if (p[ii] <= val) {
									ls.set(ii - 1, -1);
								}
								else {
									ls.set(ii - 1, val);
								}
							}
						}
					}
					++j;
				}

				j = 0;
				for (int[] p : fc.task2) {
					int val = checkOpen7Task(p[0], this.id);
					List<Integer> ls = ob.task2.get(j);
					for (int ii = 1; ii < p.length; ++ii) {
						if (ls.get(ii - 1) >= 0) {
							if (val < 0) {
								if (p[ii] >= -val) {
									ls.set(ii - 1, -1);
								}
								else {
									ls.set(ii - 1, -val);
								}
							} else {
								if (p[ii] <= val) {
									ls.set(ii - 1, -1);
								}
								else {
									ls.set(ii - 1, val);
								}
							}
						}
					}
					++j;
				}
			}
		}
		return true;
	}

	/**
	 * 刷新夺宝:
	 * <p>
	 * 夺宝从服务第一天开始,连续进行三天，从第一天开始2周为一个循环，每个循环的开始三天是可以进行
	 * 活动的，之后11天等待，进入下一个周期，前三天继续活动，循环往返
	 * <p>
	 * <p>
	 * 返回今日是本周期的第几天
	 */
	public int flushDb() {
		int today = TimeUtil.getToday();
		if (today != dbDay) {
			dbDay = today;
			dbSpend = 0;

			for (int i = 0; i < this.dbstores.size(); ++i) {
				this.dbstores.set(i, 0);
			}
			for (int i = 0; i < this.dbspends.size(); ++i) {
				this.dbspends.set(i, 0);
			}
			for (int i = 0; i < this.dbscores.size(); ++i) {
				this.dbscores.set(i, 0);
			}
			for (int i = 0; i < this.dbsingles.size(); ++i) {
				this.dbsingles.set(i, 0);
			}
			for (int i = 0; i < this.dbcharges.size(); ++i) {
				this.dbcharges.set(i, 0);
			}
		}

		Date open = LocalService.getInstance().getOpenDate();
		int days = TimeUtil.pastDays(open);
		int period = GameConfig.getInstance().getDbPeriod();
		int dr = days % period;
		int round = days - dr;
		if (round != dbLocalRound) {
			dbLocalRound = round;

			// 更新当前的回合信息，首先将上一周期的所有魂币和积分清空
			PackBean pb = DaoGame.getInstance().findPack(this.id);
			pb.delItem(夺宝魂币);
			pb.delItem(夺宝积分);
			this.dbLevel = 1;

			Readonly r = Readonly.getInstance();
			this.dbsingles.clear();
			this.dbcharges.clear();
			{
				List<DbsingleConf> dcs = r.getDbSingleConfs();
				for (int i = 0; i < dcs.size(); ++i) {
					this.dbsingles.add(0);
					this.dbcharges.add(0);
				}
			}

			this.dbscores.clear();
			{
				List<DbscoreConf> dcs = r.getDbScoreConfs();
				for (int i = 0; i < dcs.size(); ++i) {
					this.dbscores.add(0);
				}
			}
			this.dbspends.clear();
			this.dbSpend = 0;
			{
				List<DbspendConf> dcs = r.getDbSpendConfs();
				for (int i = 0; i < dcs.size(); ++i) {
					this.dbspends.add(0);
				}
			}
			this.dbstores.clear();
			{
				List<DbstoreConf> dcs = r.getDbStoreConfs();
				for (int i = 0; i < dcs.size(); ++i) {
					this.dbstores.add(0);
				}
			}
		}
		return dr;
	}

	/**
	 * 刷新夺宝打折时间
	 *
	 * @return
	 */
	public long flushDbCouple() {
		long now = System.currentTimeMillis() / 1000;
		if (now - coupleTime >= 0) {
			coupleTime = 0;
		}
		return coupleTime;
	}

	private int checkOpen7Task(int task, long _id) {
		switch (task) {
			case 1: // 当前副本通关关卡
			{
				return DaoGame.getInstance().findScene(_id).getScene();
			}
			case 2: // 战队等级
			{
				return DaoGame.getInstance().findRole(_id).getLevel();
			}
			case 3: // 必须上阵六个英雄，全部4件装备都必须装备，否则为0；
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 999;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						return 0;
					}
					boolean full = true;
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 0; j < 4; ++j) {
						if (ebs[j] == null) {
							full = false;
							break;
						}
						int l = ebs[j].getLevel();
						if (l < min) {
							min = l;
						}
					}

					if (!full) {
						return 0;
					}
				}
				return min;
			}
			case 4: // 竞技场排名
			{
				return -(LocalArenaService.getInstance().getRank(_id).getPeek() + 1);
			}
			case 5: {
				return DaoGame.getInstance().findSta(_id).getComposeTreasure();
			}
			case 6: {
				return DaoGame.getInstance().findSta(_id).getCtPurple();
			}
			case 7: {
				return DaoGame.getInstance().findSta(_id).getCtGold();
			}
			case 8: // 上阵英雄中有N件紫色以及以上品质的装备
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int count = 0;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 0; j < 4; ++j) {
						if (ebs[j] == null) {
							continue;
						}
						int l = ebs[j].getId();
						ItemConf ic = Readonly.getInstance().findItem(l);
						if (ic.quality >= 3) {
							++count;
						}
					}
				}
				return count;
			}
			case 9: // 上阵英雄中有N件橙色以及以上品质的装备
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int count = 0;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 0; j < 4; ++j) {
						if (ebs[j] == null) {
							continue;
						}
						int l = ebs[j].getId();
						ItemConf ic = Readonly.getInstance().findItem(l);
						if (ic.quality >= 4) {
							++count;
						}
					}
				}
				return count;
			}
			case 10: {
				return DaoGame.getInstance().findFireRaid(_id).getPeekStars();
			}
			case 11: // 上阵6个英雄中境界全部达到N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 999;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						return 0;
					}

					int l = hbs[i].getFate();
					if (l < min) {
						min = l;
					}
				}
				return min;
			}
			case 12: // 上阵英雄中境界最高达到N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 0;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}

					int l = hbs[i].getFate();
					if (l > min) {
						min = l;
					}
				}
				return min;
			}
			case 13: // 必须上阵六个英雄，全部4件装备都必须装备， 精炼最少N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 999;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						return 0;
					}
					boolean full = true;
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 0; j < 4; ++j) {
						if (ebs[j] == null) {
							full = false;
							break;
						}
						int l = ebs[j].getGrade();
						if (l < min) {
							min = l;
						}
					}

					if (!full) {
						return 0;
					}
				}
				return min;
			}
			case 14: // 上阵英雄，装备最高精炼
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 0;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 0; j < 4; ++j) {
						if (ebs[j] == null) {
							continue;
						}
						int l = ebs[j].getGrade();
						if (l > min) {
							min = l;
						}
					}
				}
				return min;
			}
			case 15: {
				return DaoGame.getInstance().findSta(_id).getHeroStoreFlush();
			}
			case 16: {
				return DaoGame.getInstance().findSta(_id).getHeroStoreBuy();
			}
			case 17: // 上阵英雄， 宝物穿戴最高精炼
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 0;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 4; j < 6; ++j) {
						if (ebs[j] == null) {
							continue;
						}
						int l = ebs[j].getLevel();
						if (l > min) {
							min = l;
						}
					}
				}
				return min;
			}
			case 18: // 必须上阵六个英雄，全部2件宝物都必须装备，等级最少N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 999;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						return 0;
					}
					boolean full = true;
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 4; j < 6; ++j) {
						if (ebs[j] == null) {
							full = false;
							break;
						}
						int l = ebs[j].getLevel();
						if (l < min) {
							min = l;
						}
					}

					if (!full) {
						return 0;
					}
				}
				return min;
			}
			case 19: // 上阵英雄， 穿戴的宝物数达到N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 0;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 4; j < 6; ++j) {
						if (ebs[j] == null) {
							continue;
						}
						++min;
					}
				}
				return min;
			}
			case 20: // 穿戴2件紫色或者以上品质的宝物的英雄有N个
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 0;

				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}

					boolean full = true;
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 4; j < 6; ++j) {
						if (ebs[j] == null) {
							full = false;
							break;
						}

						ItemConf ic = Readonly.getInstance().findItem(ebs[j].getId());
						if (ic.quality < 3) {
							full = false;
							break;
						}
					}
					if (full) {
						++min;
					}
				}
				return min;
			}
			case 21: // 上阵英雄最高突破到+N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 0;

				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}
					int l = hbs[i].getGrade();
					if (l > min) {
						min = l;
					}
				}
				return min;
			}
			case 22: // 上阵6名英雄最高突破到+N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 999;

				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						return 0;
					}
					int l = hbs[i].getGrade();
					if (l < min) {
						min = l;
					}
				}
				return min;
			}
			case 23: // 必须上阵六个英雄，全部2件宝物都必须装备，精炼等级最少N
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 999;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						return 0;
					}
					boolean full = true;
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 4; j < 6; ++j) {
						if (ebs[j] == null) {
							full = false;
							break;
						}
						int l = ebs[j].getGrade();
						if (l < min) {
							min = l;
						}
					}

					if (!full) {
						return 0;
					}
				}
				return min;
			}
			case 24: // 上阵英雄中，宝物最高精炼等级
			{
				HerosBean hsb = DaoGame.getInstance().findHeros(_id);
				HeroBean[] hbs = hsb.getHeros();
				int min = 0;
				for (int i = 0; i < hbs.length; ++i) {
					if (hbs[i] == null) {
						continue;
					}
					EquipBean[] ebs = hbs[i].getEquips();
					for (int j = 4; j < 6; ++j) {
						if (ebs[j] == null) {
							continue;
						}
						int l = ebs[j].getGrade();
						if (l > min) {
							min = l;
						}
					}
				}
				return min;
			}
		}
		return 0;
	}

	/**
	 * 战力请求，然后比较配置表中完成的状态
	 */
	public void flushPower(GameUser user) {
		user.calcPower(null);

		List<ScoreRewardConf> srcs = Readonly.getInstance().getScoreReward();
		int c = srcs.size() - powerStatus.size();
		for (int i = 0; i < c; ++i) {
			powerStatus.add(0);
		}

		for (int i = 0; i < srcs.size(); ++i) {
			ScoreRewardConf src = srcs.get(i);
			int s = powerStatus.get(i);
			if (s == 0 && user.getPower() >= src.key) {
				powerStatus.set(i, 1);
			}
		}
	}

	/**
	 * 单冲
	 */
	public void flushSingle() {
		int last = LocalService.getInstance().getSingleCur();
		if (lastIndex == -1 || ((last - lastIndex) >= GameConfig.getInstance().getSinglePeriod())) {
			lastIndex = last - (last % GameConfig.getInstance().getSinglePeriod());
			singleChargeAwards.clear();
			List<SinglerechargeConf> scs = Readonly.getInstance().getSingleConfs();
			int begin = LocalService.getInstance().getSingleBeginIdx();
			int end = LocalService.getInstance().getSingleEndIdx();
			for (int i = begin; i <= end; ++i) {
				SinglerechargeConf sc = scs.get(i);
				SingleStruct ss = new SingleStruct();
				ss.rmb = sc.cost * 100;
				ss.rewards = sc.reward;
				ss.limit = sc.limit;
				singleChargeAwards.add(ss);
			}
		}
	}

	/**
	 * 刷新累计充值
	 */
	public void flushCum() {
		int last = LocalService.getInstance().getCumCur();
		if (lastCumIdx == -1 || ((last - lastCumIdx) >= GameConfig.getInstance().getCumPeriod())) {
			lastCumIdx = last - (last % GameConfig.getInstance().getCumPeriod());
			cumChargeAwards.clear();
			cumContents.clear();

			List<CumrechargeConf> scs = Readonly.getInstance().getCumConfs();
			int begin = LocalService.getInstance().getCumBeginIdx();
			int end = LocalService.getInstance().getCumEndIdx();
			for (int i = begin; i <= end; ++i) {
				CumrechargeConf cc = scs.get(i);
				cumChargeAwards.add(0);
				int [] arr = new int[cc.reward.length + 1];
				arr[0] = cc.cost;
				for (int j = 0; j < cc.reward.length; ++j) {
					arr[j + 1] = cc.reward[j];
				}
				cumContents.add(arr);
			}
		}
	}

	/**
	 * 刷新 半价限购dc 和 折扣兑换ec
	 */
	public void flushDc() {
		{
			int last = LocalService.getInstance().getDcCur();
			if (lastDcIdx == -1 || ((last - lastDcIdx) >= GameConfig.getInstance().getSalePeriod())) {
				lastDcIdx = last - (last % GameConfig.getInstance().getSalePeriod());
				dcStatus.clear();
				ecStatus.clear();

				int count = LocalService.getInstance().getDcEndIdx() - LocalService.getInstance().getDcBeginIdx();
				for (int i = 0; i <= count; ++i) {
					dcStatus.add(0);
				}
				count = LocalService.getInstance().getEcEndIdx() - LocalService.getInstance().getEcBeginIdx();
				for (int i = 0; i <= count; ++i) {
					ecStatus.add(0);
				}
			}
		}
	}

	/**
	 * 刷新开服基金与全民福利
	 */
	public void flushFunds() {
		RoleBean rb = DaoGame.getInstance().findRole(this.id);
		for (int i = 0; i < fundStates.length; ++i) {
			if (fundStates[i] != 0) {
				continue;
			}

			FundConf fc = Readonly.getInstance().getFundConf(i);
			if (rb.getLevel() >= fc.reward1[0]) {
				fundStates[i] = 1;
			}
		}

		int total = LocalService.getInstance().getFundCount();
		for (int i = 0; i < fundStates2.length; ++i) {
			if (fundStates2[i] != 0) {
				continue;
			}
			FundConf fc = Readonly.getInstance().getFundConf(i);

			if (total >= fc.reward2[0]) {
				fundStates2[i] = 1;
			}
		}
	}

	public ActivityBean() {
	}

	public ActivityBean(long _id) {
		id = _id;
		for (int i = 0; i < 7; ++i) {
			open7[i] = new Open7Bean();
		}
	}

	public void setSignState(int _signState) {
		if (_signState < 10) {
			_signState = (this.signState / 10) * 10 + _signState;
		}
		else {
			_signState = (this.signState % 10) + _signState;
		}
		this.signState = _signState;
	}

	/**
	 * 检查开服基金或者全民福利是否有未领取部分
	 *
	 * @return
	 */
	public boolean checkFundsNotGet() {
		if (Arrays.stream(fundStates).anyMatch((i) -> i == 1)) {
			return true;
		}
		if (Arrays.stream(fundStates2).anyMatch((i) -> i == 1)) {
			return true;
		}
		return false;
	}


	/**
	 * 刷新登录天数，更新登录福利
	 *
	 */
	public void flushLogDays() {
		RoleBean rb = DaoGame.getInstance().findRole(this.id);
		int days = rb.getLogdays();

		// 登录天数需要在这里检查
		int today = TimeUtil.getToday();
		if (today > rb.getLogDay()) {
			++days;
			rb.setLogDay(today);
			rb.setLogdays(days);
		}

		List<ForheroConf> fcs = Readonly.getInstance().getForheroConfs();
		for (ForheroConf fc : fcs) {
			if (fc.day.length == 2 && fc.day[0] <= days && logoFuli.getOrDefault(fc.key, 0) == 0) {
				logoFuli.put(fc.key, 1);
			}
		}
	}

	public void commitHeroLevel(int _level) {
		if (this.maxLevel < _level) {
			this.maxLevel = _level;

			List<ForheroConf> fcs = Readonly.getInstance().getForheroConfs();
			for (ForheroConf fc : fcs) {
				if (fc.level[0] == maxLevel) {
					levelFuli.put(fc.key, 1);
					break;
				}
			}
		}
	}

	/**
	 * @param rmb 分
	 */
	public void putDbcharges(int rmb) {
		int day = flushDb();
		if (day > 2) {
			return;
		}

		List<DbsingleConf> dcs = Readonly.getInstance().getDbSingleConfs();
		int i = 0;
		for (DbsingleConf dc : dcs) {
			if (dc.cost == rmb / 100) {
				dbcharges.set(i, 1 + dbcharges.get(i));
			}
			++i;
		}
	}

	public void putDbspends(int gem) {
		int day = flushDb();
		if (day > 2) {
			return;
		}

		dbSpend = dbSpend + gem;
	}

}
