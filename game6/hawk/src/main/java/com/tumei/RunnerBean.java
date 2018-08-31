package com.tumei;

import com.tumei.groovy.GroovyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Administrator on 2016/11/10 0010.
 */
@Component
public class RunnerBean {
    private static final Log log = LogFactory.getLog(RunnerBean.class);

    @Value("${runbean.version}")
    private String version;

    @Value("${runbean.real}")
    private int real;

	public int getReal() {
		return real;
	}

	public void setReal(int real) {
		this.real = real;
	}

    @Autowired
    private ApplicationContext ctx;
    @Autowired
	private GroovyLoader loader;

    @PostConstruct
    public void init() {

		loader.registerController("hawk", "MobController");
        loader.registerController("hawk", "TestController");
		loader.registerController("hawk", "AibeiController");
		loader.registerController("hawk", "CmdController");
		loader.registerController("hawk", "ServiceController");

        log.info(this.getClass().getName() + " init.");
    }
}
