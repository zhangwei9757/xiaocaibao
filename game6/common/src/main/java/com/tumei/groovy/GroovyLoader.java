package com.tumei.groovy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.ScriptFactoryPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本加载器，通过groovyScriptFactoryPostProcessor加载脚本，并设置刷新时间
 */
@Component
public class GroovyLoader {

	protected static final Log logger = LogFactory.getLog(GroovyLoader.class);

	// 上下文
	private ApplicationContext context;
	// Bean工厂
	private DefaultListableBeanFactory beanFactory;

	private static Method detectHandlerMethodsMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "detectHandlerMethods", Object.class);

	private static Method getMappingForMethodMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);

	private static Method getMappingPathPatternsMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingPathPatterns", RequestMappingInfo.class);

	private static Method getPathMatcherMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getPathMatcher");

	private static Field injectionMetadataCacheField = ReflectionUtils.findField(AutowiredAnnotationBeanPostProcessor.class, "injectionMetadataCache");

	public boolean isEditor;
	private String scriptPath;

	static {
		detectHandlerMethodsMethod.setAccessible(true);
		getMappingForMethodMethod.setAccessible(true);
		getMappingPathPatternsMethod.setAccessible(true);
		getPathMatcherMethod.setAccessible(true);
		injectionMetadataCacheField.setAccessible(true);
	}

	@PostConstruct
	void init() {
		String ie = context.getEnvironment().getProperty("editor");
		if (ie == null || !ie.equals("1")) {
			isEditor = false;
		} else {
			isEditor = true;
		}

		if (isEditor) {
			scriptPath = "file:%s/src/main/java/com/tumei/groovy/commands";
		} else {
			scriptPath = "file:../commands";
		}
	}


	@Autowired
	public void setApplicationContext(ApplicationContext ctx) {
		if (!DefaultListableBeanFactory.class.isAssignableFrom(ctx.getAutowireCapableBeanFactory().getClass())) {
			throw new IllegalArgumentException("BeanFactory must be DefaultListableBeanFactory type");
		}
		this.context = ctx;
		this.beanFactory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
	}

	/**
	 * 注册控制器 用于web服务
	 * @param mod
	 * @param name
	 */
	public void registerController(String mod, String name) {
		// 真实环境或编辑器环境都使用注册方式,不扫描
		try {
			registerGroovyController(mod, name, scriptPath + "/controller/" + name + ".groovy");
		} catch (Exception ex) {
			logger.error("注册控制器mod(" + mod + ") name(" + name + ") 失败:" + ex.getMessage());
		}
	}

	/**
	 * 注册协议
	 * @param mod
	 * @param name
	 */
	public void registerProtocol(String mod, String name) {
		String location = String.format("protos/%s", name);

		byte[] items = name.getBytes();
		int i = (int) items[0];
		if (i < 97) {
			i = i - 65 + 97;
			items[0] = (byte) i;
			name = new String(items);
		}

		if (!this.isEditor) {
			// 真实环境
			try {
				registerGroovy(mod, name, scriptPath + "/" + location + ".groovy", 30000l, true);
			} catch (Exception ex) {
				logger.error("注册协议mod(" + mod + ") (" + name + ")失败:" + ex.getMessage());
			}
		} else {
			// 编辑器模式
			try {
				registerGroovy(mod, name, scriptPath + "/" + location + ".groovy", 1000l, true);
			} catch (Exception ex) {
				logger.error("注册协议mod(" + mod + ") (" + name + ")失败:" + ex.getMessage());
			}
		}
	}

	/**
	 * 注册服务
	 * @param mod
	 * @param name
	 */
	public void registerService(String mod, String name) {
		try {
			registerGroovy(mod, name, scriptPath + "/" + name + ".groovy", 1000L, false);
		} catch (Exception ex) {
			logger.error("注册服务mod(" + mod + ") (" + name + ")失败:" + ex.getMessage());
		}
	}

	public void registerBean(Class<?> beanClass) {
		registerBean(null, beanClass);
	}

	/**
	 * 将普通的Pojo类，动态加载为Bean
	 *
	 * @param beanName
	 * @param beanClass
	 */
	public void registerBean(String beanName, Class<?> beanClass) {
		Assert.notNull(beanClass, "register bean class must not null");
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(beanClass);

		if (StringUtils.hasText(beanName)) {
			beanFactory.registerBeanDefinition(beanName, bd);
		}
		else {
			BeanDefinitionReaderUtils.registerWithGeneratedName(bd, beanFactory);
		}
	}

	/**
	 * 注册groovy脚本
	 *
	 * @param beanName       名字
	 * @param scriptLocation 位置
	 */
	public void registerGroovy(String module, String beanName, String scriptLocation, long delayCheck, boolean proxy) {
		scriptLocation = String.format(scriptLocation, module);

		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClassName(GroovyScriptFactory.class.getName());

		// 设置脚本类型
		bd.setAttribute(ScriptFactoryPostProcessor.LANGUAGE_ATTRIBUTE, "groovy");
		// 设置更新时间
		bd.setAttribute(ScriptFactoryPostProcessor.REFRESH_CHECK_DELAY_ATTRIBUTE, delayCheck);
		// 设置调用代理
		bd.setAttribute(ScriptFactoryPostProcessor.PROXY_TARGET_CLASS_ATTRIBUTE, proxy);
		// 对应每个groovy文件,创建一个对应都GroovyFactory来管理,所以这里一定是prototype
		bd.setScope("prototype");

		ConstructorArgumentValues cav = bd.getConstructorArgumentValues();
		int constructorArgNum = 0;
		cav.addIndexedArgumentValue(constructorArgNum++, scriptLocation);

		beanFactory.registerBeanDefinition(beanName, bd);

		logger.warn("成功注册实体:" + beanName);
	}

	public void registerGroovySingleton(String module, String beanName, String scriptLocation) throws IOException {
		scriptLocation = String.format(scriptLocation, module);

		if (scriptNotExists(scriptLocation)) {
			logger.error("实体(" + beanName + ") 位置(" + scriptLocation + ") 不存在这样的脚本，无法注册!");
			return;
		}
		addScriptIntoMap(module, beanName, scriptLocation);

		GroovyScriptFactory groovyScriptFactory = new GroovyScriptFactory(scriptLocation);
		groovyScriptFactory.setBeanFactory(this.beanFactory);
		groovyScriptFactory.setBeanClassLoader(this.beanFactory.getBeanClassLoader());

		Object c = groovyScriptFactory.getScriptedObject(new ResourceScriptSource(context.getResource(scriptLocation)));

		if (beanFactory.containsBean(beanName)) {
			beanFactory.destroySingleton(beanName);
			removeInjectCache(c);
		}

		beanFactory.registerSingleton(beanName, c);
		beanFactory.autowireBean(c);

		logger.warn("成功注册单例实体:" + beanName);
	}



	/**
	 * 可重复注册 Controller,
	 * <p>
	 * 不同点:
	 * 1. Singleton
	 * 2. 需要修改路由器
	 *
	 * @param beanName
	 * @param scriptLocation
	 * @throws IOException
	 */
	public void registerGroovyController(String module, String beanName, String scriptLocation) throws IOException {
		scriptLocation = String.format(scriptLocation, module);

		if (scriptNotExists(scriptLocation)) {
			logger.error("实体(" + beanName + ") 位置(" + scriptLocation + ") 不存在这样的脚本，无法注册!");
			return;
		}
		addScriptIntoMap(module, beanName, scriptLocation);

		GroovyScriptFactory groovyScriptFactory = new GroovyScriptFactory(scriptLocation);
		groovyScriptFactory.setBeanFactory(this.beanFactory);
		groovyScriptFactory.setBeanClassLoader(this.beanFactory.getBeanClassLoader());

		Object c = groovyScriptFactory.getScriptedObject(new ResourceScriptSource(context.getResource(scriptLocation)));

		removeHandler(beanName);
		if (beanFactory.containsBean(beanName)) {
			beanFactory.destroySingleton(beanName);
			removeInjectCache(c);
		}

		beanFactory.registerSingleton(beanName, c);
		beanFactory.autowireBean(c);

		addHandler(beanName);

		logger.warn("成功注册实体:" + beanName);
	}

	private void removeInjectCache(Object controller) {
		AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = context.getBean(AutowiredAnnotationBeanPostProcessor.class);

		Map<String, InjectionMetadata> injectionMetadataMap = (Map<String, InjectionMetadata>) ReflectionUtils.getField(injectionMetadataCacheField, autowiredAnnotationBeanPostProcessor);

		injectionMetadataMap.remove(controller.getClass().getName());
	}

	private void removeHandler(String controllerBeanName) {
		RequestMappingHandlerMapping requestMappingHandlerMapping = requestMappingHandlerMapping();

		try {
			Class<?> handlerType = context.getType(controllerBeanName);
			final Class<?> userType = ClassUtils.getUserClass(handlerType);

			final RequestMappingHandlerMapping innerRequestMappingHandlerMapping = requestMappingHandlerMapping;
			Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
				@Override
				public boolean matches(Method method) {
					return ReflectionUtils.invokeMethod(getMappingForMethodMethod, innerRequestMappingHandlerMapping, method, userType) != null;
				}
			});

			for (Method method : methods) {
				RequestMappingInfo mapping = (RequestMappingInfo) ReflectionUtils.invokeMethod(getMappingForMethodMethod, requestMappingHandlerMapping, method, userType);

				// 删除映射handler
				requestMappingHandlerMapping.unregisterMapping(mapping);
			}
		} catch (Exception ex) {
			// remove的时候可能没有注册对应的controller
		}
	}

	private void addHandler(String controllerBeanName) {
		RequestMappingHandlerMapping requestMappingHandlerMapping = requestMappingHandlerMapping();
		//spring 3.1 开始
		ReflectionUtils.invokeMethod(detectHandlerMethodsMethod, requestMappingHandlerMapping, controllerBeanName);
	}

	private RequestMappingHandlerMapping requestMappingHandlerMapping() {
		try {
			return context.getBean(RequestMappingHandlerMapping.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("applicationContext must has RequestMappingHandlerMapping");
		}
	}

	public Object getBean(String name) {
		return beanFactory.getBean(name);
	}


	/**
	 * 检查脚本的最后更新时间
	 *
	 * @param scriptLocation
	 * @return
	 */
	private long scriptLastModified(String scriptLocation) {
		try {
			return context.getResource(scriptLocation).getFile().lastModified();
		} catch (Exception e) {
			return -1;
		}
	}

	private boolean scriptNotExists(String scriptLocation) {
		return !context.getResource(scriptLocation).exists();
	}

	/**
	 * 脚本实体名字 对应的 脚本具体信息
	 */
	private Map<String, ScriptInfo> scriptLastModifiedMap = new ConcurrentHashMap<>();

	@Scheduled(fixedDelay = 30000L)
	private void checkScripts() {
		HashMap<String, ScriptInfo> copyMap = new HashMap<>(scriptLastModifiedMap);
		for (ScriptInfo info : copyMap.values()) {
			if (scriptNotExists(info.location)) {
				scriptLastModifiedMap.remove(info);
			}

			long time = scriptLastModified(info.location);
			if (info.lastModified < time) {
				logger.info("注册实体:" + info.name);
				try {
					registerGroovyController(info.module, info.name, info.location);
				} catch (Exception ex) {
					logger.error("注册(" + info.name + ") 位置(" + info.location + ") 失败:" + ex.getMessage());
					scriptLastModifiedMap.remove(info);
				}
			}
		}
	}

	/**
	 * 记录脚本更新时间
	 *
	 * @param beanName
	 * @param scriptLocation
	 */
	private void addScriptIntoMap(String module, String beanName, String scriptLocation) {
		if (scriptLastModifiedMap.computeIfPresent(beanName, (n, i) -> {
			i.location = scriptLocation;
			i.lastModified = scriptLastModified(scriptLocation);
			return i;
		}) == null) {
			scriptLastModifiedMap.put(beanName, new ScriptInfo(module, beanName, scriptLocation, scriptLastModified(scriptLocation)));
		}
	}

	public List<ScriptInfo> status() {
		List<ScriptInfo> infos = new ArrayList<>();
		for (ScriptInfo si : scriptLastModifiedMap.values()) {
			infos.add(si);
		}
		return infos;
	}
}

