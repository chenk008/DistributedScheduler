package org.distributedScheduler.biz.task.cron;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
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
		startTask();
		// 自动容灾，监控lock
		SingleRun s = CronSchedulerTask.this.getClass().getAnnotation(
				SingleRun.class);
		if (s != null) {
			Watcher watcher = new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					EventType et = event.getType();
					switch (et) {
					case NodeDeleted:
						startTask();
						configService.addWatcher(lockNode, this);
						break;
					default:
						break;
					}
				}
			};
			configService.addWatcher(lockNode, watcher);
		}

		// 监听配置
		Watcher configWatcher = new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				EventType et = event.getType();
				switch (et) {
				case NodeCreated:
				case NodeDataChanged:
					// 判断当前是否已经在运行了
					SingleRun s = CronSchedulerTask.this.getClass()
							.getAnnotation(SingleRun.class);
					if (s != null) {
						String lockKey = CronSchedulerTask.this.getClass()
								.getName();
						if (distributedLock.tryLock(lockKey, 0)) {
							reScheduleJob();
							status = TaskStatus.RUNNING;
						} else {
							status = TaskStatus.LOCK_FAILED;
						}
					} else {
						reScheduleJob();
						status = TaskStatus.RUNNING;
					}
					configService.addWatcher(configPath, this);
					break;
				default:
					break;
				}
			}

		};
		configService.addWatcher(configPath, configWatcher);
	}

	private void startTask() {
		boolean needStart = false;
		SingleRun s = CronSchedulerTask.this.getClass().getAnnotation(
				SingleRun.class);
		if (s != null) {
			String lockKey = CronSchedulerTask.this.getClass().getName();
			if (distributedLock.tryLock(lockKey, 0)) {
				needStart = true;
				status = TaskStatus.RUNNING;
			} else {
				status = TaskStatus.LOCK_FAILED;
			}

		} else {
			needStart = true;
		}
		if (needStart) {
			JobDetail job = JobBuilder.newJob(this.getClass())
					.withIdentity(this.getClass().getName()).build();
			DateTimeFormatter df = DateTimeFormat
					.forPattern("yyyy-MM-dd HH:mm:ss");
			Trigger trigger = TriggerBuilder
					.newTrigger()
					.withIdentity(triggerKey)
					.withSchedule(
							CronScheduleBuilder
									.cronSchedule(getRealCronExpression())
									.withMisfireHandlingInstructionFireAndProceed())
					.startAt(df.parseDateTime(getStartTime()).toDate()).build();
			try {
				scheduler.scheduleJob(job, trigger);

			} catch (SchedulerException e) {
				e.printStackTrace();
				status = TaskStatus.STOP;
			}
		}
	}

	private void reScheduleJob() {
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		try {
			scheduler
					.rescheduleJob(
							triggerKey,
							TriggerBuilder
									.newTrigger()
									.withIdentity(triggerKey)
									.withSchedule(
											CronScheduleBuilder
													.cronSchedule(getRealCronExpression()))
									.startAt(
											df.parseDateTime(getStartTime())
													.toDate()).build());
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	// 优先获取zk上配置的cron表达式
	private String getRealCronExpression() {
		String cronExpression = null;
		try {
			String data = configService.getConfig(configPath);
			if (StringUtils.isBlank(data)) {
				cronExpression = getCronExpression();
			} else {
				cronExpression = data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cronExpression;
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

	@Override
	public boolean changePeriod(String period) {
		configService.putConfig(configPath, String.valueOf(period),
				CreateMode.PERSISTENT);
		return true;
	}

	public abstract String getCronExpression();

	public abstract String getStartTime();

	public abstract Map<String, Object> getData();
}
