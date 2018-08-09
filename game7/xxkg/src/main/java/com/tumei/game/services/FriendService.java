package com.tumei.game.services;

import com.tumei.common.utils.RandomUtil;
import com.tumei.model.RoleBean;
import com.tumei.model.beans.FriendBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2017/4/11 0011.
 * <p>
 * 好友服务
 *
 */
@Service
public class FriendService {

	private static FriendService sInstance;

	public static FriendService getInstance() {
		return sInstance;
	}

	private Log log = LogFactory.getLog(FriendService.class);

	/**
	 * 最近登录的好友
	 */
	private LinkedList<FriendBean> friends = new LinkedList<>();

	@PostConstruct
	void init() {
		sInstance = this;
	}

	@PreDestroy
	void dispose() {
	}

	/**
	 * 每个玩家登录的时候都要记录一下，便于推荐好友
	 *
	 * @param id
	 * @param power
	 * @param rb
	 */
	public synchronized void addRecommandFriend(long id, long power, RoleBean rb) {
		FriendBean fb = new FriendBean(id, power, rb);

		fb.logTime = (System.currentTimeMillis() / 1000);
		// 删除之前已经存在的好友
		friends.removeIf((_fb) -> _fb.id == id);
		friends.add(fb);

		// 可推荐好友数量大于300个，需要将推荐好友缩小300个。
		if (friends.size() >= 300) {
			for (int i = 0; i < 50; ++i) {
				friends.remove(i);
			}
		}
	}

	/**
	 * 返回随机的N个好友
	 * @param N
	 * @param uid 不能包含的玩家
	 * @return
	 */
	public synchronized List<FriendBean> randomFriends(int N, long uid) {
		List<FriendBean> rtn = new ArrayList<>();
		if (N >= friends.size()) {
			friends.forEach(fb -> {
				if (fb.id != uid) {
					rtn.add(new FriendBean(fb));
				}
			});
		} else {
			int idx = RandomUtil.getRandom() % friends.size();
			for (int i = idx; i < friends.size(); ++i) {
				FriendBean _fb = friends.get(i);
				if (_fb.id == uid) {
					continue;
				}
				rtn.add(_fb);
				--N;
				if (N <= 0) {
					return rtn;
				}
			}

			for (int i = 0; i < idx; ++i) {
				FriendBean _fb = friends.get(i);
				if (_fb.id == uid) {
					continue;
				}
				rtn.add(_fb);
				--N;
				if (N <= 0) {
					return rtn;
				}
			}
		}
		return rtn;
	}
}
