package org.distributedScheduler.biz.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.distributedScheduler.biz.task.TaskProcessSpringInitation.Scanner;
import org.distributedScheduler.biz.task.scheduler.PeriodSchedulerDataSourceFetcher;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class DataSourceFactory {

	private Map<String, DataSourceFetcher> fetchers = new HashMap<String, DataSourceFetcher>();

	@Resource
	private TaskProcessSpringInitation dataProcessSpringInitation;

	public void start() throws Exception {
		Scanner scanner = dataProcessSpringInitation.getScanner();
		scanner.setAutowireCandidatePatterns(new String[] { "*" });
		TypeFilter tf = new AssignableTypeFilter(DataSourceFetcher.class);
		scanner.addIncludeFilter(tf);
		Set<BeanDefinitionHolder> beanSet = scanner
				.scan("org.distributedScheduler");

		ApplicationContext applicationContext = dataProcessSpringInitation
				.getApplicationContext();
		for (BeanDefinitionHolder bean : beanSet) {
			DataSourceFetcher dsf = (DataSourceFetcher) applicationContext
					.getBean(bean.getBeanName());
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
		Set<Entry<String, DataSourceFetcher>> set = fetchers.entrySet();
		for (Entry<String, DataSourceFetcher> entry : set) {
			try {
				entry.getValue().shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		PeriodSchedulerDataSourceFetcher.shutdonwPool();
	}

	public synchronized void shutdownOne(String dataSourceFetcherType) {
		DataSourceFetcher fetcher = fetchers.get(dataSourceFetcherType);
		fetcher.shutdown();
	}

	public synchronized void startOne(String dataSourceFetcherType) {
		DataSourceFetcher fetcher = fetchers.get(dataSourceFetcherType);
		fetcher.init();
	}

	public synchronized void changeScheduleTask(String dataSourceFetcherType,
			int period) {
		DataSourceFetcher fetcher = fetchers.get(dataSourceFetcherType);
		if (fetcher instanceof PeriodSchedulerDataSourceFetcher) {
			((PeriodSchedulerDataSourceFetcher) fetcher).changePeriod(period);
		}
	}
}
