package org.distributedScheduler.biz.config;

public interface ConfigService {

	String getConfig(String path);

	void putConfig(String path, String data);
}
