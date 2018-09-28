package com.tumei.model.festival;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.DaoGame;
import com.tumei.common.Readonly;
import com.tumei.common.utils.TimeUtil;
import com.tumei.model.PackBean;
import com.tumei.modelconf.festival.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 节日相关的数据
 *
 */
@Data
@Document(collection = "Role.Festival")
public class FestivalBean {
	@JsonIgnore
	@Id
	private String objectId;

	@JsonIgnore
	@Field("id")
	@Indexed(unique = true, name = "i_id")
	private Long id;

	// 货币
	private int[] coins = new int[2];

	// 用来区分不同的节日
	private int mode;

	// 用来区分是否神器活动
	private int flag;

	// 上次刷新的时间
	private int lastDay;

	private int start;

	private int end;

	// 总消费
	private int spend;

	// 总充值
	private int cum;

	// 是否领取今日的登录奖励
	private int islog;

	private int[] b1;
	private int[] b2;

	// 登陆
	private List<FestivalLogon> festLogons = new ArrayList<>();

	// 单充状态
	private List<FestivalSingle> festSingles = new ArrayList<>();

	// 消费状态
	private List<FestivalSpend> festSpends = new ArrayList<>();

	// 兑换状态
	private List<FestivalSale> festSales = new ArrayList<>();

	// 神器活动中的累计充值
	private List<FestivalCum> festCums = new ArrayList<>();

	/**
	 * 充值回掉
	 * @param rmb 分
	 */
	public void onCharge(int rmb) {
		flush();
		if (mode < 0) {
			return;
		}

		for (FestivalSingle fs : festSingles) {
			if (fs.cost == rmb / 100) {
				fs.count += 1;
			}
		}

		if (flag > 0) {
			cum += rmb / 100;
		}
	}

	public void onSpend(int gem) {
		flush();
		if (mode < 0) {
			return;
		}

		if (flag == 0) {
			spend += gem;
		}
	}

	/**
	 * 刷新检测日常
	 */
	public void flush() {
		int today = TimeUtil.getToday();
        // 查看今日是不是在某个节日开始和结束之间
        int stMode = -1;

        FestivalConf thisFc = null;
        List<FestivalConf> fcs = Readonly.getInstance().getFestivalConfs();
        for (FestivalConf fc : fcs) {
            if (today >= fc.start && today <= fc.last) { // ok

                stMode = fc.mode;
                thisFc = fc;
                break;
            }
        }

        // 如果是新的模式,标识进入了一个新的周期
        if (stMode != mode) {

            // 将背包中的上次的货币删除
            if (coins != null && coins.length > 0) {
                PackBean pb = DaoGame.getInstance().findPack(this.id);
                for (int i = 0; i < coins.length; ++i) {
                    pb.delItem(coins[i]);
                }
            }

            mode = stMode;
            spend = 0;
            cum = 0;
            festLogons.clear();
            festSingles.clear();
            festSpends.clear();
            festSales.clear();
            festCums.clear();

            if (mode != -1) { // 如果在活动中则新建活动规则
                this.start = thisFc.start;
                this.end = thisFc.last;
                this.flag = thisFc.flag;

                if (thisFc.flag == 0) {
                    //货币调整
                    coins = thisFc.fesdrop;

                    this.b1 = thisFc.bonus1;
                    this.b2 = thisFc.bonus2;

                    List<FestivalLogonConf> flcs = Readonly.getInstance().getFestivalLogonConfs();
                    int logDay = 0;
                    for (FestivalLogonConf flc : flcs) {
                        if (++logDay > thisFc.num) {
                            break;
                        }
                        if (flc.rewards.length > 0) {
                            FestivalLogon item = new FestivalLogon();
                            item.key = flc.key;
                            int[] rwd = new int[flc.rewards.length];
                            for (int i = 0; i < flc.rewards.length; i += 2) {
                                int fid = flc.rewards[i];
                                if (fid < 0) {
                                    if (fid == -1) {
                                        fid = coins[0];
                                    } else {
                                        fid = coins[1];
                                    }
                                }

                                rwd[i] = fid;
                                rwd[i + 1] = flc.rewards[i + 1];
                            }

                            item.rewards = rwd;
                            festLogons.add(item);
                        }
                    }

                    List<FestivalSingleConf> fscs = Readonly.getInstance().getFestivalSingleConfs();
                    for (FestivalSingleConf fsc : fscs) {
                        FestivalSingle item = new FestivalSingle();
                        item.cost = fsc.cost;
                        item.limit = fsc.limit;

                        int[] rwd = new int[fsc.reward.length];
                        for (int i = 0; i < fsc.reward.length; i += 2) {
                            int fid = fsc.reward[i];
                            if (fid < 0) {
                                if (fid == -1) {
                                    fid = coins[0];
                                } else {
                                    fid = coins[1];
                                }
                            }

                            rwd[i] = fid;
                            rwd[i + 1] = fsc.reward[i + 1];
                        }

                        item.reward = rwd;
                        festSingles.add(item);
                    }

                    List<FestivalSaleConf> fslcs = Readonly.getInstance().getFestivalSaleConfs();
                    for (FestivalSaleConf fsc : fslcs) {
                        FestivalSale item = new FestivalSale();

                        {
                            int[] rwd = new int[fsc.price.length];
                            for (int i = 0; i < fsc.price.length; i += 2) {
                                int fid = fsc.price[i];
                                if (fid < 0) {
                                    if (fid == -1) {
                                        fid = coins[0];
                                    } else {
                                        fid = coins[1];
                                    }
                                }

                                rwd[i] = fid;
                                rwd[i + 1] = fsc.price[i + 1];
                            }

                            item.price = rwd;
                        }
                        {
                            int[] rwd = new int[fsc.goods.length];
                            for (int i = 0; i < fsc.goods.length; i += 2) {
                                int fid = fsc.goods[i];
                                if (fid < 0) {
                                    if (fid == -1) {
                                        fid = coins[0];
                                    } else {
                                        fid = coins[1];
                                    }
                                }

                                rwd[i] = fid;
                                rwd[i + 1] = fsc.goods[i + 1];
                            }

                            item.goods = rwd;
                        }

                        item.limit = fsc.limit;
                        festSales.add(item);
                    }

                    List<FestivalSpendConf> fspcs = Readonly.getInstance().getFestivalSpendConfs();
                    int spendNum = 0;
                    for (FestivalSpendConf fsc : fspcs) {
                        if (++spendNum > thisFc.costnum) {
                            break;
                        }

                        FestivalSpend item = new FestivalSpend();
                        item.spend = fsc.spend;

                        int[] rwd = new int[fsc.reward.length];
                        for (int i = 0; i < fsc.reward.length; i += 2) {
                            int fid = fsc.reward[i];
                            if (fid < 0) {
                                if (fid == -1) {
                                    fid = coins[0];
                                } else {
                                    fid = coins[1];
                                }
                            }

                            rwd[i] = fid;
                            rwd[i + 1] = fsc.reward[i + 1];
                        }

                        item.reward = rwd;
                        festSpends.add(item);
                    }
                } else { // 神器活动
                    List<FestivalCumConf> fscs = Readonly.getInstance().getFestivalCumConfs();
                    for (FestivalCumConf fsc : fscs) {
                        FestivalCum item = new FestivalCum();
                        item.cost = fsc.cost;

                        int[] rwd = new int[fsc.reward.length];
                        for (int i = 0; i < fsc.reward.length; i += 2) {
                            rwd[i] = fsc.reward[i];
                            rwd[i + 1] = fsc.reward[i + 1];
                        }

                        item.reward = rwd;
                        festCums.add(item);
                    }
                }
            }
        }

		if (today != lastDay) { // 每天检查一次 节日活动是否发生变更
			if (mode != -1) {
				islog = 0;
			}
			lastDay = today;
		}
	}
}
