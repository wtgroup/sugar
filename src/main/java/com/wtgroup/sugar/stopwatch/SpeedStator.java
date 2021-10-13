package com.wtgroup.sugar.stopwatch;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static cn.hutool.core.date.BetweenFormatter.Level.MILLISECOND;

/**
 * 速度统计和带限流的日志
 * <p>
 * 1> 指定间隔, 到了这个间隔, 输出一次日志.
 * 2> 指定事务次数间隔, 达到次数, 输出一次日志.
 * 3> 同时指定时间和次数, 满足其一, 输出一次日志.
 * 4> 给你当前时刻运行状态信息, 由你决定是否和怎么输出日志.
 *
 * <p>版本
 * == 2021年10月4日 ==
 * 去掉 synchronized, 改为 tryLock, 竞争发生时, 没获锁的线程就不输出. 数量增减原子操作.
 * 计数每个人都要求准确计入, 但打印这件事, 只要有个人去做了就可以了.
 *
 * == v1.0.0 2020年11月15日 dafei ==
 *
 * @author dafei
 * @version 0.1
 * @date 2020/11/15 22:46
 */
@Slf4j
public class SpeedStator {

    /**
     * 开始时间戳
     */
    @Getter
    private long start;
    /**
     * 上次日志时的时间戳, 用于计算日志间隔
     */
    @Getter
    private long preTick;
    /**
     * 处理消耗的总时长
     */
    private long totalTime;
    /**
     * 上次日志时的已处理数
     */
    @Getter
    private long preHandledCount;
    /**
     * 当前总处理数
     * 如果您需要统计总事务数, 可以所有事务结束后, 取这个值, 它是准确的, 而不用另外搞一个计数器.
     */
    private volatile AtomicLong handledCount = new AtomicLong();

    /**
     * 区分日志的标记, 便于查看. 默认 "[this.getClass().getSimpleName()]"
     */
    @Getter
    private final String tag;
    /**
     * 默认日志间隔(ms), 10s, 方法上间隔优先
     */
    private long defaultTimeInterval = Duration.ofSeconds(10).toMillis();
    /**
     * 默认日志次数间隔, 默认 100, 方法上的优先
     */
    private int defaultCountDelta = 100;
    /**
     * P1: 当前日志时点距离上一个日志时点已处理的条数和时间消耗
     * P2: 额外消息格式解析后的文本
     */
    private final BiConsumer<MomentInfo, String> logFunc;
    @Getter
    private volatile boolean running = false;

    private final ReentrantLock lock = new ReentrantLock();

    public SpeedStator() {
        this(null);
    }

    public SpeedStator(String tag) {
        this(tag, null);
    }

    public SpeedStator(String tag, BiConsumer<MomentInfo, String> logFunc) {
        this.tag = tag == null ? this.getClass().getSimpleName() : tag;
        this.logFunc = logFunc;

        this.start(); // 如果不是以构造时间点为 start, 可以在后续再次调用一下 start()
    }

    public SpeedStator setDefaultTimeInterval(long defaultTimeInterval) {
        this.defaultTimeInterval = defaultTimeInterval;
        return this;
    }

    public SpeedStator setDefaultCountDelta(int defaultCountDelta) {
        this.defaultCountDelta = defaultCountDelta;
        return this;
    }

    /**
     * 开始
     * <p>时间, 数量数据重置.
     * 如果不是以构造时间点为 start, 需要再次调用一下 start(), 否则, 不用.
     */
    public void start() {
        if (running) return;
        if (lock.tryLock()) {
            this.start = this.preTick = System.currentTimeMillis();
            this.preHandledCount = 0;
            this.handledCount.set(0);
            this.running = true;
            lock.unlock();
        }
    }

    /**
     * 标记结束, 打印全局平均速率.
     * 请确保在所有事务的确结束了才调用, 避免误差.
     */
    public void stop() {
        if(!running) return;
        if (!lock.tryLock()) return;
        long totalTime = this.getTotalTime();
        log.info("[{}] STOP: 共处理 {} 条, 耗时 {}, TPS {}/秒",
                tag, this.handledCount.get(), Duration.ofMillis(totalTime), String.format("%.3f", this.handledCount.get() / (totalTime / 1000.0)));
        this.running = false;
        lock.unlock();
    }

    public long getHandledCount() {
        return this.handledCount.get();
    }

    /**仅仅计数. 注意不要和 logxxx 在一条消息里同时使用, 因为 logxxx 本身会计数, 从而导致计数不准确.
     * @param incr 增量
     */
    public long count(int incr) {
        return this.handledCount.addAndGet(incr);
    }

    public void logTimeInterval(Object ... extraMsg_Args) {
        this.logTimeInterval(this.defaultTimeInterval, extraMsg_Args);
    }

    /**
     * 按时间间隔输出日志
     *
     */
    public void logTimeInterval(long timeInterval, Object ... extraMsg_Args) {
        this.handledCount.getAndIncrement();

        this.doLogSync((momentInfo -> momentInfo.timeInterval >= timeInterval), extraMsg_Args);
    }

    public void logCountDelta(Object ... extraMsg_Args) {
        this.logCountDelta(this.defaultCountDelta, extraMsg_Args);
    }

    /**
     * 按事务次数间隔输出日志
     *
     */
    public void logCountDelta(int countDelta, Object ... extraMsg_Args) {
        this.handledCount.getAndIncrement();

        this.doLogSync((momentInfo -> momentInfo.countDelta >= countDelta), extraMsg_Args);
    }

    public void logOr(Object ... extraMsg_Args) {
        this.logOr(this.defaultTimeInterval, this.defaultCountDelta, extraMsg_Args);
    }

    /**
     * 时间间隔和次数间隔满足其一, 就输出日志
     *
     * @param timeInterval
     * @param countDelta
     */
    public void logOr(long timeInterval, int countDelta, Object ... extraMsg_Args) {
        this.handledCount.getAndIncrement();

        this.doLogSync((momentInfo -> momentInfo.timeInterval >= timeInterval || momentInfo.countDelta >= countDelta), extraMsg_Args);
    }

    public  void logAnd(Object ... extraMsg_Args) {
        this.logAnd(this.defaultTimeInterval, this.defaultCountDelta, extraMsg_Args);
    }

    /**
     * 时间间隔和次数间隔都满足, 才输出
     * @param timeInterval
     * @param countDelta
     * @param extraMsg_Args 额外消息内容, 消息模板+占位符的参数
     */
    public void logAnd(long timeInterval, int countDelta, Object ... extraMsg_Args) {
        this.handledCount.getAndIncrement();

        this.doLogSync((momentInfo -> momentInfo.timeInterval >= timeInterval && momentInfo.countDelta >= countDelta), extraMsg_Args);
    }

    /**
     * 快捷方式, 都用默认阈值
     * {@code this.doLogSync((momentInfo -> momentInfo.timeInterval >= timeInterval))} 的快捷方式.
     */
    public void log(Object ... extraMsg_Args) {
        this.logOr(extraMsg_Args);
    }

    public void logIf(Predicate<MomentInfo> test, Object ... extraMsg_Args) {
        this.handledCount.getAndIncrement();

        this.doLogSync(test, extraMsg_Args);
    }

    private void doLogSync(Predicate<MomentInfo> test, Object ... extraMsg_Args) {
        if (!lock.tryLock()) return;

        try {
            long now = System.currentTimeMillis();
            this.totalTime = now - this.start;

            // 最近间隔内 时长, 数量. 总时长, 总数量.
            MomentInfo momentInfo = new MomentInfo();
            momentInfo.countDelta = this.handledCount.get() - this.preHandledCount;
            momentInfo.timeInterval = now - this.preTick;
            momentInfo.handledCount = this.handledCount.get();
            momentInfo.totalTime = this.totalTime;

            if (test.test(momentInfo)) {
                doLog(momentInfo, extraMsg_Args);
                this.preTick = now;
                this.preHandledCount = this.handledCount.get();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 执行日志输出
     * 不喜欢这里日志输出方式, 可以覆写.
     *
     */
    protected void doLog(MomentInfo momentInfo, Object ... extraMsg_Args) {

        String extraMsgTxt = null;

        if (extraMsg_Args != null && extraMsg_Args.length > 0) {
            if (extraMsg_Args[0] instanceof CharSequence) {
                Object[] args = Arrays.copyOfRange(extraMsg_Args, 1, extraMsg_Args.length);
                extraMsgTxt = StrUtil.format((CharSequence) extraMsg_Args[0], args);
            } else {
                extraMsgTxt = Arrays.toString(extraMsg_Args);
            }
        }

        if (this.logFunc == null) {
            log.info("[{}] 最近 {} 条, 耗时 {}, [TPS] {}/秒. 已处理 {} 条, 耗时 {}, TPS {}/秒{}",
                    tag, momentInfo.countDelta, Duration.ofMillis(momentInfo.timeInterval), String.format("%.3f", momentInfo.latestTps()), this.handledCount, Duration.ofMillis(this.totalTime), String.format("%.3f", momentInfo.totalTps()),
                    extraMsgTxt == null ? "" : "\n" + extraMsgTxt);
        }
        else {
            this.logFunc.accept(momentInfo, extraMsgTxt);
        }
    }

    public long getTotalTime() {
        return this.totalTime = System.currentTimeMillis() - this.start;
    }


    // ---- 查询实时速率 ---- //

    /**
     * 实时全局平均速率
     */
    public double getSpeed() {
        return (double) this.getHandledCount() / this.getTotalTime();
    }

    /**
     * 实时最近速率, 上次日志点开始到现在的速率
     */
    public double getLatestSpeed() {
        long dur = System.currentTimeMillis() - this.preTick;
        long c = this.getHandledCount() - this.preHandledCount;
        return (double) c / dur;
    }

    /**
     * 实时 MomentInfo , 上次日志时点开始到现在
     */
    public MomentInfo getLatestMoment() {
        long dur = System.currentTimeMillis() - this.preTick;
        long c = this.getHandledCount() - this.preHandledCount;
        MomentInfo momentInfo = new MomentInfo();
        momentInfo.countDelta = c;
        momentInfo.timeInterval = dur;
        momentInfo.handledCount = this.getHandledCount();
        momentInfo.totalTime = this.getTotalTime();
        return momentInfo;
    }


    public static class MomentInfo {
        public long countDelta;
        public long timeInterval;
        /**
         * 当前总处理数
         */
        private long handledCount;
        /**
         * 处理消耗的总时长
         */
        private long totalTime;

        // 最近的速度, 条/秒
        public double latestTps() {
            return this.countDelta / (this.timeInterval / 1000.0);
        }

        public double totalTps() {
            return this.handledCount / (this.totalTime / 1000.0);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("countDelta", countDelta)
                    .append("timeInterval", DateUtil.formatBetween(timeInterval, MILLISECOND))
                    .append("handledCount", handledCount)
                    .append("totalTime", DateUtil.formatBetween( totalTime, MILLISECOND))
                    .append("latestTps", String.format("%.3f", latestTps()))
                    .append("totalTps", String.format("%.3f", totalTps()))
                    .toString();
        }
    }
}
