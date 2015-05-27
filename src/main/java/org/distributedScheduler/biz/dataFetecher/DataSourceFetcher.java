package org.distributedScheduler.biz.dataFetecher;

import java.util.Map;

public interface DataSourceFetcher {

	/**
	 * 初始化：主动获取挂到定时器，消息数据通过消息中间件来
	 */
	void init();

	/**
	 * 关闭获取
	 */
	void shutdown();

	DataSourceFetcherType getDataSourceFetcherType();

	FetcherStatus getStatus();

	public enum FetcherStatus {
		INIT, RUNNING, LOCK_FAILED, STOP;
	}

	/**
	 * 结果集的key
	 * 
	 * @return
	 */
	Map<String, String> getDataSourceResultTypes();
}
