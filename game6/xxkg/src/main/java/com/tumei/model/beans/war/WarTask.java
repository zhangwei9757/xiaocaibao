package com.tumei.model.beans.war;

public class WarTask {
    public int tid;
    /**
     * 任务的id，和配置表的key一致
     *
     */
    public int task;

    // 任务类型, 和配置表中的mode一致
    public int mode;

    /**
     * 如果为限时任务，这个时间表示可以开始任务的最后时间
     * 只有这个时间之前，可以开始任务。
     * 如果为非限时任务，此值为0
     */
    public long expire;

    /**
     * 如果任务已经开始，这个时间表示任务结束的时间，可以收获。
     * 否则为0
     */
    public long complete;
}
