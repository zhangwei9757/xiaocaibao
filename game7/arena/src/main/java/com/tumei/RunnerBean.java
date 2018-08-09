package com.tumei;

import com.tumei.groovy.GroovyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Administrator on 2016/11/10 0010.
 */
@Component
public class RunnerBean {
    private static final Log log = LogFactory.getLog(RunnerBean.class);

    @Autowired
    private GroovyLoader loader;

    @PostConstruct
    public void init() {
        loader.setReload(true);

        loader.registerService("common", "GroovyBattle", 5000L);
        loader.registerService("arena", "ArenaService", 5000L);
        loader.registerController("arena", "ArenaController");

        log.info(this.getClass().getName() + " init.");
    }

}
