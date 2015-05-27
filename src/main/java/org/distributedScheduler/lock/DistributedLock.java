package org.distributedScheduler.lock;

public interface DistributedLock {

	boolean tryLock(String lockKey, long expireTime);

	void unlock(String lockKey);
}
