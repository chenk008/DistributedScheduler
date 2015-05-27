package org.distributedScheduler;

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

public class DataProcessSpringInitation implements
		BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

	private Scanner scanner;
	private ApplicationContext applicationContext;

	public static class Scanner extends ClassPathBeanDefinitionScanner {

		public Scanner(BeanDefinitionRegistry registry) {
			super(registry);
		}

		public Set<BeanDefinitionHolder> scan(String packageName) {
			return this.doScan(packageName);
		}

	}

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postProcessBeanDefinitionRegistry(
			BeanDefinitionRegistry registry) throws BeansException {
		scanner = new Scanner(registry);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public Scanner getScanner() {
		return scanner;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

}
