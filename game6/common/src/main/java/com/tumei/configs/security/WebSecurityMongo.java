package com.tumei.configs.security;

import com.google.common.base.Strings;
import com.mongodb.WriteResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSecurityMongo {
    private static final Log log = LogFactory.getLog(WebSecurityMongo.class);

    @Value("${runbean.security}")
    public String mode;

    @Autowired
    @Qualifier("confTemplate")
    private MongoTemplate confTemplate;
    @Autowired
    private CustomFilterInvocationSecurityMetadataSource customFilterInvocationSecurityMetadataSource;
    @Autowired
    private CustomAccessDecisionManager customAccessDecisionManager;


    public int insert(WebSecurityConf wsc) {
        try {
            wsc.mode = wsc.mode.toLowerCase();
            Query query = Query.query(Criteria.where("url").is(wsc.url).and("mode").is(wsc.mode));
            WebSecurityConf one = confTemplate.findOne(query, WebSecurityConf.class);
            // 指定url && mode 存在就直接修改，不存在则插入
            if (one == null) {
                if (!Strings.isNullOrEmpty(wsc.role)) {
                    wsc.role = wsc.role.toUpperCase();
                } else {
                    wsc.role = "";
                }
                confTemplate.save(wsc);
            } else {
                return update(wsc);
            }
        } catch (Exception ex) {
            return -1;
        }
        // 新增成功或者修改成功，直接把内存中权限同步操作
        customFilterInvocationSecurityMetadataSource.update(wsc.url, wsc.role);
        return 1;
    }

    public int delete(WebSecurityConf wsc) {
        Query query = Query.query(Criteria.where("url").is(wsc.url)
                .andOperator(Criteria.where("mode").is(wsc.mode)));

        if (!Strings.isNullOrEmpty(wsc.role)) {
            query.addCriteria(Criteria.where("role").is(wsc.role));

        }
        WriteResult remove = confTemplate.remove(query, WebSecurityConf.class);
        int index = remove.getN();
        if (index == 1) {
            // 删除成功，直接把内存中权限同步操作
            customFilterInvocationSecurityMetadataSource.delete(wsc.url);
        }
        return index;
    }

    public int update(WebSecurityConf wsc) {
        String role = "";
        if (!Strings.isNullOrEmpty(wsc.role)) {
            role = wsc.role.toUpperCase();
        }
        wsc.mode = wsc.mode.toLowerCase();
        Query query = Query.query(Criteria.where("url").is(wsc.url))
                .addCriteria(Criteria.where("mode").is(wsc.mode));

        Update update = Update.update("role", role);

        WriteResult writeResult = confTemplate.updateFirst(query, update, WebSecurityConf.class);
        int index = writeResult.getN();
        if (index == 1) {
            // 修改成功，直接把内存中权限同步操作
            customFilterInvocationSecurityMetadataSource.update(wsc.url, wsc.role);
        }
        return index;
    }

    public List<WebSecurityConf> findByfuzzy(WebSecurityConf wsc) {
        List<WebSecurityConf> list = new ArrayList<>();
        Query query = null;

        if (!Strings.isNullOrEmpty(wsc.url)) {
            query = Query.query(Criteria.where("url").is(wsc.url));
            if (!Strings.isNullOrEmpty(wsc.mode)) {
                query = query.addCriteria(Criteria.where("mode").is(wsc.mode));
            }
            if (!Strings.isNullOrEmpty(wsc.role)) {
                query = query.addCriteria(Criteria.where("role").is(wsc.role));
            }
            if (query != null) {
                // 条件为空直接返回全部列表
                list = confTemplate.find(query, WebSecurityConf.class);
            } else {
                list = confTemplate.findAll(WebSecurityConf.class);
            }
        } else {
            log.error("wsc.url is null, this is impossible, should be checked by devloper.");
        }

        return list;
    }

    public List<WebSecurityConf> getUrlsByMode(String mode) {
        Query query = Query.query(Criteria.where("mode").is(mode));
        return confTemplate.find(query, WebSecurityConf.class);
    }
}
