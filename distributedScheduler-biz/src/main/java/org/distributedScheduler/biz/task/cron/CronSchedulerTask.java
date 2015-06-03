package org.distributedScheduler.biz.task.cron;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.distributedScheduler.biz.config.ConfigService;
import org.distributedScheduler.biz.lock.DistributedLock;
import org.distributedScheduler.biz.lock.impl.ZooKeeperDistributedLock;
import org.distributedScheduler.biz.task.Task;
import org.distributedScheduler.biz.task.annotation.SingleRun;
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

	@Resource
	private DistributedLock distributedLock;

	@Resource
	private ConfigService configService;

	private String configPath = ConfigService.PERIOD_CONFIG_PATH + "/"
			+ this.getClass().getName();

	private String lockNode = ZooKeeperDistributedLock.LOCK_PATH + "/"
			+ this.getClass().getName();

	@Override
	public void init() {
		SingleRun s = CronSchedulerTask.this.getClass().getAnnotation(
				SingleRun.class);
		if (s != null) {
			String lockKey = CronSchedulerTask.this.getClass().getName();
			if (distributedLock.tryLock(lockKey, 0)) {
				submitTask();
			}
		} else {
			submitTask();
		}
		// 自动容灾，监控lock
		Watcher watcher = new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				EventType et = event.getType();
				switch (et) {
				case NodeDeleted:
					String period = configService.getConfig(configPath);
					if (StringUtils.isNotBlank(period)) {
						submitTask();
					}
					configService.addWatcher(configPath, this);
					break;
				default:
					break;
				}
			}

		};
		configService.addWatcher(lockNode, watcher);
	}

	private void submitTask() {
		JobDetail job = JobBuilder.newJob(this.getClass())
				.withIdentity(this.getClass().getName()).build();
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(triggerKey)
				.withSchedule(
						CronScheduleBuilder.cronSchedule(getCronExpression())
								.withMisfireHandlingInstructionFireAndProceed())
				.startAt(df.parseDateTime(getStartTime()).toDate()).build();
		try {
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
