package com.tumei.model.beans;

import com.tumei.common.Readonly;
import com.tumei.common.fight.ArtifactStruct;
import com.tumei.modelconf.ArtifactConf;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/3/3 0003.
 *
 * 神器信息
 */
public class ArtifactBean {
	/**
	 * 神器id
	 */
	private int id;
	/**
	 * 等级, 0标识未激活, 激活后:[1,...] 标识强化的等级
	 */
	private int level = 0;

	/**
	 * 神器对应的部件状态
	 */
	private HashMap<Integer, ArtifactComBean> coms = new HashMap<>();

	public ArtifactBean() {}

	public ArtifactBean(int _id) { id = _id; }

	/**
	 * 创建战斗需要的结构
	 * @return
	 */
	public ArtifactStruct createStruct() {
		ArtifactStruct as = new ArtifactStruct();
		as.id = this.id;
		as.level = this.level;
		this.coms.forEach((k, v) -> {
			as.coms.add(v.createStruct(k));
		});

		return as;
	}

	/**
	 * 装备神器组件
	 * @param comid
	 * @return
	 */
	public boolean equipCom(int comid) {
		if (coms.containsKey(comid)) {
			return false;
		}
		coms.put(comid, new ArtifactComBean());

		return true;
	}

	/**
	 * 激活神器
	 * @return
	 */
	public boolean promote() {
		if (level > 0) {
			return false;
		}

		boolean ok = true;
		ArtifactConf ac = Readonly.getInstance().findArtifact(this.id);
		if (ac != null) {
			for (int part : ac.parts) {
				if (!coms.containsKey(part)) {
					ok = false;
					break;
				}
			}
		}
		if (ok) {
			level = 1;
		}

		return ok;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public HashMap<Integer, ArtifactComBean> getComs() {
		return coms;
	}

	public void setComs(HashMap<Integer, ArtifactComBean> coms) {
		this.coms = coms;
	}

	@Override
	public String toString() {
		return "ArtifactBean{" + "id=" + id + ", level=" + level + ", coms=" + coms + '}';
	}
}
