# DistributedScheduler
分布式调度任务管理：

通常在分布式环境中，定时任务只需要在一台服务器上运行

## 基本配置

		bean id="taskSpringInitation"
			class="org.distributedScheduler.biz.task.TaskProcessSpringInitation"></bean>

		<bean id="taskFactory" class="org.distributedScheduler.biz.task.TaskFactory"
			init-method="start" destroy-method="shutdown"></bean>

		<bean id="distributedLock"
			class="org.distributedScheduler.biz.lock.impl.ZooKeeperDistributedLock"
			init-method="init">
		</bean>

		<bean id="zooKeeper" class="org.apache.zookeeper.ZooKeeper">
			<constructor-arg index="0">
				<value>localhost</value>
			</constructor-arg>
			<constructor-arg index="1">
				<value>2181</value>
			</constructor-arg>
			<constructor-arg index="2">
				<null />
			</constructor-arg>
		</bean>

		<bean id="configService"
			class="org.distributedScheduler.biz.config.impl.ConfigServiceImpl"
			init-method="init">
		</bean>
