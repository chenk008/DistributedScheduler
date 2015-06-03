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

##例子：使用cron表达式来定义规则，org.distributedScheduler包下的CronSchedulerTask子类自动成为spring bean，可以获取spring中其他的bean

		@SingleRun //表示单机运行的标注
		public class CronTestTask extends CronSchedulerTask {

			//cron表达式
			@Override
			public String getCronExpression() {
				return "0/5 * * * * ? *";
			}

			//开始时间
			@Override
			public String getStartTime() {
				return "2015-05-01 00:00:00";
			}

			@Override
			public Map<String, Object> getData() {
				Map<String, Object> datas = new HashMap<String, Object>();
				datas.put("value", Integer.valueOf(1));
				String tmp = new DateTime().toString("yyyy-MM-dd HH:mm:ss")
						+ CronTestTask.class + "_" + datas;
				try {
					Thread.sleep(6000L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(tmp);
				return datas;
			}

		}