package org.distributedScheduler.biz.task.cron.impl;

import java.util.HashMap;
import java.util.Map;

import org.distributedScheduler.biz.task.cron.CronSchedulerTask;
import org.joda.time.DateTime;

public class CronTestTask extends CronSchedulerTask {

	@Override
	public String getCronExpression() {
		return "0/5 * * * * ? *";
	}

	@Override
	public String getStartTime() {
		return "2015-05-01 00:00:00";
	}

	@Override
	public Map<String, Object> getData() {
		Map<String, Object> datas = new HashMap<String, Object>();
		datas.put("value", Integer.valueOf(1));
		String tmp = new DateTime().toString("yyyy-MM-dd HH:mm:ss")
				+ CronTestTask.class + "_" + datas;
		try {
			Thread.sleep(6000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(tmp);
		return datas;
	}

}
