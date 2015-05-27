package org.distributedScheduler.biz.dataFetecher;

public enum DataSourceFetcherType {
	ALARM_COUNT("告警数"), XRULE_WIRELESS("XRule无线规则"), POSTMORTEM_EVENT(
			"事件平台"), XRULE_BALANCE("XRule瓦力");

	private String desc;

	private DataSourceFetcherType(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}