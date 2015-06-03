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

	private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

	@Override
	public void init() {
		Scheduler scheduler;
		try {
			scheduler = schedulerFactory.getScheduler();
			JobDetail job = JobBuilder.newJob(this.getClass())
					.withIdentity(this.getClass().getName()).build();

			DateTimeFormatter df = DateTimeFormat
					.forPattern("yyyy-MM-dd HH:mm:ss");
			Trigger trigger = TriggerBuilder
					.newTrigger()
					.withIdentity(
							new TriggerKey(this.getClass().getName(),
									"CronSchedulerTask"))
					.withSchedule(
							CronScheduleBuilder
									.cronSchedule(getCronExpression()))
					.startAt(df.parseDateTime(getStartTime()).toDate()).build();

			scheduler.scheduleJob(job, trigger);
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		getData();
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

	public abstract String getStartTime();

	public abstract Map<String, Object> getData();
}
