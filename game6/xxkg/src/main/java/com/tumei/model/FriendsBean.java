package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.GameConfig;
import com.tumei.common.DaoService;
import com.tumei.common.utils.TimeUtil;
import com.tumei.model.beans.FriendBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 优惠码
 *
 */
@Document(collection = "Role.Friends")
public class FriendsBean {
	@JsonIgnore
	@Id
	private String ObjectId;

	@JsonIgnore
	@Field("id")
	private Long id;

	/**
	 * 好友列表
	 */
	private HashMap<Long, FriendBean> friends = new HashMap<>();

	/**
	 * 申请列表
	 */
	private List<FriendBean> ready = new ArrayList<>();

	/**
	 * 今日申请加好友的ID，明日会清空，但是这个记录的保存可以让今日不要重复去申请加同一个好友
	 */
	private Set<Long> applys = new HashSet<>();

	/**
	 * 更新日期
	 */
	private int day;

	private int sendCount;
	private int recvCount;

	/**
	 * 刷新日期
	 */
	public synchronized void flush() {
		int today = TimeUtil.getToday();
		if (today != day) {
			friends.values().forEach(f -> {
				f.send = 0;
				f.recv = 0;
			});
			sendCount = 0;
			recvCount = 0;
			applys.clear();
			day = today;
		}
	}

	/**
	 * 指定的玩家是自己的好友，返回 1， 如果是0表示对方在你的审批列表中
	 * @param _id
	 * @return
	 */
	public synchronized int isFriendStatus(long _id) {
		if (friends.containsKey(_id)) {
			return 1;
		}

		if (applys.contains(_id)) {
			return 2;
		}

//		if (ready.stream().filter(fb -> fb.id == _id).findFirst().isPresent()) {
//			return 0;
//		}

		return 3;
	}

	/***
	 * 上线，下线的时候将我的状态通知给所有的好友,
	 *
	 * 让好友修改我保存的状态
	 */
	public void notifyAllFriends(long power, RoleBean rb) {
		List<Long> fds = new ArrayList<>();
		synchronized (this) {
			friends.values().stream().forEach(fb -> {
				fds.add(fb.id);
			});
		}

		long logTime = -1;
		if (rb.getOnline() == 0) {
			logTime = rb.getLogtimeLong();
		}

		for (long fid : fds) {
			FriendsBean fsb = DaoService.getInstance().findFriends(fid);
			fsb.update(this.id, rb.getNickname(), power, logTime, rb.getVip(), rb.getIcon());
		}
	}

	/**
	 * 对应指定的_id的好友，如果他存在好友列表，则更新他的信息
	 * @param _id
	 * @param name
	 * @param power
	 * @param logTime
	 * @param vip
	 * @param icon
	 */
	public synchronized void update(long _id, String name, long power, long logTime, int vip, int icon) {
		FriendBean fb = friends.getOrDefault(_id, null);
		if (fb != null) {
			fb.name = name;
			fb.power = power;
			fb.logTime = logTime;
			fb.vip = vip;
			fb.icon = icon;
		}
	}

	/**
	 * 好友是否已经达到上限
	 * @return
	 */
	public synchronized boolean isFull() {
		return (friends.size() >= GameConfig.getInstance().getFriend_count());
	}

	/***
	 * 发起申请增加指定玩家为好友
	 * @param _id
	 * @return
	 */
	public String applyFriend(long _id, long _power) {
		if (isFull()) {
			return "好友已达上限";
		}

		if (applys.contains(_id)) {
			return "今日已经向该玩家发送过申请";
		}

		if (friends.containsKey(_id)) {
			return "对方已经是好友";
		}

		FriendBean fb = new FriendBean(id, _power);
		fb.logTime = System.currentTimeMillis() / 1000;
		if (!DaoService.getInstance().findFriends(_id).addApplyFriend(fb)) {
			return "对方好友已达上限";
		}

		applys.add(_id);

		return "";
	}

	/**
	 * 将申请者信息填充到待审核列表中, 如果玩家好友已满，则失败。
	 * @param fb
	 * @return
	 */
	public synchronized boolean addApplyFriend(FriendBean fb) {
		if (isFull()) {
			return false;
		}
		// 删除fb对应的玩家之前申请的记录
		for (int i = 0; i < ready.size(); ++i) {
			if (ready.get(i).id == fb.id) {
				ready.remove(i);
				break;
			}
		}

		/**
		 * 如果审批列表大于指定的数量，则会去掉第一个申请
		 */
		if (ready.size() >= GameConfig.getInstance().getFriend_ready_count()) {
			ready.remove(0);
		}

		ready.add(fb);


		return true;
	}

	/**
	 * 确认好友的申请
	 *
	 * @return, 失败只能是当前的好友已经大于30个
	 *
	 *
	 * 返回成功后，需要通知修改另外一个好友
	 *
	 * 这里会2个玩家之间的交互，不能锁函数，要在内部独立锁, 否则A加B的同时B加A会导致死锁
	 */
	public String confirmFriend(long _id, long _power, int _flag) {
		FriendBean fb;
		synchronized (this) {
			if (_flag == 1) { // 拒绝，直接删除当前的申请中的玩家
				ready.removeIf((_fb) -> _fb.id == _id);
				return "";
			}

			if (isFull()) {
				return "好友已达上限";
			}

			Optional<FriendBean> opt = ready.stream().filter(f -> f.id == _id).findFirst();
			if (!opt.isPresent()) {
				return "对方未发送好友申请";
			}
			fb = opt.get();
			fb.mode = 1;
			friends.put(_id, fb);
			ready.remove(fb);
		}

		FriendBean my = new FriendBean(this.id, _power);
		if (DaoService.getInstance().findFriends(_id).notifyAddFriend(my) == 0) {
			// 删除
			removeFriend(_id, false);
			return "对方好友已达上限";
		}

		return "";
	}

	/***
	 * 玩家审核同意了你的申请，则需要江审核的玩家加入到你自己的好友列表
	 * @param fb 同意申请的玩家必定在线
	 * @return
	 *
	 * 返回申请者当前的状态， 0表示不能增加好友 -1表示当前在线， >0表示上次时间
	 *
	 */
	public synchronized long notifyAddFriend(FriendBean fb) {
		RoleBean rb = DaoService.getInstance().findRole(id);
		long rtn;
		if (rb.getOnline() == 0) {
			rtn = rb.getLogtimeLong();
		} else {
			rtn = -1;
		}

		if (isFull()) {
			return 0;
		}

		fb.mode = 1;
		friends.put(fb.id, fb);
		ready.removeIf((_fb) -> _fb.id == fb.id);

		return rtn;
	}

	public FriendsBean() {}

	public FriendsBean(long _id) {
		id = _id;
	}

	/***
	 * 主动删除好友
	 * @param _id
	 * @param _relevant 是否删除关联的好友中的自己
	 */
	public void removeFriend(long _id, boolean _relevant) {
		FriendBean fb;
		synchronized (this) {
			fb = friends.remove(_id);
		}

		if (_relevant && fb != null) {
			FriendsBean fsb = DaoService.getInstance().findFriends(_id);
			fsb.removeFriend(id, false);
		}
	}

	/**
	 * 发送花朵
	 * @param _id
	 * @return
	 */
	public boolean send(long _id) {
		synchronized (this) {
			FriendBean fb = friends.getOrDefault(_id, null);
			if (fb.send != 0 || sendCount >= GameConfig.getInstance().getFriend_send()) {
				return false;
			}
			fb.send = 1;
			sendCount++;
		}

		FriendsBean fsb = DaoService.getInstance().findFriends(_id);
		fsb.recv(id);
		return true;
	}

	/**
	 * 接受花朵, 是由赠送的时候激活的函数，不会主动调用
	 * @param _id
	 */
	public synchronized boolean recv(long _id) {
		FriendBean fb = friends.getOrDefault(_id, null);
		if (fb != null && fb.recv == 0) {
			fb.recv = 1;
			return true;
		}
		return false;
	}

	/***
	 * 玩家在界面上看到其他好友赠送的活力，使用该函数进行领取
	 * @param _id
	 * @return
	 */
	public synchronized boolean getAward(long _id) {
		FriendBean fb = friends.getOrDefault(_id, null);
		if (fb != null && fb.recv == 1 && recvCount < GameConfig.getInstance().getFriend_recv()) {
			recvCount++;
			fb.recv = 2;
			return true;
		}
		return false;
	}

	public synchronized void copyFriends(List<FriendBean> fbs) {
		friends.values().stream().forEach(fb -> fbs.add(fb));
	}

	public synchronized void copyReady(List<FriendBean> fbs) {
		ready.stream().forEach(fb -> fbs.add(fb));
	}


	public String getObjectId() {
		return ObjectId;
	}

	public void setObjectId(String objectId) {
		this.ObjectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public HashMap<Long, FriendBean> getFriends() {
		return friends;
	}

	public void setFriends(HashMap<Long, FriendBean> friends) {
		this.friends = friends;
	}

	public List<FriendBean> getReady() {
		return ready;
	}

	public void setReady(List<FriendBean> ready) {
		this.ready = ready;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public int getRecvCount() {
		return recvCount;
	}

	public void setRecvCount(int recvCount) {
		this.recvCount = recvCount;
	}

	public Set<Long> getApplys() {
		return applys;
	}

	public void setApplys(Set<Long> applys) {
		this.applys = applys;
	}
}


