package com.wtgroup.sugar.stopwatch;

import cn.hutool.core.date.DateUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static cn.hutool.core.date.BetweenFormater.Level.MILLISECOND;

/**
 * 速度统计器
 * <p>
 * 1> 指定间隔, 到了这个间隔, 输出一次日志.
 * 2> 指定事务次数间隔, 达到次数, 输出一次日志.
 * 3> 同时指定时间和次数, 满足其一, 输出一次日志.
 * 4> 给你当前时刻运行状态信息, 由你决定是否和怎么输出日志.
 *
 * <p>版本
 * == v1.0.0 2020年11月15日 dafei ==
 *
 * @author dafei
 * @version 0.1
 * @date 2020/11/15 22:46
 */
@Slf4j
@Getter
@Accessors(chain = true)
public class SpeedStator {

    /**
     * 开始时间戳
     */
    private long start;
    /**
     * 上次日志时的时间戳, 用于计算日志间隔
     */
    private long preTick;
    /**
     * 处理消耗的总时长
     */
    @Getter
    private long totalTime;
    /**
     * 上次日志时的已处理数
     */
    private long preHandledCount;
    /**
     * 当前总处理数
     */
    @Getter
    private long handledCount;
    /**
     * 区分日志的标记, 便于查看. 默认 "[this.getClass().getSimpleName()]"
     */
    // @Setter
    private final String tag;
    /**
     * 默认日志间隔(ms), 10s, 方法上间隔优先
     */
    private long defaultTimeInterval = Duration.ofSeconds(10).toMillis();
    /**
     * 默认日志次数间隔, 默认 100, 方法上的优先
     */
    private int defaultCountDelta = 100;

    private final BiConsumer<MomentInfo, String> logFunc;

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
    public synchronized void start() {
        this.start = this.preTick = System.currentTimeMillis();
        this.preHandledCount = this.handledCount = 0;
    }

    @SafeVarargs
    public final synchronized void logTimeInterval(Function<MomentInfo, String> ... extraMsg) {
        this.logTimeInterval(this.defaultTimeInterval, extraMsg);
    }

    /**
     * 按时间间隔输出日志
     *
     */
    @SafeVarargs
    public final synchronized void logTimeInterval(long timeInterval, Function<MomentInfo, String> ... extraMsg) {
        this.handledCount++;
        long now = System.currentTimeMillis();
        long realInterval = now - this.preTick;
        this.totalTime = now - this.start;
        long realDelta = this.handledCount - this.preHandledCount;
        if (realInterval >= timeInterval) {
            doLog(realDelta, realInterval, extraMsg);
            this.preTick = now;
            this.preHandledCount = this.handledCount;
        }
    }

    @SafeVarargs
    public final synchronized void logCountDelta(Function<MomentInfo, String> ... extraMsg) {
        this.logCountDelta(this.defaultCountDelta, extraMsg);
    }

    /**
     * 按事务次数间隔输出日志
     *
     */
    @SafeVarargs
    public final synchronized void logCountDelta(int countDelta, Function<MomentInfo, String> ... extraMsg) {
        this.handledCount++;
        long now = System.currentTimeMillis();
        long realInterval = now - this.preTick;
        this.totalTime = now - this.start;
        long realDelta = this.handledCount - this.preHandledCount;
        if (realDelta >= countDelta) {
            doLog(realDelta, realInterval, extraMsg);
            this.preTick = now;
            this.preHandledCount = this.handledCount;
        }
    }

    /**
     * 时间间隔和次数间隔满足其一, 就输出日志
     *
     * @param timeInterval
     * @param countDelta
     */
    @SafeVarargs
    public final synchronized void logOr(long timeInterval, int countDelta, Function<MomentInfo, String> ... extraMsg) {
        this.handledCount++;
        long now = System.currentTimeMillis();
        long realInterval = now - this.preTick;
        this.totalTime = now - this.start;
        long realDelta = this.handledCount - this.preHandledCount;
        if (realDelta >= countDelta || realInterval >= timeInterval) {
            doLog(realDelta, realInterval, extraMsg);
            this.preTick = now;
            this.preHandledCount = this.handledCount;
        }
    }

    /**
     * 快捷方式, 都用默认阈值
     *
     */
    @SafeVarargs
    public final synchronized void log(Function<MomentInfo, String> ... extraMsg) {
        this.logOr(this.defaultTimeInterval, this.defaultCountDelta, extraMsg);
    }

    @SafeVarargs
    public final synchronized void logIf(Predicate<MomentInfo> test, Function<MomentInfo, String> ... extraMsg) {
        this.handledCount++;
        long now = System.currentTimeMillis();
        long realInterval = now - this.preTick;
        this.totalTime = now - this.start;
        long realDelta = this.handledCount - this.preHandledCount;
        MomentInfo momentInfo = new MomentInfo();
        momentInfo.countDelta = realDelta;
        momentInfo.timeInterval = realInterval;
        if (test.test(momentInfo)) {
            doLog(realDelta, realInterval, extraMsg);
            this.preTick = now;
            this.preHandledCount = this.handledCount;
        }
    }


    /**
     * 执行日志输出
     * 不喜欢这里日志输出方式, 可以覆写.
     *
     * @param delta
     * @param realInterval
     * @param extraMsg
     */
    protected final void doLog(long delta, long realInterval, Function<MomentInfo, String> ... extraMsg) {
        // 最近间隔内 时长, 数量. 总时长, 总数量.
        MomentInfo momentInfo = new MomentInfo();
        momentInfo.countDelta = delta;
        momentInfo.timeInterval = realInterval;
        momentInfo.handledCount = this.handledCount;
        momentInfo.totalTime = this.totalTime;

        String extraMsgTxt = null;

        if (extraMsg != null && extraMsg.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (Function<MomentInfo, String> it : extraMsg) {
                if (it !=null) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(it.apply(momentInfo));
                }
            }
            extraMsgTxt = sb.toString();
        }


        if (this.logFunc == null) {
            double r = delta / (realInterval / 1000.0);
            double tr = this.handledCount / (this.totalTime / 1000.0);
            log.info("[{}] 最近 {} 条, 耗时 {}, [TPS] {}/秒. 已处理 {} 条, 耗时 {}, TPS {}/秒{}",
                    tag, delta, Duration.ofMillis(realInterval), String.format("%.3f", momentInfo.latestTps()), this.handledCount, Duration.ofMillis(this.totalTime), String.format("%.3f", momentInfo.totalTps()),
                    extraMsgTxt == null ? "" : "\n" + extraMsgTxt);
        }
        else {
            this.logFunc.accept(momentInfo, extraMsgTxt);
        }
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
