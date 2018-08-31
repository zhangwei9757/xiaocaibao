package com.tumei;

import com.tumei.groovy.GroovyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Administrator on 2016/11/10 0010.
 */
@Component
public class RunnerBean {
    private Log log = LogFactory.getLog(RunnerBean.class);

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private GroovyLoader loader;

    @PostConstruct
    public void init() {
        String mod = "arena";

        loader.registerController(mod, "ArenaController");

        log.info(this.getClass().getName() + " init.");
    }

}
