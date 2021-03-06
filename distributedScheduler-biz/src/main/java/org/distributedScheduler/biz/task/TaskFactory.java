package org.distributedScheduler.biz.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.distributedScheduler.biz.task.TaskProcessSpringInitation.Scanner;
import org.distributedScheduler.biz.task.scheduler.PeriodSchedulerTask;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class TaskFactory {

	private Map<String, Task> fetchers = new HashMap<String, Task>();

	@Resource
	private TaskProcessSpringInitation dataProcessSpringInitation;

	public void start() throws Exception {
		Scanner scanner = dataProcessSpringInitation.getScanner();
		scanner.setAutowireCandidatePatterns(new String[] { "*" });
		TypeFilter tf = new AssignableTypeFilter(Task.class);
		scanner.addIncludeFilter(tf);
		Set<BeanDefinitionHolder> beanSet = scanner
				.scan("org.distributedScheduler");

		ApplicationContext applicationContext = dataProcessSpringInitation
				.getApplicationContext();
		for (BeanDefinitionHolder bean : beanSet) {
			Task dsf = (Task) applicationContext.getBean(bean.getBeanName());
			if (fetchers.get(dsf.getClass().getName()) != null) {
				throw new RuntimeException(
						"dataSourceFetcher has duplicate type:"
								+ dsf.getClass().getName());
			}
			dsf.init();
			fetchers.put(dsf.getClass().getName(), dsf);
		}
	}

	public void shutdown() {
		Set<Entry<String, Task>> set = fetchers.entrySet();
		for (Entry<String, Task> entry : set) {
			try {
				entry.getValue().shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		PeriodSchedulerTask.shutdonwPool();
	}

	public synchronized void shutdownOne(String dataSourceFetcherType) {
		Task fetcher = fetchers.get(dataSourceFetcherType);
		fetcher.shutdown();
	}

	public synchronized void startOne(String dataSourceFetcherType) {
		Task fetcher = fetchers.get(dataSourceFetcherType);
		fetcher.init();
	}

	public synchronized void changeScheduleTask(String taskType, String config) {
		Task fetcher = fetchers.get(taskType);
		fetcher.changePeriod(config);
	}
}
