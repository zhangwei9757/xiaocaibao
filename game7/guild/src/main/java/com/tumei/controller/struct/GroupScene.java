package com.tumei.controller.struct;

import com.tumei.dto.battle.DirectHeroStruct;
import com.tumei.common.group.GroupSceneRoleStruct;
import com.tumei.common.webio.AwardStruct;
import com.tumei.modelconf.GuildraidConf;
import com.tumei.common.Readonly;

import java.util.*;

/**
 * Created by Leon on 2017/5/23 0023.
 */
public class GroupScene {
	/**
	 * 当前公会副本的进度
	 */
	public int scene = 1;

	/**
	 * 血量进度
	 */
	public int[] progress = new int[4];

	/**
	 * 是否首杀
	 */
	public int[] firstKill = new int[4];

	/**
	 * 今日攻打次数与最高伤害列表
	 */
	public Map<Long, GroupSceneRoleStruct> roles = new HashMap<>();

	/**
	 * 公会副本当前章节的奖励
	 * <p>
	 * 最外层一共4个，对应的四个关卡
	 * <p>
	 * 每个关卡对应一个列表，包含了各种奖励个数，每次都随机从这些奖励个数中读取一个
	 */
	public List<List<AwardStruct>> awards = new ArrayList<>();

	/**
	 * 4个关卡的对手信息，因为一天内，这些对手的数值是递减的
	 */
	public List<List<DirectHeroStruct>> peers = new ArrayList<>();

	/**
	 * 每关对应的总血量
	 */
	public List<Long> totals = new ArrayList<>();

	public GroupScene() {
	}

	/**
	 * 重置当前的关卡, 如果进度到100，则推进一关，否则重复当前关卡的战斗
	 */
	public void reset() {
		if (Arrays.stream(progress).allMatch((am) -> am >= 100)) {
			// 前进一个关卡, 首杀和进度都要重置;
			++scene;
			for (int i = 0; i < 4; ++i) {
				firstKill[i] = 0;
			}
		}

		for (int i = 0; i < 4; ++i) {
			progress[i] = 0;
		}

		awards.clear();
		totals.clear();
		roles.clear();
		peers.clear();
		GuildraidConf rc = Readonly.getInstance().findGuildraid(scene);
		if (rc != null) {
			// 1. 根据关卡创建四个阵营的敌人
			for (int i = 1; i < 5; ++i) {
				long total = 0;
				List<DirectHeroStruct> right = new ArrayList<>();
				int[] defend = new int[0];
				switch (i) {
					case 1:
						defend = rc.sect1;
						break;
					case 2:
						defend = rc.sect2;
						break;
					case 3:
						defend = rc.sect3;
						break;
					case 4:
						defend = rc.sect4;
						break;
				}

				for (int h : defend) {
					if (h != 0) {
						int ratio = 100;
						DirectHeroStruct shs = new DirectHeroStruct();
						shs.hero = h;
						shs.life = (long)(rc.hp * ratio / 100.0);
						shs.attack = (int) (rc.attack * ratio / 100.0);
						shs.def = (int) (rc.defence1 * ratio / 100.0);
						shs.mdef = (int) (rc.defence2 * ratio / 100.0);
						shs.critical = (int) (rc.crit * ratio / 100.0);
						shs.aim = (int) (rc.hit * ratio / 100.0);
						shs.antiCrit = (int) (rc.critoff * ratio / 100.0);
						shs.dodge = (int) (rc.dog * ratio / 100.0);
						shs.enHarm = (int) (rc.increase * ratio / 100.0);
						shs.overHarm = (int) (rc.reduce * ratio / 100.0);
						right.add(shs);
						total += shs.life;
					}
					else {
						right.add(null);
					}
				}
				peers.add(right);
				totals.add(total);
			}

			// 2. 根据关卡创建四个阵营的奖励
			for (int i = 0; i < 4; ++i) {
				List<AwardStruct> ls = new ArrayList<>();
				int[] rewards = rc.reward4[i];
				// 1个
				{
					AwardStruct as = new AwardStruct(rewards[0], rewards[1]);
					ls.add(as);
				}

				// 2个
				for (int j = 0; j < 2; ++j) {
					AwardStruct as = new AwardStruct(rewards[0], rewards[2]);
					ls.add(as);
				}

				// 5个
				for (int j = 0; j < 3; ++j) {
					AwardStruct as = new AwardStruct(rewards[0], rewards[3]);
					ls.add(as);
				}

				// 10个
				for (int j = 0; j < 6; ++j) {
					AwardStruct as = new AwardStruct(rewards[0], rewards[4]);
					ls.add(as);
				}

				// 30个
				for (int j = 0; j < 8; ++j) {
					AwardStruct as = new AwardStruct(rewards[0], rewards[5]);
					ls.add(as);
				}

				awards.add(ls);
			}
		}
	}
}
