package org.distributedScheduler.lock.impl;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.distributedScheduler.lock.DistributedLock;

public class ZooKeeperDistributedLock implements DistributedLock {

	private ZooKeeper zk;

	public ZooKeeperDistributedLock() throws Exception {
		zk = new ZooKeeper("localhost", 2181, new Watcher() {

			@Override
			public void process(WatchedEvent event) {

			}

		});
		if (zk.exists("/distributedScheduler/lock", false) == null) {
			createNode("/distributedScheduler/lock");
		}
	}

	@Override
	public boolean tryLock(String lockKey, long expireTime) {
		try {
			String result = zk.create("/distributedScheduler/lock/" + lockKey,
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
			zk.delete("/distributedScheduler/lock/" + lockKey, -1);
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
				if (zk.exists(path, false) == null) {
					zk.create(path, path.getBytes(), Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}
			}
		}
	}
}
