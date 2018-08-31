package com.tumei.model;

import com.tumei.common.fight.ArtifactStruct;
import com.tumei.common.fight.HeroStruct;
import com.tumei.dto.arena.ArenaItemDto;
import com.tumei.dto.arena.ArenaRoleDto;
import com.tumei.dto.arena.LadderSimpleDto;
import com.tumei.modelconf.Readonly;
import com.tumei.modelconf.TopRankConf;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Created by Leon on 2017/11/6 0006.
 */
@Data
@Document(collection = "ArenaRoles")
public class ArenaRoleBean {
	private long id;

	private int rank;

	private int peek;

	private String name;

	private int level;

	// 头像, 如果穿了时装则是正确的时装头像
	private int icon;

	// 领主品质
	private int grade;

	// 时装
	private int fashion;

	// 战斗力
	private long power;

	//
	private int[] lineups = new int[6];

	/**
	 * 阵形
	 */
	private List<HeroStruct> formation = new ArrayList<HeroStruct>();

	/**
	 * 各种战斗buff
	 */
	private Map<Integer, Integer> buffs = new HashMap<Integer, Integer>();

	/**
	 * 神器
	 */
	private List<ArtifactStruct> arts = new ArrayList<>();

	/***
	 * 天梯赛相关数据
	 */

	/**
	 * 默认不属于任何slot,玩家可以在进入的时候选择
	 */
	private int slot = -1;

	/**
	 * 是否选择的随机slot,可以获得更高的奖励
	 */
	private boolean randSlot = false;

	/**
	 * 默认是6号分组，备战状态
	 */
	private int group = 6;

	/**
	 * 分组内的索引
	 */
	private int gindex = 0;

	/**
	 * 上次领取随机勋章的时间
	 */
	private long rewardTime = 0;

	/**
	 * 暂存的荣誉，不在线被攻击了，也会计算一次荣誉，以免之前高级组内的荣誉被计算到低级组
	 */
	private int honor = 0;

	/**
	 * 进入某个档位的时间
	 */
	private long groupTime = 0;

	/**
	 * 被攻击视频  最多保存10个
	 */
	private List<LadderVideo> videos = new LinkedList<>();

	public ArenaRoleBean() {}

	public ArenaRoleBean(long _id, int _rank) {
		id = _id;
		rank = _rank;
		peek = _rank;
	}

	/**
	 * 使用传输的数据更新内部
	 * @param dto
	 */
	public void update(ArenaRoleDto dto) {
		name = dto.name;
		level = dto.level;
		icon = dto.icon;
		grade = dto.grade;
		fashion = dto.fashion;
		power = dto.power;
		lineups = dto.lineups;
		formation = dto.formation;
		buffs = dto.buffs;
		arts = dto.arts;
	}

	/**
	 * 创建传递对象，此时回传给玩家使用，比如buffs 和 lineups就不需要
	 *
	 * @return
	 */
	public ArenaItemDto createDto() {
		ArenaItemDto dto = new ArenaItemDto();
		dto.uid = id;
		dto.name = name;
		dto.level = level;
		dto.icon = icon;
		dto.grade = grade;
		dto.fashion = fashion;
		dto.power = power;
		dto.rank = rank;

		for (HeroStruct hs : formation) {
			if (hs != null && !hs.assist) {
				dto.formation.add(new HeroStruct(hs));
			}
		}

		return dto;
	}

	/**
	 * 刷新荣誉
	 *
	 * @param isClear 是否清理掉荣誉, 如果荣誉传递给下一个服务器了，就清理，否则继续累计
	 *
	 * 返回当前honor
	 *
	 */
	public int flushHonor(boolean isClear) {
		int rtn = 0;
		if (slot == -1 || rewardTime <= 0) {
			return rtn;
		}

		long now = System.currentTimeMillis() / 1000;
		// 计算荣誉和时间
		long diff = now - rewardTime;
		if (diff > 0) {
			int rewardCount = (int)(diff / Readonly.reward_interval + 1);

			// 将当前的honor计算出来
			TopRankConf trc = Readonly.getInstance().findTopRankConf(group + 1);
			rtn = rewardCount * trc.goods;
			if (randSlot) {
				rtn += (int)(rtn * 0.05);
			}
			// 加上之前的
			rtn += honor;

			if (isClear) {
				honor = 0;
			} else {
				honor = rtn;
			}

			// 当前时间减去剩余的一点模数
			rewardTime = now - (diff % Readonly.reward_interval) + Readonly.reward_interval;
		}

		return rtn;
	}

	public LadderSimpleDto createSimpleDto() {
		LadderSimpleDto dto = new LadderSimpleDto();
		dto.uid = this.id;
		dto.name = this.name;
		dto.fashion = this.fashion;
		dto.icon = this.icon;
		dto.grade = this.grade;
		dto.power = this.power;
		dto.time = this.groupTime;
		return dto;
	}

	// 增加视频
	public synchronized void addViedo(int zone, String name, int grade, String data, int win) {
		LadderVideo lv = new LadderVideo();
		lv.zone = zone;
		lv.name = name;
		lv.grade = grade;
		lv.data = data;
		lv.win = win;
		lv.time = System.currentTimeMillis() / 1000;

		// 将大于10个的视频删除
		while (videos.size() >= 10) {
			videos.remove(0);
		}
		videos.add(lv);
	}


}

