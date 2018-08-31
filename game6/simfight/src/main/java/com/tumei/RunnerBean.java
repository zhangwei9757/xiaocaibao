package com.tumei;

import com.tumei.groovy.GroovyLoader;
import com.tumei.groovy.contract.IFightSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

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
        String mod = "simfight";

        loader.registerController(mod, "SimfightController");

        log.info(this.getClass().getName() + " init.");
    }

}
