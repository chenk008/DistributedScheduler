package org.distributedScheduler.biz.task.scheduler.impl;

import java.util.HashMap;
import java.util.Map;

import org.distributedScheduler.biz.task.annotation.SingleRun;
import org.distributedScheduler.biz.task.scheduler.PeriodSchedulerDataSourceFetcher;

@SingleRun
public class AlarmCountDataSourceFetcher extends
		PeriodSchedulerDataSourceFetcher {

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

	@Override
	public Map<String, String> getDataSourceResultTypes() {
		Map<String, String> resultTypes = new HashMap<String, String>();
		resultTypes.put("value", "告警数");
		return resultTypes;
	}

}
