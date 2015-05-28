package org.distributedScheduler.biz.task.scheduler.impl;

import java.util.HashMap;
import java.util.Map;

import org.distributedScheduler.biz.task.annotation.SingleRun;
import org.distributedScheduler.biz.task.scheduler.PeriodSchedulerTask;

@SingleRun
public class AlarmCountTask extends PeriodSchedulerTask {

	@Override
	protected Map<String, Object> getData() {
		Map<String, Object> datas = new HashMap<String, Object>();
		datas.put("value", Integer.valueOf(1));
		return datas;
	}

	@Override
	protected String getStartTime() {
		return "2015-05-01 00:00:00";
	}

	@Override
	protected int getDefaultPeriod() {
		return 60;
	}
}
