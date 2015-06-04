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
import org.distributedScheduler.biz.util.ZKUtils;

public class ZooKeeperDistributedLock implements DistributedLock {

	public static final String LOCK_PATH = "/distributedScheduler/lock";

	@Resource
	private ZooKeeper zooKeeper;

	public void init() throws Exception {
		if (zooKeeper.exists(LOCK_PATH, false) == null) {
			ZKUtils.createNodePath(zooKeeper, LOCK_PATH);
		}
	}

	@Override
	public boolean tryLock(String lockKey, long expireTime) {
		String data = null;
		try {
			data = ZKUtils.getNode(zooKeeper, LOCK_PATH + "/" + lockKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (StringUtils.equals(getMessage(), data)) {
				return true;
			}
			String result = zooKeeper.create(LOCK_PATH + "/" + lockKey,
					getMessage().getBytes(), Ids.OPEN_ACL_UNSAFE,
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
			zooKeeper.delete(LOCK_PATH + "/" + lockKey, -1);
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

}
