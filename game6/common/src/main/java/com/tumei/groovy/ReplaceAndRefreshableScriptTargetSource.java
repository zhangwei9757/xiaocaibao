package com.tumei.groovy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.RefreshableScriptTargetSource;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 */
public class ReplaceAndRefreshableScriptTargetSource extends RefreshableScriptTargetSource {

	private final ScriptFactory scriptFactory;

	private final ScriptSource scriptSource;

	private final boolean isFactoryBean;


	/**
	 * Create a new RefreshableScriptTargetSource.
	 *
	 * @param beanFactory   the BeanFactory to fetch the scripted bean from
	 * @param beanName      the name of the target bean
	 * @param scriptFactory the ScriptFactory to delegate to for determining
	 *                      whether a refresh is required
	 * @param scriptSource  the ScriptSource for the script definition
	 * @param isFactoryBean whether the target script defines a FactoryBean
	 */
	public ReplaceAndRefreshableScriptTargetSource(BeanFactory beanFactory, String beanName,
												   ScriptFactory scriptFactory, ScriptSource scriptSource, boolean isFactoryBean) {

		super(beanFactory, beanName, scriptFactory, scriptSource, isFactoryBean);
		Assert.notNull(scriptFactory, "ScriptFactory must not be null");
		Assert.notNull(scriptSource, "ScriptSource must not be null");
		this.scriptFactory = scriptFactory;
		this.scriptSource = scriptSource;
		this.isFactoryBean = isFactoryBean;
	}


	/**
	 * Determine whether a refresh is required through calling
	 * ScriptFactory's {@code requiresScriptedObjectRefresh} method.
	 *
	 * @see ScriptFactory#requiresScriptedObjectRefresh(ScriptSource)
	 */
	@Override
	protected boolean requiresRefresh() {
		return this.scriptFactory.requiresScriptedObjectRefresh(this.scriptSource);
	}

	/**
	 * Obtain a fresh target object, retrieving a FactoryBean if necessary.
	 */
	@Override
	protected Object obtainFreshBean(BeanFactory beanFactory, String beanName) {
		removeInjectCache(beanFactory, beanName);

		return super.obtainFreshBean(beanFactory,
		(this.isFactoryBean ? BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName));
	}

	private void removeInjectCache(BeanFactory beanFactory, String beanName) {

		AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor =
		beanFactory.getBean(AutowiredAnnotationBeanPostProcessor.class);

		Map<String, InjectionMetadata> injectionMetadataMap =
		(Map<String, InjectionMetadata>) ReflectionUtils.getField(injectionMetadataCacheField, autowiredAnnotationBeanPostProcessor);

		injectionMetadataMap.remove(beanName.replace("scriptedObject", "scriptFactory"));
		injectionMetadataMap.remove(beanName);
	}

	private static Field injectionMetadataCacheField = ReflectionUtils.findField(AutowiredAnnotationBeanPostProcessor.class, "injectionMetadataCache");

	static {
		injectionMetadataCacheField.setAccessible(true);
	}
}
