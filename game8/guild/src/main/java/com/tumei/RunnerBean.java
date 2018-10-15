package com.tumei;

import com.tumei.groovy.GroovyLoader;
import com.tumei.groovy.contract.IBattle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Administrator on 2016/11/10 0010.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RunnerBean {
    static final Log log = LogFactory.getLog(RunnerBean.class);

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private GroovyLoader loader;

    @PostConstruct
    public void init() {
        String mod = "guild";

        loader.registerService("common", "GroovyBattle", 5000L, false);
        loader.registerService(mod, "BossService", 5000L, true);
        loader.registerController(mod, "BossController");


        log.info(this.getClass().getName() + " init.");
    }

}
