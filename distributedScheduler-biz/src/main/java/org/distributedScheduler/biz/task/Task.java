package org.distributedScheduler.biz.task;


public interface Task {

	/**
	 * 初始化：主动获取挂到定时器，消息数据通过消息中间件来
	 */
	void init();

	/**
	 * 关闭获取
	 */
	void shutdown();

	TaskStatus getStatus();

	boolean changePeriod(String period);

	public enum TaskStatus {
		INIT, RUNNING, LOCK_FAILED, STOP;
	}
}
