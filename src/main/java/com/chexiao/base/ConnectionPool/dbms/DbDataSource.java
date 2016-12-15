package com.chexiao.base.ConnectionPool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.chexiao.base.ConnectionPool.dbms.config.DbConfig;
import com.chexiao.base.ConnectionPool.jsr166.LinkedTransferQueue;
import com.chexiao.base.ConnectionPool.jsr166.TransferQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbDataSource extends AbstractDataSource {

    public static int POOL_IS_FULL = 58001;

    private String name = "";

    private final ThreadLocal<DbConfig> config = new ThreadLocal<DbConfig>();

    private final List<DbMonitor> monitors = new ArrayList<DbMonitor>();

    private volatile boolean isAlive = true;

    /** set to true if the connection pool has been flagged as shutting down. */
    protected volatile boolean isShutDown;

    /** Connections available to be taken */
    private TransferQueue<ConnectionWrapper> freeConnections;

    /** Logger class. */
    private static Logger logger = LoggerFactory.getLogger(DbDataSource.class);

    /** Prevent repeated termination of all connections when the DB goes down. */
    protected Lock terminationLock = new ReentrantLock();

    /** Statistics lock. */
    protected ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();
    /** Number of connections that have been created. */
    private int size = 0;

    protected String checkLiveSQL = "SELECT 1";

    // �ϴλ���ʱ��
    private volatile long lastReleaseTime = 0;
    // ���ռ��
    private  volatile static long releaseInterval = 10 * 60 * 1000;

    private static int releaseStrategyValve = 10;

    private AtomicLong creatingConnCount = new AtomicLong(0);
    private static int  MAX_CREATING_THREADS = 20;
    //	private final static int TOTAL_THREAD_SIZE = 100;
    private AtomicBoolean isInit = new AtomicBoolean(false);

    private final ReentrantLock releaseLock = new ReentrantLock();
    private final ExecutorService releaseExecutor = new ThreadPoolExecutor(1, 1, 1500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());


    public DbDataSource(DbConfig config, DbMonitor monitor) {
        releaseInterval = 9090L;
        this.config.set(config);
        freeConnections = new LinkedTransferQueue<ConnectionWrapper>();

        LoadDrivers();
        registerExcetEven();
        addMonitor(monitor);

        int min = config.getMinPoolSize();
        releaseInterval = config.getReleaseInterval() * 1000;
        releaseStrategyValve = config.getReleaseStrategyValve();
        min = min < 1 ? 1 : min;

        //��ʼ��MAX_CREATING_THREADS ȡ�����߳����ݵ�һ�� ����������������е���Сֵ
//		MAX_CREATING_THREADS = Math.min(TOTAL_THREAD_SIZE/2, config.getMaxPoolSize());

        //���ڸ������û�ȡÿ������Դ������߳���
        if (!isInit.get()) {
            MAX_CREATING_THREADS = config.getMax_threads_per_ds()<5?5:config.getMax_threads_per_ds();
            logger.info("HaPlus : MAX_CREATING_THREADS is : " + MAX_CREATING_THREADS);
            isInit.set(true);
        }

        // 2011-05-30 ��֤������Сֵ
        try {
            for (int index = 0; index < min; index++) {
                Connection connection = createConnection();
                connection.close();
            }
        } catch (Exception e) {
            logger.warn("�������ݿ�����ʧ��"+e.getMessage());
            this.isAlive = false;
        }

    }

    public DbDataSource(DbConfig config) {
        this(config, null);
    }

    /**
     * shutdown the datasource
     */
    public synchronized void shutdown() {
        System.out.println("========================��ǰ���ӳعر�״̬=========================="+this.isShutDown +"\n\n\n\n");
        if (this.isShutDown)
            return;

        logger.info("Shutting down connection pool...");
        this.isShutDown = true;

        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onShutDown(this);
        }

        terminateAllConnections();
        logger.info("Connection pool has been shutdown.");

    }

    /**
     * �ж����ݿ����ӳ��Ƿ���
     *
     * @return
     */
    public boolean isFull() {
        int min = config.get().getMinPoolSize();
        int max = config.get().getMaxPoolSize();

        min = min > 0 ? min : 1;
        max = max > min ? max : min;

        int totalSize = this.getSize();
        int freeSize = this.getAvailableConnections();

        return ((totalSize - freeSize) >= max) ? true : false;
    }

    /**
     * �϶����ݿ��ǻ��
     *
     * @throws SQLException
     */
    private void assertLive() throws SQLException {
        if ((!isAlive) || isShutDown)
            throw new SQLException("SWAP " + this.getName() + " db connection pool is no alive or shutdown!");
    }

    @Override
    public Connection getConnection() throws SQLException {
        assertLive();

        ConnectionWrapper connection = this.freeConnections.poll();

        // create
        if (connection == null) {
//			synchronized (freeConnections) {
            if (isFull())
                throw new SQLException("SWAP db connection pool is full!" + this, null, POOL_IS_FULL); // vendorcode
            // 58001,
            // dbconnection
            // pool
            // is
            // full.

            // double check
            connection = this.freeConnections.poll();
            if (connection == null) {
                assertLive();
                connection = createConnection();
            }
        }
//		}
        connection.renew();

        // set readonly
        connection.setReadOnly(config.get().isReadonly());

        // hook calls
        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onCheckOut(this, connection);
        }

        return connection;
    }

    protected void release(ConnectionWrapper connection) throws SQLException {
        // hook calls
        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onCheckIn(this, connection);
        }
        boolean emerency=false;//��¼�Ƿ���connection broken ���������Ϣ���ݸ���س���  ֱ�ӽ�����һ��check ����ȴ�
        logger.debug("Release Connection dataSource url:"+connection.getDataSource().getName()+"  isAlive:"+connection.getDataSource().isAlive());
        // // �عرջ����������ش��󣬴ݻ�connection
        if (connection.isBroken() || isShutDown ) {
            // hook calls
            if (connection.isBroken()) {
                emerency=true;
                this.logger.debug("SWAP connection.isBroken, pool is" + this);

                for (int index = 0; index < this.getMonitors().size(); index++) {
                    DbMonitor monitor = this.getMonitors().get(index);
                    monitor.onBroken(this, connection);
                }
            }

            destroyConnection(connection);
            return;
        }

        this.addFreeConnection(connection); // �Żس�
        logger.debug(this.freeConnections.size()+"    ");
        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onSuccess(this, connection,emerency);
        }

        keepSize();

        // executor.execute(keepSizeExecute);
    }

    final Runnable keepSizeExecute = new Runnable() {
        @Override
        public void run() {
            keepSize();
        }
    };

    public boolean isAlive() {
        return isAlive;
    }

    public void addMonitor(DbMonitor monitor) {
        if (monitor == null)
            return;
        if (!this.monitors.contains(monitor)) {
            this.monitors.add(monitor);
            monitor.onBound(this);
        }
    }

    public void removeMonitor(DbMonitor monitor) {
        this.monitors.remove(monitor);
    }

    public List<DbMonitor> getMonitors() {
        return monitors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * ̽�����ݿ��Ƿ���Ŷ��
     * @return
     */
    public boolean checkDbLiveForTimes() {
        boolean rtn = true;
        int checkTimes = 0;
        try {
            rtn =  checkLive();
            while(!rtn && (checkTimes<10)){
                rtn =  checkLive();
                checkTimes++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("HaPlus: checkDbLiveForTimes Errors "+e.getMessage(),e);
            rtn = false;
        }
        logger.debug("HaPlus: checkDbLiveForTimes name : rtn" + getName() +" : " +rtn);
        return rtn;
    }

    /**
     * ������ݿ��Ƿ����
     *
     * @return
     */
    public boolean checkLive() {

        logger.debug("SWAP BEGIN check DataSource live. NAME: " + name);
        Connection connection = null;
        boolean result = false;
        boolean emerency=false;//��¼�Ƿ���connection broken ���������Ϣ���ݸ���س���  ֱ�ӽ�����һ��check ����ȴ�

        // first, try by a wrapped connection.
        try{
            connection = this.getConnection();
            if (connection != null) {
                result = checkConnectionAlive(connection);
            }
        } catch (SQLException e) {
            result = false;

        }

        DbUtils.closeConnection(connection);

        if (result) {
            this.isAlive = true;
            logger.debug("SWAP END of check live with wrapped." + ", RESULT: " + isAlive + "; NAME: " + name
                    + "; Thread Id: " + Thread.currentThread().getId());
            return result;
        }else{
            emerency=true;
            logger.info("Db check live ." + ", RESULT: " + result + "; NAME: " + name +"emergency "+ emerency
                    + "; Thread Id: " + Thread.currentThread().getId());


        }

        // second try by a new raw connection.
        try {
            // safety�� �½�һ��ԭʼ����
            connection = createConnection();
            result = checkConnectionAlive(connection);
        } catch (SQLException e) {
            result = false;
        }
        DbUtils.closeConnection(connection);
        if(emerency) {
            for (int index = 0; index < this.getMonitors().size(); index++) {
                DbMonitor monitor = this.getMonitors().get(index);
                monitor.onBroken(this, (ConnectionWrapper) connection);
            }
        }

        this.isAlive=result;
        if (result)
            logger.debug("SWAP END of check live with raw." + ", RESULT: " + isAlive + "; NAME: " + name
                    + "; Thread Id: " + Thread.currentThread().getId());
        else
            logger.info("SWAP END of check live with raw." + ", RESULT: " + isAlive + "; NAME: " + name
                    + "; Thread Id: " + Thread.currentThread().getId());

        return result;
    }

    @Override
    public String toString() {
        int min = config.get().getMinPoolSize();
        int max = config.get().getMaxPoolSize();
        int totalSize = this.getSize();
        int freeSize = this.getAvailableConnections();
        return this.getName() + (this.isAlive ? " ALIVE" : " DEAD") + ", url: " + config.get().getConnetionURL() + ", min="
                + min + ", max=" + max + ", totalSize=" + totalSize + ", freeSize=" + freeSize;
    }

    protected void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
        if (!this.isAlive)
            this.terminateAllConnections();
    }

    /**
     * �������ݿ����ӳصĴ�С����
     */
    protected void keepSize() {

        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastReleaseTime < releaseInterval)
            return;

        int min = config.get().getMinPoolSize();
        int max = config.get().getMaxPoolSize();

        min = min > 0 ? min : 1;
        max = max > min ? max : min;

        int totalSize = this.getSize();
        if (totalSize <= min)
            return;

        int freeSize = this.getAvailableConnections();

        // ��֤�̳߳���2��
        if (freeSize <= 2)
            return;

        if(freeSize <= releaseStrategyValve){
            // ����������ʹ�����ӱ���Ϊ2:1ʱ��������
            if (((freeSize * 3) > (totalSize * 2)) || totalSize > max) {
                if (currentTime - this.lastReleaseTime < releaseInterval)
                    return;
                this.lastReleaseTime = currentTime;
                logger.debug("SWAP release a connection. pool is" + this);
                ConnectionWrapper connection = this.freeConnections.poll();
                if (connection == null)
                    return;
                try {
                    this.destroyConnection(connection);
                } catch (SQLException e) {
                    logger.error("SWAP Error in keepSize to release connection", e);
                }
            }
        }else{

            final ReentrantLock lock = this.releaseLock;
            lock.lock();

            try{
                if (currentTime - this.lastReleaseTime < releaseInterval){
                    return;
                }
                this.lastReleaseTime = currentTime;
                logger.debug("SWAP release "+ freeSize/2 +" connections. pool is" + this);
                //�첽��������
                releaseExecutor.execute(new ReleaseTask(freeSize,this));
            }finally{
                lock.unlock();
            }



//			for(int i = 0 ; i < freeSize/2 ; i++){
//
//				ConnectionWrapper connection = this.freeConnections.poll();
//				if (connection == null)
//					return;
//				try {
//					this.destroyConnection(connection);
//				} catch (SQLException e) {
//					logger.error("SWAP Error in keepSize to release connection", e);
//				}
//			}
        }

    }

    /**
     * ���һ�������Ƿ���Ч
     *
     * @param connection
     * @return
     */
    protected boolean checkConnectionAlive(Connection connection) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(checkLiveSQL);
            logger.debug(" Connection is :" +connection.getMetaData().getURL());
            result = true;
        } catch (SQLException e) {
            result = false;
        }

        DbUtils.closeResultSet(rs);
        DbUtils.closeStatement(stmt);
        return result;
    }

    /**
     * Adds a free connection.
     *
     * @throws SQLException
     *             on error
     */
    protected void addFreeConnection(ConnectionWrapper connection) throws SQLException {
        if (!this.freeConnections.offer(connection)) {
            connection.internalClose();
        }
    }

    /**
     * �õ�һ��������
     *
     * @return
     * @throws SQLException
     */
    protected ConnectionWrapper createConnection() throws SQLException {

        logger.debug("HaPlus: createConnection  cucrent creatingConnCount  :" + creatingConnCount.get());
        if (creatingConnCount.get() > MAX_CREATING_THREADS) {
            throw new SQLException("HaPlus: createConnection creatingConnCount is to max :" + creatingConnCount.get());

        }
        creatingConnCount.incrementAndGet();

        ConnectionWrapper lgconnection = null;

        try {

            Connection connection = null;
            String url = this.config.get().getConnetionURL();
            String username = this.config.get().getUsername();
            String password = this.config.get().getPassword();

            connection = DriverManager.getConnection(url, username, password);

            lgconnection = new ConnectionWrapper(this, connection);

            updateSize(1);

            // 2011-05-30
            this.lastReleaseTime = System.currentTimeMillis();

            // hook calls
            for (int index = 0; index < this.getMonitors().size(); index++) {
                DbMonitor monitor = this.getMonitors().get(index);
                monitor.onCreate(this, lgconnection);
            }

            logger.debug("SWAP Created a new connection by " + this);
        } catch (SQLException e) {
            throw e;
        } finally {
            creatingConnCount.decrementAndGet();
        }


        return lgconnection;
    }

    /**
     * ��ȫ�ݻ�һ�����ݿ�����
     *
     * @param connectionHandle
     * @throws SQLException
     */
    protected void destroyConnection(ConnectionWrapper connection) throws SQLException {

        updateSize(-1);
        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onDestroy(this, connection);
        }

        connection.internalClose();
        logger.debug("SWAP Destroyed a connection by " + this);

    }

    /**
     * Updates leased connections statistics
     *
     * @param increment
     *            value to add/subtract
     */
    protected void updateSize(int increment) {
        try {
            this.statsLock.writeLock().lock();
            this.size += increment;

        } finally {
            this.statsLock.writeLock().unlock();
        }
    }

    /**
     * @return the leasedConnections
     */
    protected int getSize() {
        try {
            this.statsLock.readLock().lock();
            return this.size;
        } finally {
            this.statsLock.readLock().unlock();
        }
    }

    private void LoadDrivers() {
        try {
            Driver driver = (Driver) Class.forName(config.get().getDriversClass()).newInstance();
            DriverManager.registerDriver(driver);
            DriverManager.setLoginTimeout(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Closes off all connections in all partitions. */
    protected void terminateAllConnections() {
        this.terminationLock.lock();
        try {
            ConnectionWrapper conn;
            while ((conn = this.freeConnections.poll()) != null) {
                try {
                    this.destroyConnection(conn);
                    // conn.internalClose();
                } catch (SQLException e) {
                    logger.error("Error in attempting to close connection", e);
                }
            }
        } finally {
            this.terminationLock.unlock();
        }
    }

    /**
     * Returns the number of avail connections
     *
     * @return avail connections.
     */
    public int getAvailableConnections() {
        return this.freeConnections.size();
    }

    /*
     * when application exiting destroy all connections
     */
    private void registerExcetEven() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
        });
    }

    public DbConfig getConfig() {
        return this.config.get();
    }


    class ReleaseTask implements Runnable {
        int freeSize;
        DbDataSource source;

        public ReleaseTask(int freeSize,DbDataSource source){
            this.freeSize = freeSize;
            this.source = source;
        }

        public void run() {
            for(int i = 0 ; i < freeSize/2 ; i++){

                ConnectionWrapper connection = source.freeConnections.poll();
                if (connection == null)
                    return;
                try {
                    source.destroyConnection(connection);
                } catch (SQLException e) {
                    logger.error("SWAP Error in keepSize to release connection", e);
                }
            }
        }
    }{
}
