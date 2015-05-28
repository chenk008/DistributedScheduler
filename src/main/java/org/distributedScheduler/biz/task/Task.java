package org.distributedScheduler.biz.task;

import java.util.Map;

public interface Task {

	/**
	 * 初始化：主动获取挂到定时器，消息数据通过消息中间件来
	 */
	void init();

	/**
	 * 关闭获取
	 */
	void shutdown();

	FetcherStatus getStatus();

	public enum FetcherStatus {
		INIT, RUNNING, LOCK_FAILED, STOP;
	}
}
