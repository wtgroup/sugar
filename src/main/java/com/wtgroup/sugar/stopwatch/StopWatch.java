package com.wtgroup.sugar.stopwatch;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wtgroup.sugar.collection.SoMap;
import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 秒表增强<br>
 *
 * <p>! 只能用于单线程串行 !
 * <p>
 * 版本<br>
 * == 2022年04月27日 L&J ==
 * TaskInfo 增加 data
 *
 * == v1.0.0 2020年11月17日 dafei ==
 * <li>优化时间输出, 友好话. 用 Duration 自带的格式化字符.</li>
 * <li>
 * 容忍连续 start, stop ; 容忍上次任务没有正常 stop; 容忍 stop 的任务没有被 start .
 * 总之, start 和 stop 不必一定要成对. 因为编码中难保出现纰漏, 不能因为这个无关紧要的计时功能导致程序崩溃.
 * </li>
 * </p>
 * <br>
 * <p>
 * 此工具用于存储一组任务的耗时时间，并一次性打印对比。<br>
 * 比如：我们可以记录多段代码耗时时间，然后一次性打印（StopWatch提供了一个prettyString()函数用于按照指定格式打印出耗时）
 *
 * <p>
 * 此工具来自：https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/util/StopWatch.java
 *
 * <p>
 * 使用方法如下：
 *
 * <pre>
 * StopWatch stopWatch = new StopWatch("任务名称");
 *
 * // 任务1
 * stopWatch.start("任务一");
 * Thread.sleep(1000);
 * stopWatch.stop();
 *
 * // 任务2
 * stopWatch.start("任务一");
 * Thread.sleep(2000);
 * stopWatch.stop();
 *
 * // 打印出耗时
 * Console.log(stopWatch.prettyPrint());
 *
 * </pre>
 *
 * 注: 不建议并发使用, 可能计时不准.
 * @author Spring Framework, Looly
 * @since 4.6.6
 */
@Slf4j
public class StopWatch {

	/**
	 * 秒表唯一标识，用于多个秒表对象的区分
	 */
	private final String id;
	private LinkedList<TaskInfo> taskList;

	/**
	 * 任务名称
	 */
	private String currentTaskName;
	/**
	 * 开始时间
	 */
	private long startTimeNanos;

	/**
	 * 最后一次任务对象
	 */
	private TaskInfo currentTaskInfo;
	/**
	 * 总任务数
	 */
	private int taskCount;
	/**
	 * 总运行时间
	 */
	private long totalTimeNanos;
	/**
	 * 保留最近的 task info 最大数量
	 *
	 * 高并发时, 最终实际任务数 >= 此数值
	 */
	private int maxTaskInfoSize = 1000;

	private final ReentrantLock lock = new ReentrantLock();

	// ------------------------------------------------------------------------------------------- Constructor start

	/**
	 * 构造，不启动任何任务
	 */
	public StopWatch() {
		this(StrUtil.EMPTY);
	}

	/**
	 * 构造，不启动任何任务
	 *
	 * @param id 用于标识秒表的唯一ID
	 */
	public StopWatch(String id) {
		this(id, true);
	}

	/**
	 * 构造，不启动任何任务
	 *
	 * @param id           用于标识秒表的唯一ID
	 * @param keepTaskList 是否在停止后保留任务，{@code false} 表示停止运行后不保留任务
	 */
	public StopWatch(String id, boolean keepTaskList) {
		this.id = id;
		if (keepTaskList) {
			this.taskList = new LinkedList<>();
		}
	}
	// ------------------------------------------------------------------------------------------- Constructor end

	/**
	 * 获取{@link StopWatch} 的ID，用于多个秒表对象的区分
	 *
	 * @return the ID 空字符串为
	 * @see #StopWatch(String)
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * 设置是否在停止后保留任务，{@code false} 表示停止运行后不保留任务
	 *
	 * @param keepTaskList 是否在停止后保留任务
	 */
	public StopWatch setKeepTaskList(boolean keepTaskList) {
		if (keepTaskList) {
			if (null == this.taskList) {
				this.taskList = new LinkedList<>();
			}
		} else {
			this.taskList = null;
		}

		return this;
	}

	public StopWatch setMaxTaskInfoSize(int n) {
		if (n <=0) {
			n = 1;
		}
		this.maxTaskInfoSize = n;

		return this;
	}

	/**
	 * 开始默认的新任务
	 *
	 * @throws IllegalStateException 前一个任务没有结束
	 */
	public void start() throws IllegalStateException {
		start(StrUtil.EMPTY);
	}

	/**
	 * 开始指定名称的新任务
	 *
	 * @param taskName 新开始的任务名称
	 * @throws IllegalStateException 前一个任务没有结束
	 */
	public void start(String taskName) throws IllegalStateException {
		// if (null != this.currentTaskName) {
		// 	throw new IllegalStateException("Can't start StopWatch: it's already running");
		// }

		if (taskName == null) {
			taskName = StrUtil.EMPTY;
		}
		// 连续 start, 可能是因为上次的因为异常(但被捕获了)没有执行, 总之, 因为意外, 没有执行 stop, 这里改为容忍
		// 1> 重复开启同一个 taskName , 忽略, return. 2> 不同 taskName , 上次任务强制认为结束了 .
		if (this.currentTaskName != null) {
			if (this.currentTaskName.equals(taskName)) {
				log.debug("`{}` already running, return.", taskName);
				return;
			} else {
				log.debug("previous task `{}` isn't stop normally, force to stop, then start current task `{}`", this.currentTaskName, taskName);
				this.stop();
			}
		}

		this.currentTaskName = taskName;
		this.startTimeNanos = System.nanoTime();
		this.currentTaskInfo = new TaskInfo(this.currentTaskName);
	}

	/**
	 * 停止当前任务
	 *
	 * @throws IllegalStateException 任务没有开始
	 */
	public void stop() throws IllegalStateException {
		stop(null);
	}

	public void stop(String message, Object ... args) throws IllegalStateException {
		if (null == this.currentTaskName) {
			// throw new IllegalStateException("Can't stop StopWatch: it's not running");
			log.warn("Can't stop StopWatch: it's not running");
			return;
		}

		final long lastTime = System.nanoTime() - this.startTimeNanos;
		this.totalTimeNanos += lastTime;
		this.currentTaskInfo.setTimeNanos(lastTime);
		this.currentTaskInfo.addMessage(message, args);

		if (null != this.taskList) {
			this.taskList.add(this.currentTaskInfo); // tt 不会被别人更改
			this.tryRemoveEarly();
		}
		++this.taskCount;
		this.currentTaskName = null;
	}

	public void addData(Map<String, Object> data) {
		if (isRunning()) {
			this.currentTaskInfo.addData(data);
		} else {
			log.warn("Can't add data to StopWatch: it's not running");
		}
	}

	public void addData(String key, Object value) {
		if (isRunning()) {
			this.currentTaskInfo.addData(key, value);
		} else {
			log.warn("Can't add data to StopWatch: it's not running");
		}
	}

	public void addMessage(String message, Object ... args) {
		if (isRunning()) {
			this.currentTaskInfo.addMessage(message, args);
		} else {
			log.warn("Can't add data to StopWatch: it's not running");
		}
	}

	/**删除超过长度限制的早期 task info, 并发下, 有个人完成就够了*/
	private void tryRemoveEarly() {
		if(!lock.tryLock()) {
			return;
		}
		try {
			int ext = this.taskList.size() - this.maxTaskInfoSize;
			if (ext >= 0) { // 并发下, 删除过程中, 别人增加了, 会删除"不干净"(容忍)
				for (int i = 0; i <= ext; i++) {
					this.taskList.removeFirst();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 检查是否有正在运行的任务
	 *
	 * @return 是否有正在运行的任务
	 * @see #currentTaskName()
	 */
	public boolean isRunning() {
		return (this.currentTaskName != null);
	}

	/**
	 * 获取当前任务名，{@code null} 表示无任务
	 *
	 * @return 当前任务名，{@code null} 表示无任务
	 * @see #isRunning()
	 */
	public String currentTaskName() {
		return this.currentTaskName;
	}

	/**
	 * 获取最后任务的花费时间（纳秒）
	 *
	 * @return 任务的花费时间（纳秒）
	 * @throws IllegalStateException 无任务
	 */
	public long getLastTaskTimeNanos() throws IllegalStateException {
		if (this.currentTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task interval");
		}
		return this.currentTaskInfo.getTimeNanos();
	}

	/**
	 * 获取最后任务的花费时间（毫秒）
	 *
	 * @return 任务的花费时间（毫秒）
	 * @throws IllegalStateException 无任务
	 */
	public long getLastTaskTimeMillis() throws IllegalStateException {
		if (this.currentTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task interval");
		}
		return this.currentTaskInfo.getTimeMillis();
	}

	/**
	 * 获取最后的任务名
	 *
	 * @return 任务名
	 * @throws IllegalStateException 无任务
	 */
	public String getLastTaskName() throws IllegalStateException {
		if (this.currentTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task name");
		}
		return this.currentTaskInfo.getTaskName();
	}

	/**
	 * 获取最后的任务对象
	 *
	 * @return {@link TaskInfo} 任务对象，包括任务名和花费时间
	 * @throws IllegalStateException 无任务
	 */
	public TaskInfo getLastTaskInfo() throws IllegalStateException {
		if (this.currentTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task info");
		}
		return this.currentTaskInfo;
	}

	/**
	 * 获取所有任务的总花费时间（纳秒）
	 *
	 * @return 所有任务的总花费时间（纳秒）
	 * @see #getTotalTimeMillis()
	 * @see #getTotalTimeSeconds()
	 */
	public long getTotalTimeNanos() {
		return this.totalTimeNanos;
	}

	/**
	 * 获取所有任务的总花费时间（毫秒）
	 *
	 * @return 所有任务的总花费时间（毫秒）
	 * @see #getTotalTimeNanos()
	 * @see #getTotalTimeSeconds()
	 */
	public long getTotalTimeMillis() {
		return DateUtil.nanosToMillis(this.totalTimeNanos);
	}

	/**
	 * 获取所有任务的总花费时间（秒）
	 *
	 * @return 所有任务的总花费时间（秒）
	 * @see #getTotalTimeNanos()
	 * @see #getTotalTimeMillis()
	 */
	public double getTotalTimeSeconds() {
		return DateUtil.nanosToSeconds(this.totalTimeNanos);
	}

	/**
	 * 获取任务数
	 *
	 * @return 任务数
	 */
	public int getTaskCount() {
		return this.taskCount;
	}

	/**
	 * 获取任务列表
	 *
	 * @return 任务列表
	 */
	public TaskInfo[] getTaskInfo() {
		if (null == this.taskList) {
			throw new UnsupportedOperationException("Task info is not being kept!");
		}
		return this.taskList.toArray(new TaskInfo[0]);
	}

	/**
	 * 获取任务信息
	 *
	 * @return 任务信息
	 */
	public String shortSummary() {
		return StrUtil.format("StopWatch '{}': running time = {}", this.id, this.nanos2Duration(this.totalTimeNanos));
	}

	/**
	 * 生成所有任务的一个任务花费时间表
	 *
	 * @return 任务时间表
	 */
	public String prettyString() {
		StringBuilder sb = new StringBuilder(shortSummary());
		sb.append(FileUtil.getLineSeparator());
		if (null == this.taskList) {
			sb.append("No task info kept");
		} else {
			sb.append("---------------------------------------------").append(FileUtil.getLineSeparator());
			sb.append("ns         %     Task name         *").append(FileUtil.getLineSeparator());
			sb.append("---------------------------------------------").append(FileUtil.getLineSeparator());

			// final NumberFormat nf = NumberFormat.getNumberInstance();
			// nf.setMinimumIntegerDigits(9);
			// nf.setGroupingUsed(false);

			final NumberFormat pf = NumberFormat.getPercentInstance();
			pf.setMinimumIntegerDigits(3);
			pf.setGroupingUsed(false);
			for (TaskInfo task : getTaskInfo()) {
				sb.append(this.nanos2Duration(task.getTimeNanos())).append("  ");
				sb.append(pf.format((double) task.getTimeNanos() / getTotalTimeNanos())).append("  ");
				sb.append("[").append(task.getTaskName()).append("]");
				if (task.hasMessage()) {
					sb.append("  ").append(task.messagePrint());
				}
				sb.append(FileUtil.getLineSeparator());
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(shortSummary());
		if (null != this.taskList) {
			for (TaskInfo task : this.taskList) {
				sb.append("; [").append(task.getTaskName()).append("] took ").append(this.nanos2Duration(task.getTimeNanos()));
				long percent = Math.round(100.0 * task.getTimeNanos() / getTotalTimeNanos());
				sb.append(" = ").append(percent).append("%");
				if (task.hasMessage()) {
					sb.append(", ").append(task.messagePrint());
				}
			}
		} else {
			sb.append("; no task info kept");
		}
		return sb.toString();
	}

	private Duration nanos2Duration(long nanos) {
		return Duration.ofNanos(nanos);
	}

	/**
	 * 存放任务名称和花费时间对象
	 *
	 * @author Looly
	 */
	public static final class TaskInfo {

		private final String taskName;
		private long timeNanos;
		/**
		 * 额外信息 (结构化)
		 */
		private Map<String, Object> data;
		/**
		 * 额外信息 (非结构化)
		 */
		private String message;

		TaskInfo(String taskName) {
			this(taskName, 0L);
		}

		TaskInfo(String taskName, long timeNanos) {
			this.taskName = taskName;
			this.timeNanos = timeNanos;
		}

		/**
		 * 获取任务名
		 *
		 * @return 任务名
		 */
		public String getTaskName() {
			return this.taskName;
		}

		/**
		 * 获取任务花费时间（单位：纳秒）
		 *
		 * @return 任务花费时间（单位：纳秒）
		 * @see #getTimeMillis()
		 * @see #getTimeSeconds()
		 */
		public long getTimeNanos() {
			return this.timeNanos;
		}

		public void setTimeNanos(long timeNanos) {
			this.timeNanos = timeNanos;
		}

		/**
		 * 获取任务花费时间（单位：毫秒）
		 *
		 * @return 任务花费时间（单位：毫秒）
		 * @see #getTimeNanos()
		 * @see #getTimeSeconds()
		 */
		public long getTimeMillis() {
			return DateUtil.nanosToMillis(this.timeNanos);
		}

		/**
		 * 获取任务花费时间（单位：秒）
		 *
		 * @return 任务花费时间（单位：秒）
		 * @see #getTimeMillis()
		 * @see #getTimeNanos()
		 */
		public double getTimeSeconds() {
			return DateUtil.nanosToSeconds(this.timeNanos);
		}


		public Map<String, Object> getData() {
			return this.data;
		}

		public String getMessage() {
			return this.message;
	}

		public void addData(Map<String, Object> data) {
			if (this.data == null) {
				this.data = data;
			} else {
				this.data.putAll(data);
}

			// String kvPair = getKvPair(data);
			// if (kvPair == null) {
			// 	return;
			// }
			// this.data = this.data == null ? kvPair : this.data + kvPair;
		}

		public void addData(String key, Object value) {
			if (this.data == null) {
				this.data = SoMap.of(key, value);
			} else {
				this.data.put(key, value);
			}
		}

		public void addMessage(String message, Object ... args) {
			if (message == null) {
				return;
			}
			String fmt = StrUtil.format(message, args);
			if (StrUtil.isBlank(this.message)) {
				this.message = fmt;
			} else {
				this.message += " | " + fmt;
			}
		}

		public String messagePrint() {
			String res = "";
			if (this.message != null) {
				res += this.message;
			}
			if (this.data != null && this.data.size() > 0) {
				res += (res.length() == 0 ? this.data : "  " + this.data);
			}
			return res;
		}

		public boolean hasMessage() {
			return this.message != null || this.data != null;
		}
	}
}