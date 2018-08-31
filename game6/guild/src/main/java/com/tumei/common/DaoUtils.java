package com.tumei.common;

import com.tumei.common.service.CacheIt;
import com.tumei.common.utils.RandomUtil;
import com.tumei.model.IDBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by Administrator on 2016/12/23 0023.
 */
@Component
public class DaoUtils {
    private Log log = LogFactory.getLog(DaoUtils.class);

    @Autowired
    private CacheIt cacheIt;

    /**
     * 账户数据库的Template操作句柄
     */
    @Autowired
    private MongoTemplate mongoTemplate;


    /********** 各种缓存接口 **********/

    /********** END 各种缓存接口 **********/



    /***
     * 根据玩家id创建一个属于玩家的随机串
     * @param id
     * @return
     */
    private String createRandomChars(Long id) {
        StringBuilder sb = new StringBuilder();
        String letters = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 2; ++i) {
            char c = letters.charAt(RandomUtil.getRandom() % letters.length());
            sb.append(c);
        }
        sb.append((id/10000));
        return sb.toString();
    }

    /**
     * 注册各种缓存与数据库的对应
     */
    @PostConstruct
    public void initialize() {
        log.warn(this.getClass().getName() + " init.");
    }

    /**
     * 系统退出或者关闭的时候需要调用，确保更新到数据库
     */
    @PreDestroy
    public void dispose() {
        log.warn("--- dispose: " + this.getClass().getName());
    }

	/**
	 * 获取一个随机递增的数字, twitter-snowflake 算法
	 * @return
	 *
	 * 	workerid + datacenterid + sequence
	 */
	public long nextVal() {
        Query query = new Query(where("name").is("guildid"));
        Update update = new Update().inc("nextval", 1);
        IDBean idBean = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), IDBean.class);
        return idBean.nextval;
    }
}
