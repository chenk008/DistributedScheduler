package org.distributedScheduler.biz.task.cron;

import org.distributedScheduler.biz.task.Task;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public abstract class CronSchedulerTask implements Task {

	private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

	@Override
	public void init() {
		Scheduler scheduler;
		try {
			scheduler = schedulerFactory.getScheduler();

		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public TaskStatus getStatus() {
		return null;
	}

	public abstract String getCronExpression();
}
