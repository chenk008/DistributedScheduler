package org.distributedScheduler.util;

import java.io.UnsupportedEncodingException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZKUtils {

	public static void createNode(ZooKeeper zk, String path, String data,
			CreateMode createMode) throws UnsupportedEncodingException,
			KeeperException, InterruptedException {
		zk.create(path, path.getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, createMode);
	}

	public static String getNode(ZooKeeper zk, String path)
			throws UnsupportedEncodingException, KeeperException,
			InterruptedException {
		Stat stat = new Stat();
		byte[] data = zk.getData(path, false, stat);
		if (data != null) {
			return new String(data, "UTF-8");
		}
		return null;
	}

	public static Stat existsNode(ZooKeeper zk, String path)
			throws KeeperException, InterruptedException {
		return zk.exists(path, false);
	}

	public static Stat setData(ZooKeeper zk, String path, String data)
			throws UnsupportedEncodingException, KeeperException,
			InterruptedException {
		return zk.setData(path, data.getBytes("UTF-8"), -1);
	}
}
