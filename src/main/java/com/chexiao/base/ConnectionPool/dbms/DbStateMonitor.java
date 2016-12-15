package com.chexiao.base.ConnectionPool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ���DbDatasource���¼��������¼������ж����ݿ�����
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
        logger.debug(dataSource.getName()+"���ô�����"+count);
        if (count < 0){
            idleLiveCount.set(0);
        }

        if (dataSource.isShutDown)
            return;

        if (count < expectCount && (dataSource.isAlive())) return;


        logger.debug("������ݿ�����״̬��"+dataSource.getName());
        dataSource.checkLive();
        // ������ݿ��ǻ��ŵģ����Ҳ��ǽ���״̬ (����״̬������connection ��kill����)�򽫼�������λ�������´λ��������ݿ�
        if (dataSource.isAlive()&&!emergency){

            idleLiveCount.set(0);
        }else{
            idleLiveCount.set(12);//���Ϊ����״̬ ��ֱ�ӽ�����һ��check
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
