package com.tumei.common.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import com.tumei.common.utils.RandomUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/12/27 0027.
 * <p>
 * 1. 负责通过服务名 + 服务器id 得到具体服务url
 * 2. 通过服务名获取对应的所以服务实例
 */
public class RouterService {
	private static final Log log = LogFactory.getLog(RouterService.class);

	@Autowired
	private ConsulClient consulClient;

	/**
	 * consul的kv 解密字段
	 */
	@Value("${router.token:nevergotcha}")
	private String token = "nevergotcha";

	private boolean isChecking;

	/**
	 * 当前检测到的所有服务
	 */
	private ConcurrentHashMap<String, ServiceItems> services = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		update();
		debug();
	}

	@Scheduled(fixedDelay = 5000)
	void update() {
		if (!isChecking) {
			return;
		}

		Map<String, com.ecwid.consul.v1.agent.model.Service> ss = consulClient.getAgentServices().getValue();

		// 1. 遍历所有注册的服务名
		ss.forEach((k, v) -> {
			// 2. 根据服务名，遍历所有的实例
			String service = v.getService();

			ServiceItems s = services.getOrDefault(service, null);
			if (s == null) {
				s = new ServiceItems(service);
				s.items.put(k, new ServiceItem(k, service, v.getAddress(), v.getPort()));
				services.put(service, s);
				log.warn("+++++ 服务[" + service + "] 上线，实例[" + k + "], 对应地址:" + v.getAddress());
			}

			s.refresh(k, v.getAddress(), v.getPort());
		});

		long now = System.currentTimeMillis() / 1000;
		services.forEach((k, v) -> {
			List<String> dels = new ArrayList<String>();
			v.items.forEach((kk, vv) -> {
				if (now >= vv.ts) {
					dels.add(kk);
				}
			});
			dels.forEach(kk -> {
				v.items.remove(kk);
			});
		});

		// 根据健康检查，查看是否有服务注销掉
//        services.forEach((k, v) -> {
//			List<HealthService> hss = consulClient.getHealthServices(k, false, null).getValue();
//			for (HealthService hs : hss) {
//
//				hs.getChecks().forEach(check -> {
//					String instance = check.getServiceId();
//					if (!check.getStatus().name().equals(Check.CheckStatus.PASSING.name())) {
//						v.items.remove(instance);
//						consulClient.agentCheckDeregister(instance);
//					}
//				});
//			}
//		});

//		debug();
	}

	/**
	 * 是否包含服务
	 * @param server
	 * @param serverid
	 * @return
	 */
	public boolean contains(String server, int serverid) {
		ServiceItems sis = services.getOrDefault(server, null);
		if (sis == null) {
			return false;
		}

		if (serverid < 0) { // 只要有任何一个服务即可
			if (sis.items.size() > 0) {
				return true;
			}
		}

		ServiceItem si = sis.items.getOrDefault(String.format("%s%s", server, serverid), null);
		if (si != null) {
			return true;
		}
		return false;
	}

	/**
	 * 根据服务及对应的id，获取对应服务器的url,
	 * 如果服务id为-1，表示随机获取
	 *
	 * @param server
	 * @param serverid
	 * @return
	 */
	public String getServiceURI(String server, int serverid) {
		server = server.toLowerCase();
		ServiceItems sis = services.getOrDefault(server, null);
		if (sis == null) {
			log.error("--- 服务:" + server + " 不存在!");
			return null;
		}
		if (sis.items.size() <= 0) {
			log.error("--- 服务:" + server + " 没有对应的实例在线!");
			return null;
		}

		if (serverid < 0) {
			ArrayList<ServiceItem> array = new ArrayList<>(sis.items.values());
//			log.info("战斗服务器数量:" + array.size());
			ServiceItem si = array.get(RandomUtil.getRandom() % array.size());
			if (si != null) {
				return si.uri;
			}
		}

		{
			ServiceItem si = sis.items.getOrDefault(String.format("%s%s", server, serverid), null);
			if (si != null) {
				return si.uri;
			}
		}

		return null;
	}


	/**
	 * 获取服务url
	 *
	 * @param server
	 * @param serverid
	 * @param suffix
	 * @return
	 */
	public String getServiceUrl(String server, int serverid, String suffix) {
		String uri = getServiceURI(server, serverid);
		if (uri == null) {
			log.error("--- RemoteService error, server[" + server + "] serverid[" + serverid + "] suffix[" + suffix + "].");
			return null;
		}
		String url = uri.toString() + suffix;

//		log.info("+++ RemoteService called, server[" + server + "] serverid[" + serverid + "] suffix[" + suffix + "] url[" + url + "].");
		return url;
	}

	public String debug() {
		List<String> msgs = new ArrayList<>();
		services.forEach((k, v) -> {
			msgs.add("+++ service:" + k);
			v.items.forEach((kk, vv) -> {
				msgs.add(" \t--instance:" + kk + " url:" + vv.uri);
			});
		});

		return String.join("\r\n", msgs);
	}

	/**
	 * 将所有服务中不能通过健康检查的干掉
	 *
	 * @return
	 */
	public String deregisterNoPassing() {
		services.forEach((service, v) -> {
			List<HealthService> response = consulClient.getHealthServices(service, false, null).getValue();
			for (HealthService sv : response) {
				ConsulClient cc = new ConsulClient(sv.getNode().getAddress(), 8500);
				sv.getChecks().forEach(check -> {
					if (check.getStatus() != Check.CheckStatus.PASSING) {
						cc.agentServiceDeregister(check.getServiceId());
					}
				});
			}
		});
		return "完成";
	}

	/**
	 * 获取consul的keyvalue
	 *
	 * @param key
	 * @return
	 */
	public String getKeyValues(String key) {
		try {
			return consulClient.getKVValue(key, token).getValue().getDecodedValue();
		} catch (Exception e) {
			return "";
		}
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isChecking() {
		return isChecking;
	}

	public void setChecking(boolean checking) {
		isChecking = checking;
	}
}
