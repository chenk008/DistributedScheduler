package org.distributedScheduler.app1.module.screen;

import javax.annotation.Resource;

import org.distributedScheduler.biz.common.ResultMsg;
import org.distributedScheduler.biz.task.TaskFactory;

import com.alibaba.citrus.turbine.dataresolver.Param;

/**
 * localhost:8080/ReScheduleCronTask.json?taskType=
 * org.distributedScheduler.biz.task.cron.impl.CronTestTask&cronExpression=10
 * 
 * 修改定时任务的周期
 * 
 * @author wuhua.ck
 *
 */
public class ReScheduleCronTask {
	@Resource
	private TaskFactory taskFactory;

	public ResultMsg execute(@Param("taskType") String taskType,
			@Param("cronExpression") String cronExpression) {
		ResultMsg result = new ResultMsg();
		taskFactory.reScheduleCronTask(taskType, cronExpression);
		result.setSuccess(true);
		return result;
	}
}