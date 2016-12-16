package com.chexiao.base.dbconnectionpool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 *  监控DbDatasource的事件，根据事件决定判断数据库的死活。
 */
public class DbStateMonitor
        implements DbMonitor, LifeCycle {

    DbDataSource dataSource = null;

    AtomicLong idleLiveCount = new AtomicLong(60000);

    private static Logger logger = LoggerFactory.getLogger(DbMonitor.class);


    int expectCount = 12; // 1 min
    boolean emergency =false;

    void execute() {
        if (dataSource == null) return;

        long count = idleLiveCount.incrementAndGet();
        logger.debug(dataSource.getName()+"调用次数："+count);
        if (count < 0){
            idleLiveCount.set(0);
        }

        if (dataSource.isShutDown)
            return;

        if (count < expectCount && (dataSource.isAlive())) return;


        logger.debug("检测数据库连接状态："+dataSource.getName());
        dataSource.checkLive();
        // 如果数据库是活着的，并且不是紧急状态 (紧急状态就是有connection 被kill掉了)则将计数器复位，否则下次还需检查数据库
        if (dataSource.isAlive()&&!emergency){

            idleLiveCount.set(0);
        }else{
            idleLiveCount.set(12);//如果为紧急状态 则直接进行下一次check
        }

    }

    Runnable command = new Runnable() {
        @Override
        public void run() {
            execute();

            checking = false;
        }
    };

    volatile boolean checking = false;

    @Override
    public void check() {
        if (checking) return;
        checking = true;

//		execute();
        LifeCycle.Dispatcher.singleton.executor.execute(command);


    }

    @Override
    public void onCreate(DbDataSource ds, ConnectionWrapper connection) {
    }

    @Override
    public void onDestroy(DbDataSource ds, ConnectionWrapper connection) {
    }

    @Override
    public void onCheckIn(DbDataSource ds, ConnectionWrapper connection) {
    }

    @Override
    public void onCheckOut(DbDataSource ds, ConnectionWrapper connection) {
    }

    @Override
    public void onException(DbDataSource ds, ConnectionWrapper connection,
                            Throwable t) {
        idleLiveCount.getAndAdd(2);
    }

    @Override
    public void onBroken(DbDataSource ds, ConnectionWrapper connection) {
        idleLiveCount.getAndAdd(4);
        this.emergency=true;
        logger.debug("connection has broken!");
    }

    @Override
    public void onBound(DbDataSource ds) {
        this.dataSource = ds;
        LifeCycle.Dispatcher.singleton.register(this);
//		checkLiveTask.setDaemon(true);
//		checkLiveTask.start();
    }

    @Override
    public void onShutDown(DbDataSource ds) {
        LifeCycle.Dispatcher.singleton.remove(this);
    }

    @Override
    public void onSuccess(DbDataSource ds, ConnectionWrapper connection,boolean emergency) {
        ds.setAlive(true);
        this.emergency=emergency;
        idleLiveCount.getAndAdd(-1);
    }
}
