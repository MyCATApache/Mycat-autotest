package io.mycat.db.autotest.autoTestCheckPerformance.check.vo;

/**
 * Created by qiank on 2017/2/3.
 */
public class CheckMsg {

    private String id;

    private String name;

    private String msg;

    // 详情
    private String path;

    private long time;

    private boolean stauts = true;

    public CheckMsg(String id, String name, String msg, String path, long time,boolean stauts) {
        this.id = id;
        this.name = name;
        this.msg = msg;
        this.path = path;
        this.time = time;
        this.stauts = stauts;
    }

    public CheckMsg() {
    }

    public boolean isStauts() {
        return stauts;
    }

    public void setStauts(boolean stauts) {
        this.stauts = stauts;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
