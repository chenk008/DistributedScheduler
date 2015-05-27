package org.distributedScheduler.biz.dataFetecher;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.distributedScheduler.DataProcessSpringInitation;
import org.distributedScheduler.DataProcessSpringInitation.Scanner;
import org.distributedScheduler.biz.dataFetecher.scheduler.PeriodSchedulerDataSourceFetcher;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class DataSourceFactory {

	private Map<String, DataSourceFetcher> fetchers = new HashMap<String, DataSourceFetcher>();

	@Resource
	private DataProcessSpringInitation dataProcessSpringInitation;

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
			if (fetchers.get(dsf.getDataSourceFetcherType().name()) != null) {
				throw new RuntimeException(
						"dataSourceFetcher has duplicate type:"
								+ dsf.getDataSourceFetcherType().name());
			}
			dsf.init();
			fetchers.put(dsf.getDataSourceFetcherType().name(), dsf);
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
