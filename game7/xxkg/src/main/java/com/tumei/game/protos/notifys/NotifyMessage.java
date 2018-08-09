package com.tumei.game.protos.notifys;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tumei.common.utils.JsonUtil;
import com.tumei.dto.MessageDto;
import com.tumei.game.GameUser;
import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class NotifyMessage extends BaseProtocol {
	public List<MessageDto> data = new ArrayList<>();

	@PostConstruct
	void onInit() {
		try {
			File file = ResourceUtils.getFile("messages.cache");
			long l = file.length();
			if (l > 0) {
				byte[] bytes = new byte[(int) l];
				InputStream is = new FileInputStream(file);
				is.read(bytes);
				is.close();

				TypeReference<Deque<MessageDto>> tr = new TypeReference<Deque<MessageDto>>() {
				};

//				GameServer.log.info("bytes:" + new String(bytes));
				cache = JsonUtil.getMapper().readValue(bytes, tr);
//				GameServer.log.warn("init:" + cache.size());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PreDestroy
	void onDispose() {
		/**
		 * 将缓存的消息记录保存下来，下次启动的时候拉取
		 */
		save();
	}

	public void save() {
		try {
			byte[] data = JsonUtil.getMapper().writeValueAsBytes(cache);
			File file = ResourceUtils.getFile("messages.cache");
			OutputStream os = new FileOutputStream(file);
			os.write(data);
			os.close();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 缓存的所有消息
	 */
	private static Deque<MessageDto> cache = new LinkedBlockingDeque<>();
	/**
	 * 缓存的世界消息
	 */
	private static Deque<MessageDto> allcache = new LinkedBlockingDeque<>();

	/**
	 * 暂存一条消息进入缓存
	 *
	 * @param ms
	 */
	public static void push(MessageDto ms) {
		cache.addLast(ms);
		if (cache.size() > 50) {
			cache.pollFirst();
		}
	}

	public static void pushWorld(MessageDto ms) {
		allcache.addLast(ms);
		if (allcache.size() > 50) {
			allcache.pollFirst();
		}
	}

	/**
	 * 将最近的消息广播
	 */
	public static void broadcast(GameUser user) {
		NotifyMessage nm = new NotifyMessage();

		Iterator<MessageDto> itr = cache.iterator();
		while (itr.hasNext()) {
			MessageDto ms = itr.next();
			nm.data.add(ms);
		}

		itr = allcache.iterator();
		while (itr.hasNext()) {
			MessageDto ms = itr.next();
			nm.data.add(ms);
		}

		user.send(nm);
	}
}
