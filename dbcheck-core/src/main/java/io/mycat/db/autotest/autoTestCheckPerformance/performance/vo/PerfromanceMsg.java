package io.mycat.db.autotest.autoTestCheckPerformance.performance.vo;

import java.util.List;

/**
 * Created by qiank on 2017/2/3.
 */
public class PerfromanceMsg {

    private String id;

    private String name;

    // 详情
    private String path;

    private List<Long> times;

    private long average;

    private long min;

    private long max;

    public PerfromanceMsg(String id, String name, String path, List<Long> times, long average, long min, long max) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.times = times;
        this.average = average;
        this.min = min;
        this.max = max;
    }

    public PerfromanceMsg() {
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public List<Long> getTimes() {
        return times;
    }

    public void setTimes(List<Long> times) {
        this.times = times;
    }

    public long getAverage() {
        return average;
    }

    public void setAverage(long average) {
        this.average = average;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
