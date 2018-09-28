package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.tumei.common.RemoteService;
import com.tumei.common.fight.*;
import com.tumei.common.group.GroupMessage;
import com.tumei.common.group.GroupRoleMessage;
import com.tumei.common.group.GroupSceneRoleStruct;
import com.tumei.common.group.GroupSimpleStruct;
import com.tumei.common.utils.Defs;
import com.tumei.common.utils.ErrCode;
import com.tumei.common.utils.RandomUtil;
import com.tumei.common.utils.TimeUtil;
import com.tumei.common.webio.AwardStruct;
import com.tumei.common.webio.BattleResultStruct;
import com.tumei.controller.GroupService;
import com.tumei.controller.struct.*;
import com.tumei.controller.struct.notify.GroupTextNotifyStruct;
import com.tumei.modelconf.GroupConf;
import com.tumei.modelconf.GuildraidConf;
import com.tumei.modelconf.Readonly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static com.tumei.common.utils.Defs.*;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Groups")
public class GroupBean {
	static final Log log = LogFactory.getLog(GroupBean.class);

	@JsonIgnore
	@Id
	private String objectId;
	@Field("id")
	private Long id;

	private String name = "";

	private int icon;

	private boolean dirty;

	/**
	 * 创建时间
	 */
	private Date create;

	/**
	 * 公会所在服务器
	 */
	private int zone;

	/**
	 * 今日贡献总数值
	 */
	private int progress;

	/**
	 * 加入公会的方案
	 * 0: 自动加入
	 * 1: 需要审批
	 * 2: 拒绝加入
	 */
	private int approval;

	/**
	 * 军团贡献
	 */
	private int contrib;
	/**
	 * 军团等级
	 */
	private int level = 1;
	/**
	 * 军团经验
	 */
	private int exp;

	/**
	 * 总军团经验
	 */
	private int allExp;

	/**
	 * 军团描述
	 */
	private String desc = "";

	/**
	 * 内部公告
	 */
	private String notify = "";

	private HashMap<Long, GroupRole> roles = new HashMap<>();

	/**
	 * 待审批成员
	 */
	private List<GroupPreRole> preRoles = new ArrayList<>();

	/**
	 * 按照服务器分块的成员列表, 不会保存到数据库
	 */
	private HashMap<Integer, List<GroupRole>> zoneRoles = new HashMap<>();

	/**
	 * 需要广播全体的消息
	 */
	private List<String> messages = new ArrayList<>();

	private int flushDay;

	private GroupScene scene = new GroupScene();

	/**
	 * 消息记录,登录公会的时候返回以前的100条老消息
	 */
	private List<String> notifys = new ArrayList<>();

	public GroupBean() {
		arrangeZoneRoles();
	}

	/**
	 * 搜寻是否有需要广播的信息
	 */
	public synchronized void update() {
		if (messages.size() > 0) {
			String text = String.join("\n", messages);
			messages.clear();
			if (Strings.isNullOrEmpty(text)) {
				return;
			}

			GroupTextNotifyStruct n = new GroupTextNotifyStruct(text);

			try {
				zoneRoles.forEach((server, grs) -> {
					n.addUsers(grs);
					RemoteService.getInstance().notifyGroup(server, n);
				});
			} catch (Exception ex) {

			}
		}
	}

	public synchronized GroupScene getScene() {
		return scene;
	}

	public void setScene(GroupScene scene) {
		this.scene = scene;
	}

	public int getFlushDay() {
		return flushDay;
	}

	public void setFlushDay(int flushDay) {
		this.flushDay = flushDay;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getApproval() {
		return approval;
	}

	public void setApproval(int approval) {
		this.approval = approval;
	}

	public HashMap<Long, GroupRole> getRoles() {
		return roles;
	}

	public void setRoles(HashMap<Long, GroupRole> roles) {
		this.roles = roles;
	}

	public synchronized int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getContrib() {
		return contrib;
	}

	public void setContrib(int contrib) {
		this.contrib = contrib;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public Date getCreate() {
		return create;
	}

	public void setCreate(Date create) {
		this.create = create;
	}

	public List<GroupPreRole> getPreRoles() {
		return preRoles;
	}

	public void setPreRoles(List<GroupPreRole> preRoles) {
		this.preRoles = preRoles;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getNotify() {
		return notify;
	}

	public void setNotify(String notify) {
		this.notify = notify;
	}

	public List<String> getNotifys() {
		return notifys;
	}

	public void setNotifys(List<String> notifys) {
		this.notifys = notifys;
	}

	public static GroupBean create(long id) {
		GroupBean gb = new GroupBean();
		gb.id = id;
		gb.zone = (int) (id % 1000);
		gb.create = new Date();
		return gb;
	}


	/*********** 需要同步的操作 ***************/

	/**
	 * 增加公会经验
	 *
	 * @param _exp
	 */
	public synchronized void addExp(int _exp) {
		exp += _exp;
		allExp += _exp;
		GroupService.getInstance().submitExpRank(id, allExp);

		while (true) {
			GroupConf gc = Readonly.getInstance().findGroup(level);
			if (gc == null || gc.exp == 0 || exp < gc.exp) {
				break;
			}
			else {
				exp -= gc.exp;
				++level;
				report(TimeUtil.nowString() + "|在全体成员的努力下，公会达到" + Defs.getColorString(5, level+"") + "级");
			}
		}
	}

	public synchronized String join(GroupRoleMessage grm) {
		GroupConf gc = Readonly.getInstance().findGroup(level);
		if (roles.size() >= gc.num) {
			return "公会成员已满";
		}

		if (approval == 2 && roles.size() > 0) {
			return "拒绝加入";
		}
		else if (approval == 1 && roles.size() > 0) {
			if (preRoles.size() >= 15) {
				return "申请人数过多";
			}

			// 删除重复的
			preRoles.removeIf((_gpr) -> _gpr.id == grm.id);

			GroupPreRole gpr = new GroupPreRole(grm);
			preRoles.add(gpr);

			return "等待审批";
		}
		else {
			if (roles.values().stream().filter((rr) -> {
				return (rr.id == grm.id);
			}).count() > 0) {
				return "已经加入该公会";
			}

			long other = GroupService.getInstance().tryGroup(grm.id, id);
			if (other != 0) {
				return "other:" + other;
			}

			GroupRole gr = new GroupRole(grm);
			if (roles.size() == 0) {
				gr.setLord();
			}
			gr.last = LocalDateTime.now();
			roles.put(gr.id, gr);
			List<GroupRole> tmp = zoneRoles.getOrDefault(getZone(gr.id), null);
			if (tmp == null) {
				tmp = new ArrayList<>();
				zoneRoles.put(getZone(gr.id), tmp);
			}
			tmp.add(gr);
			report(TimeUtil.nowString() + "|玩家" + Defs.getColorString(5, gr.name) + "加入公会.");
		}

		return "";
	}

	public synchronized String approveJoin(GroupPreRole gpr, GroupRole leader) {
		GroupConf gc = Readonly.getInstance().findGroup(level);
		if (roles.size() >= gc.num) {
			return "公会成员已满";
		}

		long other = GroupService.getInstance().tryGroup(gpr.id, id);
		if (other != 0) {
			return "玩家已经加入其它公会";
		}

		GroupRole gr = new GroupRole(gpr);
		gr.last = LocalDateTime.now();
		roles.put(gr.id, gr);
		List<GroupRole> tmp = zoneRoles.getOrDefault(getZone(gr.id), null);
		if (tmp == null) {
			tmp = new ArrayList<>();
			zoneRoles.put(getZone(gr.id), tmp);
		}
		tmp.add(gr);

		report(TimeUtil.nowString() + "|玩家" + Defs.getColorString(1, gr.name) + "被官员" + Defs.getColorString(5, leader.name) + "批准加入公会.");
		return "";
	}

	private int getZone(long id) {
		return (int) (id % 1000);
	}

	/**
	 * 根据roles里的数据，按照角色所在服务器进行整理
	 */
	protected void arrangeZoneRoles() {
		zoneRoles.clear();
		for (GroupRole gr : roles.values()) {
			long id = gr.id;
			List<GroupRole> tmp = zoneRoles.getOrDefault(getZone(id), null);
			if (tmp == null) {
				tmp = new ArrayList<>();
				zoneRoles.put(getZone(id), tmp);
			}
			tmp.add(gr);
		}
	}

	/**
	 * 删除成员的缓存
	 *
	 * @param id
	 */
	private void remove(long id) {
		roles.remove(id);

		List<GroupRole> tmp = zoneRoles.getOrDefault(getZone(id), null);
		if (tmp != null) {
			tmp.removeIf((r) -> {
				return (r.id == id);
			});
		}

		GroupService.getInstance().leaveGroup(id, getId());
	}

	public synchronized String leave(long id) {
		GroupRole role = roles.getOrDefault(id, null);
		if (role != null) {
			if (roles.size() > 1) {
				// 会长离开公会，副会长继承,没有副会长不准离开
				if (role.isLord()) {
					Optional<GroupRole> opt = roles.values().stream().filter((r) -> {
						return r.isVp();
					}).findFirst();

					if (!opt.isPresent()) {
						return "至少有一名副会长,才能离开公会";
					}
					else {
						// 设定为会长
						opt.get().setLord();
					}
				}
			}

			remove(id);
			report(TimeUtil.nowString() + "|公会成员" + Defs.getColorString(5, role.name) + "离开公会.");
		}
		return "";
	}

	/**
	 * 根据公会当前信息，返回公会传递数据的结构
	 *
	 * @return
	 */
	public synchronized GroupMessage createBody() {
		GroupMessage gm = new GroupMessage();

		gm.gid = getId();
		gm.name = getName();
		gm.icon = getIcon();
		gm.zone = getZone();
		if (create != null) {
			gm.create = create.getTime();
		}
		gm.desc = getDesc();
		gm.notify = getNotify();
		gm.contrib = getContrib();
		gm.approval = getApproval();
		gm.level = getLevel();
		gm.exp = getExp();

		getRoles().values().stream().forEach((role) -> {
			gm.roles.add(role.createGroupRoleMessage());
		});
		getPreRoles().stream().forEach((role) -> {
			gm.pres.add(role.createGroupRoleMessage());
		});

		return gm;
	}

	/**
	 * 查找公会，公会推荐的时候返回的结构
	 *
	 * @return
	 */
	public synchronized GroupSimpleStruct createSimpleBody() {
		GroupSimpleStruct gss = new GroupSimpleStruct();

		gss.gid = getId();
		gss.name = getName();
		gss.icon = getIcon();
		gss.count = getRoles().size();
		gss.level = getLevel();
		gss.desc = getDesc();
		gss.zone = getZone();

		Optional<GroupRole> opt = getRoles().values().stream().filter((r) -> {
			return r.isLord();
		}).findFirst();

		opt.ifPresent((r) -> {
			gss.lord = r.name;
			gss.lordlvl = r.level;
		});

		return gss;
	}

	public synchronized String getLeaderName() {
		Optional<GroupRole> opt = roles.values().stream().filter((r) -> {
			return r.isLord();
		}).findFirst();

		if (opt.isPresent()) {
			return opt.get().name;
		}
		return "";
	}

	public synchronized boolean isDirty() {
		return dirty;
	}

	public synchronized void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public synchronized void save(Consumer<GroupBean> func) {
		if (dirty) {
			dirty = false;
			func.accept(this);
		}
	}

	/**
	 * 修改公会加入的方式
	 *
	 * @param role 操作者
	 * @param mode 方案
	 * @return
	 */
	public synchronized String modifyApproval(long role, int mode) {
		if (mode < 0 || mode > 2) {
			return ErrCode.未知参数.name();
		}

		GroupRole gr = roles.getOrDefault(role, null);
		if (gr == null || !gr.isLord()) {
			return "没有权限";
		}

		approval = mode;
		report(TimeUtil.nowString() + "|管理员" + Defs.getColorString(5, gr.name) + "修改公会审批方式.");
		return "";
	}

	/**
	 * 修改公会公告
	 *
	 * @param role
	 * @param desc
	 * @return
	 */
	public synchronized String modifyDesc(long role, String desc) {
		if (desc == null || desc.length() > 255) {
			return "公告内容非法";
		}

		GroupRole gr = roles.getOrDefault(role, null);
		if (gr == null || !gr.isLord()) {
			return "没有权限";
		}

		this.desc = desc;
		report(TimeUtil.nowString() + "|管理员" + Defs.getColorString(5, gr.name) + "修改宣言");

		return "";
	}

	public synchronized String modifyNotify(long role, String desc) {
		if (desc == null || desc.length() > 255) {
			return "公告内容非法";
		}

		GroupRole gr = roles.getOrDefault(role, null);
		if (gr == null || !gr.isLord()) {
			return "没有权限";
		}

		this.notify = desc;
		report(TimeUtil.nowString() + "|管理员" + Defs.getColorString(5, gr.name) + "修改通知");

		return "";
	}

	/***
	 * 报告发生的事件给所有成员所在的服务器
	 */
	public synchronized void report(String text) {
		messages.add(text);
		while (notifys.size() > 200) {
			notifys.remove(0);
		}
		notifys.add(text);
	}

	/**
	 * 报告玩家通过审批，加入公会
	 *
	 * @param role
	 */
	public synchronized void reportApproval(long role) {
		RemoteService.getInstance().notifyApproval((int) (role % 1000), this.id, role);
	}

	/**
	 * 修改成员的权限级别
	 *
	 * @param role   操作者
	 * @param target 被操作者
	 * @param mode   1:提升  2:降低
	 * @return
	 */
	public synchronized String modify(long role, long target, int mode) {
		if (mode < 1 || mode > 2) {
			return ErrCode.未知参数.name();
		}

		GroupRole gr = roles.getOrDefault(role, null);
		GroupRole tr = roles.getOrDefault(target, null);
		if (gr == null || tr == null || role == target || !gr.isLord()) {
			return "没有权限";
		}

		if (mode == 1) { // 提升
			if (tr.isVp()) { // 副会长提升的时候，会长需要下降
				gr.setVp();
				tr.setLord();
			}
			else {
				// 检查副团长的个数
				if (roles.values().stream().filter((r) -> {
					return r.isVp();
				}).count() >= 4) {
					return "副会长个数已达上限";
				}
				tr.setVp();
			}
		}
		else {
			tr.setNormal();
		}

		return "";
	}

	/**
	 * 审批成员
	 *
	 * @param role   审批人
	 * @param target 被审批人
	 * @param mode   1:同意 2:拒绝
	 * @return
	 */
	public synchronized String approve(long role, long target, int mode) {
		if (mode < 1 || mode > 2) {
			return ErrCode.未知参数.name();
		}

		GroupRole gr = roles.getOrDefault(role, null);
		Optional<GroupPreRole> opt = preRoles.stream().filter((r) -> {
			return (r.id == target);
		}).findFirst();

		if (!opt.isPresent()) {
			return "审批对象不存在";
		}

		GroupPreRole tr = opt.get();
		if (gr == null || role == target || !gr.isVpAbove()) {
			return "没有权限";
		}

		if (roles.values().stream().anyMatch((r) -> {
			return (r.id == target);
		})) {
			// 审批对象已经存在成员列表中，直接和拒绝流程一样，江这次审批记录删除即刻。
			mode = 2;
		}

		if (mode == 1) { // 同意
			String rtn = approveJoin(tr, gr); // 审批成功才删除这个申请
			preRoles.remove(tr);
			if (Strings.isNullOrEmpty(rtn)) {
				reportApproval(tr.id);
			}
			return rtn;
		}
		else { // 拒绝
			preRoles.remove(tr);
		}

		return "";
	}

	/**
	 * @param role
	 * @param target
	 * @return
	 */
	public synchronized String kick(long role, long target) {
		GroupRole gr = roles.getOrDefault(role, null);
		GroupRole tr = roles.getOrDefault(target, null);
		if (gr == null || role == target || !gr.isVpAbove()) {
			return "没有权限";
		} else if (tr == null) {
			return "操作对象不在公会中";
		}

		if (tr.isVpAbove()) {
			return "官员不能被踢";
		}

		remove(target);

		report(TimeUtil.nowString() + "|公会成员" + Defs.getColorString(1, tr.name) + "被官员" + Defs.getColorString(5, gr.name) + "踢出公会.");
		return "";
	}

	/**
	 * 弹劾团长
	 * <p>
	 * 1. 副团长或者贡献排名前5个成员才能进行弹劾
	 * 2. 团长最近登录时间是在五天前
	 * 3. 弹劾成功后，立刻成为新的团长
	 *
	 * @param role
	 * @return
	 */
	public synchronized String impeach(long role) {
		GroupRole gr = roles.getOrDefault(role, null);
		if (gr == null) {
			return "成员不存在";
		}

		if (gr.isLord()) {
			return "会长不能弹劾";
		}

		if (!gr.isVp()) {
			// 在非副会长的情况下，检查是否贡献为前5
			List<GroupRole> tmp = new ArrayList<>(roles.values());
			Collections.sort(tmp, (o1, o2) -> {
				if (o1.cb > o2.cb) {
					return -1;
				}
				else if (o1.cb < o2.cb) {
					return 1;
				}
				return 0;
			});
			int n = 5;
			if (n > tmp.size()) {
				n = tmp.size();
			}
			boolean found = false;
			for (int i = 0; i < 5; ++i) {
				if (tmp.get(i).id == role) {
					found = true;
					break;
				}
			}
			if (!found) {
				return "只有副会长或者历史贡献排名前五的成员才能弹劾离线五天以上的会长";
			}
		}

		// 检查团长是否离线超过五天
		GroupRole leader = roles.values().stream().filter((r) -> {
			return r.isLord();
		}).findFirst().get();

		// 当前时间减去5天，如果在会长上次登录时间之后，表示会长超过5天未登录, 否则不能弹劾
		if (!LocalDateTime.now().minusDays(5).isAfter(leader.last)) {
			return "会长5天内登录过，不可被弹劾";
		}

		// 弹劾会长，提升自己
		leader.setNormal();
		gr.setLord();
		report(TimeUtil.nowString() + "|会长" + Defs.getColorString(1, leader.name) + "被玩家" + Defs.getColorString(5, gr.name) + "成功弹劾.");

		return "";
	}

	public synchronized boolean logon(GroupRoleMessage grm) {
		GroupRole gr = roles.getOrDefault(grm.id, null);
		if (gr == null) {
			return false;
		}

		gr.logon(grm);
		return true;
	}

	/**
	 * 贡献
	 *
	 * @param role
	 * @param pg
	 * @param _exp
	 * @param cb
	 * @return
	 */
	public synchronized String donate(long role, int pg, int _exp, int cb) {
		GroupRole gr = roles.get(role);
		if (gr == null) {
			return "玩家已经不在该公会中";
		}

		flush();

		gr.cbs += cb;
		gr.cb += cb;

		addExp(_exp);

		progress += pg;
		String mode = String.format(绿色字段, "普通捐献");
		if (pg >= 5) {
			mode = String.format(紫色字段, "高级捐献");
		} else if (pg >= 3){
			mode = String.format(蓝色字段, "中级捐献");
		}

		report("1|" + TimeUtil.nowString() + "|" + Defs.getColorString(5, gr.name) + ":进行了一次" + mode + ".");
		return "";
	}


	/**
	 * 刷新成员的当日贡献和公会进度
	 */
	public synchronized void flush() {
		int today = TimeUtil.getToday();
		if (today != flushDay) {
			flushDay = today;

			progress = 0;
			roles.forEach((rid, gr) -> gr.flush());

			// 如果当前副本章节未通过，则恢复所有战斗力，否则建立下一关的战斗力
			scene.reset();

			GroupService.getInstance().submitSceneRank(id, scene.scene);
			dirty = true;
		}
	}

	public synchronized void flushToScene(int sc) {
		progress = 0;
		roles.forEach((rid, gr) -> gr.flush());

		// 如果当前副本章节未通过，则恢复所有战斗力，否则建立下一关的战斗力
		scene.scene = sc;
		scene.reset();

		GroupService.getInstance().submitSceneRank(id, scene.scene);
		dirty = true;
	}

	/**
	 * 是否可以进行公会副本战斗
	 * @return
	 */
	public synchronized boolean canFight() {
		// 1. 判断时间 早上10点到晚上10点
		if (LocalDateTime.now().getHour() < 10) {
			return false;
		}

		return true;
	}

	// 获取指定副本关卡，指定阵营的奖励
	public synchronized List<AwardStruct> getSceneAwards(int idx) {
		List<AwardStruct> rtn = new ArrayList<>();
		scene.awards.get(idx).forEach(as -> {
			rtn.add(new AwardStruct(as.id, as.count));
		});
		return rtn;
	}

	public synchronized AwardStruct randomSceneAwards(int idx) {
		List<AwardStruct> awards = scene.awards.get(idx);

		int find = (RandomUtil.getRandom() % awards.size());
		return awards.remove(find);
	}

	/**
	 * 公会请求战斗
	 *
	 * @param bs
	 * @param index
	 * @param rl
	 */
	public synchronized void callFight(GroupRole role, HerosStruct bs, int index, BattleResultStruct rl) {
		List<DirectHeroStruct> peers = scene.peers.get(index - 1);

		// 如果对手全部死亡了，直接返回结束
		if (peers.stream().noneMatch((dhs) -> dhs.life > 0)) {
			rl.result = "关卡Boss已经被其他成员击杀";
		} else {
			SceneFightStruct arg = new SceneFightStruct();
			arg.hss = bs;
			arg.right = peers;

			GroupSceneRoleStruct gsrs = scene.roles.get(role.id);
			if (gsrs == null) {
				gsrs = new GroupSceneRoleStruct();
				gsrs.name = role.name;
				gsrs.icon = role.icon;
				scene.roles.put(role.id, gsrs);
			}

			GuildraidConf grc = Readonly.getInstance().findGuildraid(scene.scene);

			rl.rCon = RandomUtil.getBetween(grc.reward1[0], grc.reward1[1]);

			try {
				FightResult r = GroupService.getInstance().callRemote(RemoteService::callFight, arg);

				if (r == null) {
					rl.result = "战斗服务器维护中";
				} else {
					++gsrs.count;
					rl.data = r.data;
					if (r.win < 1) {
						rl.result = "战斗出错";
					} else {// 胜利，就是击杀
						if (r.win == 1) {
							rl.kill = 1;
						}

						long harm = 0;
						long total = 0;
						for (int j = 0; j < peers.size(); ++j) {
							DirectHeroStruct shs = peers.get(j);
							if (shs != null) {
								long l = r.lifes.get(j);
//								log.info("英雄(" + shs.hero + ") life:" + shs.life + "  现在:" + l);
								harm = harm + (shs.life - l);
								shs.life = l;
								total += shs.life;
							}
						}
						if (gsrs.harm < harm) {
							gsrs.harm = harm;
						}
						rl.harm = harm;

						int rt = (int)(100.0f - (total * 100f) / scene.totals.get(index - 1));
						if (rt < 0) {
							rt = 0;
						} else if (rt > 100) {
							rt = 100;
							rl.kill = 1; // 如果扣除都血量满足条件,也算胜利
						}
						log.info(this.id + ": 玩家(" + role.name + ") 战斗，血量为:" + total + "，最大血量：" + scene.totals.get(index - 1) + " 进度:" + rt);

						scene.progress[index-1] = rt;

						if (rl.kill == 1) { // 击杀 奖励判定
							if (scene.firstKill[index-1] == 0) {
								scene.firstKill[index-1] = 1;
								rl.kill = 2;
								log.info(this.id + ": 玩家(" + role.name + ") 首次击杀boss(" + index + ") 获得经验:" + grc.reward5 + " 原来等级经验:" + this.level + "," + this.exp);
								this.addExp(grc.reward5);
							} else {
								log.info(this.id + ": 玩家(" + role.name + ") 非首次击杀boss(" + index + ") 不能获得经验:" + grc.reward5);
							}
							role.cb += rl.rCon + grc.reward2;
							role.cbs += rl.rCon + grc.reward2;
							this.contrib += grc.reward2 + rl.rCon;

							log.info(this.id + ": 玩家(" + role.name + ") 获得贡献:" + (rl.rCon + grc.reward2));

							report("2|" + TimeUtil.nowString() + "|" + scene.scene + "|" + index + "|" + Defs.getColorString(5, role.name));
						} else {
							role.cb += rl.rCon;
							role.cbs += rl.rCon;
							this.contrib += rl.rCon;
							log.info(this.id + ": 玩家(" + role.name + ") 获得贡献:" + rl.rCon);
						}

						this.dirty = true;
					}
				}
			} catch (Exception ex) {
				log.error("错误原因:" + ex.getMessage());
				ex.printStackTrace();
				rl.result = "战斗出错";
			}
		}
	}
}
