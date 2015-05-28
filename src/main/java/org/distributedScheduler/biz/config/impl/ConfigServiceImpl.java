package org.distributedScheduler.biz.config.impl;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.distributedScheduler.biz.config.ConfigService;
import org.distributedScheduler.util.ZKUtils;

public class ConfigServiceImpl implements ConfigService {

	@Resource
	private ZooKeeper zooKeeper;

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
	public void putConfig(String path, String data) {
		try {
			if (ZKUtils.existsNode(zooKeeper, path) == null) {
				ZKUtils.setData(zooKeeper, path, data);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
