package com.tumei.common;

import com.google.common.base.Strings;
import com.tumei.centermodel.BundleBean;
import com.tumei.centermodel.ParamBean;
import com.tumei.common.utils.TimeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Leon on 2017/9/14 0014.
 */
@Service
public class Readonly {
	@PostConstruct
	void init() {
		reload();
	}

	@PreDestroy
	void dispose() {
		log.warn("---- 只读配置析构");
	}

	private Log log = LogFactory.getLog(Readonly.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	class Conf {
		public Map<String, BundleBean> bundles = new HashMap<>();

		public Map<String, String> params = new HashMap<>();

		public void Initialize() {
			// 包名
			List<BundleBean> bbs = mongoTemplate.findAll(BundleBean.class);
			if (bbs != null && bbs.size() > 0) {
				bbs.forEach(bb -> bundles.put(bb.bundle, bb));
			} else {
				BundleBean bb = new BundleBean();
				bb.bundle = "com.tumei.tm006";
				mongoTemplate.save(bb);
			}

			List<ParamBean> pbs = mongoTemplate.findAll(ParamBean.class);
			if (pbs != null && pbs.size() > 0) {
				pbs.forEach(bb -> params.put(bb.key.toLowerCase(), bb.value));
			} else {
				ParamBean pb = new ParamBean();
				pb.key = "webbuff";
				pb.value = "0";
				params.put(pb.key, pb.value);
				mongoTemplate.save(pb);
			}
		}
	}

	private Conf conf;

	/**
	 * 获得所有可用的包名
	 * @return
	 */
	public Map<String, BundleBean> getAllBundles() {
		return conf.bundles;
	}

	/**
	 * 重新加载新的配置
	 */
	public void reload() {
		Conf _conf = new Conf();
		_conf.Initialize();
		conf = _conf;
	}

	public String debug() {
		return conf.bundles.toString() + "\n" + conf.params.toString();
	}

	public String getParam(String key) {
		return conf.params.getOrDefault(key.toLowerCase(), "");
	}

	/**
	 * 获取当前网页充值的buff, buff是一个用逗号分隔的2个日期[A,B]
	 * 只有在A,B两个日期之间的时间，才会被允许生成新的buff,并且buff为
	 * 第一个日期标识的时间，否则返回0，没有buff
	 *
	 * @return
	 */
	public int getWebBuff() {
		try {
			String buff = getParam("webbuff");
			if (Strings.isNullOrEmpty(buff)) {
				return 0;
			}
			String[] fields = buff.split(",");
			if (fields.length != 2) {
				return 0;
			}

			int begin = Integer.parseInt(fields[0]);
			int end = Integer.parseInt(fields[1]);

			LocalDate ldBegin = TimeUtil.fromDay(begin);
			LocalDate ldEnd = TimeUtil.fromDay(end);
			LocalDate now = LocalDate.now();

			// 如果今天还在标记的buff活动期间
			if (now.isBefore(ldBegin) || now.isAfter(ldEnd)) {
				return 0;
			} else {
				return begin;
			}
		} catch (Exception ex) {

		}
		return 0;
	}
}
