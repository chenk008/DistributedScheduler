package org.distributedScheduler.biz.task.scheduler;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.distributedScheduler.biz.config.ConfigService;
import org.distributedScheduler.biz.lock.DistributedLock;
import org.distributedScheduler.biz.task.Task;
import org.distributedScheduler.biz.task.annotation.SingleRun;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;

/**
 * 主动获取的数据源
 * 
 *
 */
public abstract class PeriodSchedulerTask implements Task {
	private static final Logger logger = LoggerFactory
			.getLogger(PeriodSchedulerTask.class);

	protected static ScheduledExecutorService POOL = Executors
			.newScheduledThreadPool(10);

	private ScheduledFuture<?> future;

	@Resource
	private DistributedLock distributedLock;

	@Resource
	private ConfigService configService;

	private TaskStatus status = TaskStatus.INIT;

	private String configPath = ConfigService.PERIOD_CONFIG_PATH + "/"
			+ this.getClass().getName();

	@Override
	public void init() {
		SingleRun s = PeriodSchedulerTask.this.getClass().getAnnotation(
				SingleRun.class);
		if (s != null) {
			try {
				String lockKey = PeriodSchedulerTask.this.getClass().getName();
				int period = getPeriod();
				int expireTime = period - 1;
				if (expireTime <= 0) {
					expireTime = 1;
				}
				if (distributedLock.tryLock(lockKey, expireTime)) {
					submitTask(period);
					status = TaskStatus.RUNNING;
				} else {
					status = TaskStatus.LOCK_FAILED;
				}
			} catch (Exception e) {
				// 加锁的发生异常
				e.printStackTrace();
			} finally {
			}
		} else {
			submitTask(getPeriod());
			status = TaskStatus.RUNNING;
		}
		Watcher watcher = new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				EventType et = event.getType();
				switch (et) {
				case NodeCreated:
				case NodeDataChanged:
					String period = configService.getConfig(event.getPath());
					if (StringUtils.isNotBlank(period)) {
						applyPeriod(Integer.parseInt(period));
					}
					configService.addWatcher(configPath, this);
					break;
				default:
					break;
				}
			}

		};
		configService.addWatcher(configPath, watcher);
	}

	private class WorkTask implements Runnable {

		@Override
		public void run() {
			if (Thread.interrupted()) {
				return;
			}
			Map<String, Object> params = getData();
			System.out.println(params);
		}

	}

	@Override
	public void shutdown() {
		if (future != null) {
			future.cancel(true);
		}
		SingleRun s = this.getClass().getAnnotation(SingleRun.class);
		if (s != null) {
			distributedLock.unlock(this.getClass().getName());
		}
		status = TaskStatus.STOP;
	}

	@Override
	public TaskStatus getStatus() {
		return status;
	}

	public static void shutdonwPool() {
		POOL.shutdown();
	}

	private int getPeriod() {
		int period = 0;
		try {
			String data = configService.getConfig(configPath);
			if (StringUtils.isBlank(data)) {
				period = getDefaultPeriod();
			} else {
				period = Integer.valueOf(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return period;
	}

	public boolean changePeriod(int period) {
		configService.putConfig(configPath, String.valueOf(period),
				CreateMode.PERSISTENT);
		return true;
	}

	private void applyPeriod(int period) {
		if (period > 0) {
			if (future != null) {
				future.cancel(true);
			}

			// 等到老任务完结
			while (!future.isCancelled()) {

			}
			submitTask(period);
		}
	}

	private void submitTask(int period) {
		DateTimeFormatter format = DateTimeFormat
				.forPattern("yyyy-MM-dd HH:mm:ss");
		long start = format.parseDateTime(getStartTime()).getMillis();
		long now = System.currentTimeMillis();
		long initialDelay = 0L;
		long realPeriod = period * 1000L;
		if (start > now) {
			initialDelay = start - now;
		} else {
			long multiple = (now - start) / realPeriod;
			start += multiple * realPeriod;
			if (start < now) {
				start += realPeriod;
			}
			initialDelay = start - now;
		}
		logger.error(this.getClass().getName()
				+ ":startTime"
				+ new DateTime(now + initialDelay)
						.toString("yyyy-MM-dd HH:mm:ss"));
		try {
			future = POOL.scheduleAtFixedRate(new WorkTask(), initialDelay,
					realPeriod, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor  did not accept task: "
					+ this.getClass().getName(), ex);
		}
	}

	/**
	 * 获取数据的方法
	 * 
	 * @return
	 */
	protected abstract Map<String, Object> getData();

	/**
	 * 起始时间：yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	protected abstract String getStartTime();

	/**
	 * 执行周期
	 * 
	 * @return
	 */
	protected abstract int getDefaultPeriod();

}
