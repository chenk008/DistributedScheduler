package org.distributedScheduler.biz.config.impl;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.distributedScheduler.biz.config.ConfigService;
import org.distributedScheduler.util.ZKUtils;

public class ConfigServiceImpl implements ConfigService {

	@Resource
	private ZooKeeper zooKeeper;

	public void init() throws Exception {
		if (zooKeeper.exists(ConfigService.PERIOD_CONFIG_PATH, false) == null) {
			ZKUtils.createNodePath(zooKeeper, ConfigService.PERIOD_CONFIG_PATH);
		}
	}

	@Override
	public String getConfig(String path) {
		try {
			return ZKUtils.getNode(zooKeeper, path);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void putConfig(String path, String data, CreateMode createMode) {
		try {
			if (ZKUtils.existsNode(zooKeeper, path) != null) {
				ZKUtils.setData(zooKeeper, path, data);
			} else {
				ZKUtils.createNode(zooKeeper, path, data, createMode);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addWatcher(String path, Watcher watcher) {
		try {
			ZKUtils.addWatcher(zooKeeper, path, watcher);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
