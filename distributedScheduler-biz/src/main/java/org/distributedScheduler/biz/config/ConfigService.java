package org.distributedScheduler.biz.config;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

public interface ConfigService {

	public static final String PERIOD_CONFIG_PATH = "/distributedScheduler/period";

	String getConfig(String path);

	void putConfig(String path, String data, CreateMode createMode);

	void addWatcher(String path, Watcher watcher);
}
