package com.tumei.configs;

import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/18.
 */
@Validated
@ConfigurationProperties(prefix = "mongo")
@Configuration
public class MongoTemplateConfig {
	private Log log = LogFactory.getLog(MongoTemplateConfig.class);

	private String addr = "";

	private String user = "";

	private String password = "";

	private String db = "tm6";

	private String confdb = "tm6conf";

	private String centerdb = "tm6center";

	@PostConstruct
	void init() {
		log.info("数据库配置:" + toString() + " id:" + this.hashCode());
	}

	private MongoClient buildClient() {

		List<MongoCredential> mcs = new ArrayList<>();
		if (!Strings.isNullOrEmpty(user)) {
			MongoCredential mc = MongoCredential.createCredential(user, "admin", password.toCharArray());
			mcs.add(mc);
		}

		MongoClientOptions options = MongoClientOptions.builder().build();
		ServerAddress sa = new ServerAddress(addr);
		return new MongoClient(sa, mcs, options);
	}

	@Bean(autowire = Autowire.BY_NAME, name = "mongoTemplate")
	public MongoTemplate mongoTemplate() throws Exception {
		log.info("建立连接数据库:" + db);
		MongoClient mongoClient = buildClient();
		MongoTemplate mt = new MongoTemplate(mongoClient, db);
		mt.setWriteResultChecking(WriteResultChecking.EXCEPTION);
		return mt;
	}

	@Bean(autowire = Autowire.BY_NAME, name = "confTemplate")
	public MongoTemplate confTemplate() throws Exception {
		log.info("建立连接数据库:" + confdb);
		MongoClient mongoClient = buildClient();
		MongoTemplate mt = new MongoTemplate(mongoClient, confdb);
		mt.setWriteResultChecking(WriteResultChecking.EXCEPTION);
		return mt;
	}

	@Primary
	@Bean(autowire = Autowire.BY_NAME, name = "centerTemplate")
	public MongoTemplate centerTemplate() throws Exception {
		log.info("建立连接数据库:" + centerdb);
		MongoClient mongoClient = buildClient();
		MongoTemplate mt = new MongoTemplate(mongoClient, centerdb);
		mt.setWriteResultChecking(WriteResultChecking.EXCEPTION);
		return mt;
	}

	public MongoTemplate otherTemplate(String other) throws Exception {
		log.info("建立连接数据库:" + other);
		MongoClient mongoClient = buildClient();
		MongoTemplate mt = new MongoTemplate(mongoClient, other);
		mt.setWriteResultChecking(WriteResultChecking.EXCEPTION);
		return mt;
	}

	/**
	 * Created by Administrator on 2017/1/19 0019.
	 */
	@Configuration
	@EnableMongoRepositories(basePackages = "com.tumei.modelconf", mongoTemplateRef = "confTemplate")
	public static class ConfRepo {
	}

	/**
	 * Created by Administrator on 2017/1/19 0019.
	 */
	@Configuration
	@EnableMongoRepositories(basePackages = "com.tumei.model", mongoTemplateRef = "mongoTemplate")
	public static class ModelConfRepo {
	}

	@Configuration
	@EnableMongoRepositories(basePackages = "com.tumei.centermodel", mongoTemplateRef = "centerTemplate")
	public static class CenterConfRepo {
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getConfdb() {
		return confdb;
	}

	public void setConfdb(String confdb) {
		this.confdb = confdb;
	}

	public String getCenterdb() {
		return centerdb;
	}

	public void setCenterdb(String centerdb) {
		this.centerdb = centerdb;
	}

	@Override
	public String toString() {
		return "MongoTemplateConfig{" + "addr='" + addr + '\'' + ", user='" + user + '\'' + ", password='" + password + '\'' + ", db='" + db + '\'' + ", confdb='" + confdb + '\'' + ", centerdb='" + centerdb + '\'' + '}';
	}
}
