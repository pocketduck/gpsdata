package com.chexiao.base.dao.monitor;

/**
 * Created by fulei on 2016-12-15.
 */
public class MonitorParameter {
    //最大执行时间
    private int maxts = 300;
    //最大返回行数
    private int maxrows = 100;
    //监控时间间隔
    private int monitorInterval = 60;


    public int getMaxts() {
        return maxts;
    }


    public void setMaxts(int maxts) {
        this.maxts = maxts;
    }


    public int getMaxrows() {
        return maxrows;
    }


    public void setMaxrows(int maxrows) {
        this.maxrows = maxrows;
    }


    public int getMonitorInterval() {
        return monitorInterval;
    }


    public void setMonitorInterval(int monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

    private static class SingletonHandler {
        private static MonitorParameter obj= new MonitorParameter();

    }

    public static MonitorParameter getInstance() {
        return SingletonHandler.obj;
    }
}
