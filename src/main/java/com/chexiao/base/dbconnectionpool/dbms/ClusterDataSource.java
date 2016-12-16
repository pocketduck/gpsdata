package com.chexiao.base.dbconnectionpool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.chexiao.base.dbconnectionpool.MessageAlert;
import com.chexiao.base.dbconnectionpool.dbms.config.ClusterConfig;
import com.chexiao.base.dbconnectionpool.dbms.config.DbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供集群的数据源，用户无需了解集群的细节和状态。当更改真实的数据源属性，所以任何访问该数据源的代码都无需更改。
 */
public class ClusterDataSource extends AbstractDataSource {

    private String name = "";

    /**
     * 某服务的所有数据库连接池对象。
     */
    protected final List<DbDataSource> dbDataSources = new ArrayList<DbDataSource>();
    /**
     * 当前正在使用的数据库连接。
     */
    private volatile DbDataSource curDbDataSource ;

    /** Logger class. */
    protected static Logger logger = LoggerFactory.getLogger(ClusterDataSource.class);

    private volatile long readSwitchedTime = 0;
    private static final long READ_SLAVE_DB_TIME = 1000 * 60;
    public volatile DbDataSource currentReadDataSource = null;
    public final DbDataSource masterDbDataSource; // 主库
    public final DbConfig masterConfig;

    public DbDataSource slaveDbDataSource; // 从库
    public final static ExecutorService exeService = Executors.newFixedThreadPool(1);
    private AtomicBoolean isInit = new AtomicBoolean(false);
    public static List<ClusterDataSource> clusterList = new CopyOnWriteArrayList<ClusterDataSource>();
    private long curDbSwitchedTime = 0;

    private Thread checkAndChangeThread = new Thread(new Runnable() {
        @Override
        public void run() {
            for (;;) {
                try {
                    Thread.sleep(1000);

                    // 如果配置了从库 ，启用HA
                    if (slaveDbDataSource !=null ) {

                        for (Iterator iterator = clusterList.iterator(); iterator.hasNext();) {
                            ClusterDataSource clusterDb = (ClusterDataSource) iterator.next();

                            // 检测 主库是否正常
                            if (((clusterDb.currentReadDataSource==null)
                                    && (!checkHostLive(getIpFromUrl(clusterDb.curDbDataSource.getName())) || !clusterDb.curDbDataSource.checkDbLiveForTimes() ))) {
                                clusterDb.currentReadDataSource = clusterDb.slaveDbDataSource;
                                clusterDb.curDbDataSource = clusterDb.currentReadDataSource;
                                curDbSwitchedTime = System.currentTimeMillis();
                                logger.error("HaPlus:读切库  checkAndChangeThread switched to slave curDbDataSource dbs , current is :"+ clusterDb.curDbDataSource.toString());
                                MessageAlert.Factory.get().sendMessage("HaPlus: 读切库" + clusterDb.slaveDbDataSource.getConfig().getConnetionURL());
                            }

                            //检测是否发生切库
                            if((clusterDb.currentReadDataSource!=null)
                                    && (System.currentTimeMillis()-curDbSwitchedTime > READ_SLAVE_DB_TIME)
                                    &&(checkHostLive(getIpFromUrl(clusterDb.masterDbDataSource.getName())))
                                    &&(clusterDb.masterDbDataSource.checkDbLiveForTimes())){
                                clusterDb.currentReadDataSource = null;
                                clusterDb.curDbDataSource = clusterDb.masterDbDataSource;
                                logger.error("HaPlus: 读恢复 , checkAndChangeThread switched to master  curDbDataSource dbs , current is :"+ clusterDb.curDbDataSource.toString());
                                MessageAlert.Factory.get().sendMessage("HaPlus: 读恢复" + clusterDb.masterDbDataSource.getConfig().getConnetionURL());
                            }

                            logger.debug("HaPlus: checkAndChangeThread iterator dbs , current is :"+ clusterDb.curDbDataSource.toString());
                        }

                    }

                    logger.debug("HaPlus: checkAndChangeThread check status is normal  , cluster size is : "+clusterList.size());

                } catch (Exception e) {
                    logger.error("checkAndChange Errors " + e.getMessage(), e);
                }
            }

        }
    });


    /**
     * 从jdbc连串接串中分离出主机
     * @param jdbcUrl
     * @return
     */
    public  static String getIpFromUrl(String jdbcUrl) {
        String ip = "";
        String tmpSocket = jdbcUrl.substring(jdbcUrl.indexOf("//")+2);
        ip = tmpSocket.substring(0,tmpSocket.indexOf(":"));
        return ip;
    }

    /**
     * 判断主机是否活着（已废弃）
     * @param hostIp
     * @return
     */
    @Deprecated
    public static boolean checkHostLive(String hostIp) {
//		boolean rtn = true;
//		int checkTimes = 0;
//		try {
//			InetAddress address = InetAddress.getByName(hostIp);
//			rtn =  address.isReachable(1000);
//			while(!rtn && (checkTimes<10)){
//				rtn =  address.isReachable(1000);
//				checkTimes ++;
//				Thread.sleep(1000);
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(),e);
//			rtn = false;
//		}
//		logger.debug("HaPlus: checkHostLive hostIp : rtn" + hostIp +" : " +rtn);
//		return rtn;
        return true;
    }

    /**
     * DataSourceCloud 的构造函数 根据DataSourceConfig 进行初始化操作 可同时初始化 多个源文件。
     *
     * @param dataSourceConfig
     * @throws Exception
     */
    public ClusterDataSource(ClusterConfig dataSourceConfig) throws Exception {

        this.curDbDataSource = null;
        DbConfig mconfig = null;
        for (DbConfig dbConfig : dataSourceConfig.getDbConfigList()) {
            DbDataSource dbDataSource = new DbDataSource(dbConfig);
            dbDataSource.setName(dbConfig.getConnetionURL());
            DbMonitor monitor = new DbStateMonitor();
            dbDataSource.addMonitor(monitor);

            if (this.curDbDataSource == null) {
                curDbDataSource = dbDataSource;
                mconfig = dbConfig;
            } else {
                slaveDbDataSource = dbDataSource;
            }

            dbDataSources.add(dbDataSource);
        }

        masterDbDataSource = curDbDataSource; // 2011-05-24 主库
        masterConfig = mconfig;

        if (masterDbDataSource == null) {
            throw new Exception("SWAP no DbConfig in " + dataSourceConfig.getName());
        }

        clusterList.add(this);
        if (!isInit.get()) {
            logger.debug("HaPlus ClusterDataSource init check ");
            exeService.execute(checkAndChangeThread);
            isInit.set(true);
        }
    }

    /**
     * 2011-05-24 数据库对主库降压
     */
    @Override
    public Connection GetReadConnection() throws SQLException {

        return getReadDataSource().getConnection();
    }

    /**
     * 获得数据库连接
     *
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {

        return this.getDataSource().getConnection();
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(" real datasources count: ")
                .append(this.dbDataSources.size())
                .append(", current datasource: ")
                .append(this.curDbDataSource == null ? "null" : curDbDataSource);

        return sb.toString();
    }

    /**
     * 新增数据库连接池
     *
     * @param dbDataSource
     * @param dbDataSource
     */

    protected void addDataSource(DbDataSource dbDataSource) {
        if (dbDataSource == null) return;
        dbDataSources.add(dbDataSource);
    }

    protected DbDataSource getReadDataSource() throws SQLException{

        long currentTime = System.currentTimeMillis();
        // 读连接不为空
        if (currentReadDataSource != null) {
            // 继续读从库
            if ((currentTime - readSwitchedTime) < READ_SLAVE_DB_TIME)
                return currentReadDataSource;

            // 需要切回主库
//			logger.error("SWAP readable datasource RESET datasource, ClusterDataSource:" + this.toString());
//
//
//			MessageAlert.Factory.get().sendMessage("读恢复" + this.masterConfig.getConnetionURL());

        }

        for(int index = 0; index < dbDataSources.size(); index ++) {
            DbDataSource dataSource = dbDataSources.get(index);

            if(dataSource == null) continue;

            currentTime = System.currentTimeMillis();
            // double check
            if (readSwitchedTime >= currentTime)
                return currentReadDataSource;

            if (dataSource.isAlive() && (!dataSource.isFull())){

                if(dataSource == this.masterDbDataSource){
                    currentReadDataSource = null;
                } else{

                    logger.error("SWAP readable datasource TO " + dataSource);
                    currentReadDataSource = dataSource;
                    readSwitchedTime = currentTime;
                    MessageAlert.Factory.get().sendMessage("读切库" + dataSource.getConfig().getConnetionURL());
                }

                return dataSource;
            }
        }

        throw new SQLException("SWAP no available datasource. " + this ,"08S01");
    }


    /**
     * 获得当前有效数据源首次获得DbDataSource设置为默认连接，每次返回DbDataSource之前都要执行一次
     * notNeedSwap(strategys)方法对每个切换策略的运行结果进行扫描。
     *
     * @return
     * @throws Exception
     */
    protected DbDataSource getDataSource(){

        for(int index = 0; index < dbDataSources.size(); index ++) {
            DbDataSource dataSource = dbDataSources.get(index);
            if (dataSource != null && dataSource.isAlive()){

                if(dataSource != curDbDataSource){
                    synchronized(this){
                        if(dataSource != curDbDataSource){
                            logger.error("SWAP datasource FROM " + curDbDataSource + " TO " + dataSource);
                            curDbDataSource = dataSource;
                            MessageAlert.Factory.get().sendMessage("切库" + dataSource.getConfig().getConnetionURL());

                        }
                    }
                }
                return curDbDataSource;
            }
        }

        logger.info("SWAP no available datasource. but current datasource is " + curDbDataSource);
        return curDbDataSource;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

