package org.distributedScheduler.biz.task.cron;

import java.util.Map;

import org.distributedScheduler.biz.task.Task;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

public abstract class CronSchedulerTask implements Task, Job {

	private static Scheduler scheduler;

	static {
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		try {
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	private TriggerKey triggerKey = new TriggerKey(this.getClass().getName(),
			"CronSchedulerTask");

	private TaskStatus status = TaskStatus.INIT;

	@Override
	public void init() {
		try {
			JobDetail job = JobBuilder.newJob(this.getClass())
					.withIdentity(this.getClass().getName()).build();

			DateTimeFormatter df = DateTimeFormat
					.forPattern("yyyy-MM-dd HH:mm:ss");
			Trigger trigger = TriggerBuilder
					.newTrigger()
					.withIdentity(triggerKey)
					.withSchedule(
							CronScheduleBuilder
									.cronSchedule(getCronExpression()))
					.startAt(df.parseDateTime(getStartTime()).toDate()).build();

			scheduler.scheduleJob(job, trigger);
			status = TaskStatus.RUNNING;
		} catch (SchedulerException e) {
			e.printStackTrace();
			status = TaskStatus.STOP;
		}

	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		getData();
	}

	@Override
	public void shutdown() {
		try {
			scheduler.unscheduleJob(triggerKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public TaskStatus getStatus() {
		return status;
	}

	public void reScheduleJob(String cronExpression) {
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		try {
			scheduler.rescheduleJob(
					triggerKey,
					TriggerBuilder
							.newTrigger()
							.withIdentity(triggerKey)
							.withSchedule(
									CronScheduleBuilder
											.cronSchedule(cronExpression))
							.startAt(df.parseDateTime(getStartTime()).toDate())
							.build());
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public abstract String getCronExpression();

	public abstract String getStartTime();

	public abstract Map<String, Object> getData();
}
