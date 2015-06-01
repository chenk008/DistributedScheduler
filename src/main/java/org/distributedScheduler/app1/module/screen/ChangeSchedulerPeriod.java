package org.distributedScheduler.app1.module.screen;

import javax.annotation.Resource;

import org.distributedScheduler.biz.common.ResultMsg;
import org.distributedScheduler.biz.task.TaskFactory;

import com.alibaba.citrus.turbine.dataresolver.Param;

/**
 * localhost:8080/dataProcess/ChangeSchedulerPeriod.json?dataSourceFetcherType=org.distributedScheduler.biz.task.scheduler.impl.AlarmCountTask&period=10
 * 
 * 修改定时任务的周期
 * 
 * @author wuhua.ck
 *
 */
public class ChangeSchedulerPeriod {
	@Resource
	private TaskFactory taskFactory;

	public ResultMsg execute(
			@Param("dataSourceFetcherType") String dataSourceFetcherType,
			@Param("period") int period) {
		ResultMsg result = new ResultMsg();
		taskFactory.changeScheduleTask(dataSourceFetcherType, period);
		result.setSuccess(true);
		return result;
	}
}
