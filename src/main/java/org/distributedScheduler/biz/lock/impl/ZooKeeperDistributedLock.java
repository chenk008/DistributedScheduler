package org.distributedScheduler.biz.lock.impl;

import java.io.IOException;
import java.net.InetAddress;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.distributedScheduler.biz.lock.DistributedLock;
import org.distributedScheduler.util.ZKUtils;

public class ZooKeeperDistributedLock implements DistributedLock {

	@Resource
	private ZooKeeper zooKeeper;

	public void init() throws Exception {
		if (zooKeeper.exists("/distributedScheduler/lock", false) == null) {
			createNode("/distributedScheduler/lock");
		}
	}

	@Override
	public boolean tryLock(String lockKey, long expireTime) {
		try {
			String result = zooKeeper.create("/distributedScheduler/lock/"
					+ lockKey, getMessage().getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
			System.out.println(result);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void unlock(String lockKey) {
		try {
			zooKeeper.delete("/distributedScheduler/lock/" + lockKey, -1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	private String getMessage() throws IOException {
		// return InetAddress.getLocalHost().getHostName() + ":"
		// + Thread.currentThread().getName();
		return InetAddress.getLocalHost().getHostName();
	}

	private void createNode(String nodePath) throws Exception {
		String[] nodes = StringUtils.split(nodePath, "/");
		String path = "";
		if (nodes != null) {
			for (String node : nodes) {
				path = path + "/" + node;
				if (ZKUtils.existsNode(zooKeeper, path) == null) {
					ZKUtils.createNode(zooKeeper, path, path,
							CreateMode.PERSISTENT);
				}
			}
		}
	}
}
