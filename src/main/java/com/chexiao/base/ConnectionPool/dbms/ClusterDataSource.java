package com.chexiao.base.ConnectionPool.dbms;

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

import com.chexiao.base.ConnectionPool.MessageAlert;
import com.chexiao.base.ConnectionPool.dbms.config.ClusterConfig;
import com.chexiao.base.ConnectionPool.dbms.config.DbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterDataSource extends AbstractDataSource {


    private String name = "";

    /**
     * ĳ������������ݿ����ӳض���
     */
    protected final List<DbDataSource> dbDataSources = new ArrayList<DbDataSource>();
    /**
     * ��ǰ����ʹ�õ����ݿ����ӡ�
     */
    private volatile DbDataSource curDbDataSource ;

    /** Logger class. */
    protected static Logger logger = LoggerFactory.getLogger(ClusterDataSource.class);

    private volatile long readSwitchedTime = 0;
    private static final long READ_SLAVE_DB_TIME = 1000 * 60;
    public volatile DbDataSource currentReadDataSource = null;
    public final DbDataSource masterDbDataSource; // ����
    public final DbConfig masterConfig;

    public DbDataSource slaveDbDataSource; // �ӿ�
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

                    // ��������˴ӿ� ������HA
                    if (slaveDbDataSource !=null ) {

                        for (Iterator iterator = clusterList.iterator(); iterator.hasNext();) {
                            ClusterDataSource clusterDb = (ClusterDataSource) iterator.next();

                            // ��� �����Ƿ�����
                            if (((clusterDb.currentReadDataSource==null)
                                    && (!checkHostLive(getIpFromUrl(clusterDb.curDbDataSource.getName())) || !clusterDb.curDbDataSource.checkDbLiveForTimes() ))) {
                                clusterDb.currentReadDataSource = clusterDb.slaveDbDataSource;
                                clusterDb.curDbDataSource = clusterDb.currentReadDataSource;
                                curDbSwitchedTime = System.currentTimeMillis();
                                logger.error("HaPlus:���п�  checkAndChangeThread switched to slave curDbDataSource dbs , current is :"+ clusterDb.curDbDataSource.toString());
                                MessageAlert.Factory.get().sendMessage("HaPlus: ���п�" + clusterDb.slaveDbDataSource.getConfig().getConnetionURL());
                            }

                            //����Ƿ����п�
                            if((clusterDb.currentReadDataSource!=null)
                                    && (System.currentTimeMillis()-curDbSwitchedTime > READ_SLAVE_DB_TIME)
                                    &&(checkHostLive(getIpFromUrl(clusterDb.masterDbDataSource.getName())))
                                    &&(clusterDb.masterDbDataSource.checkDbLiveForTimes())){
                                clusterDb.currentReadDataSource = null;
                                clusterDb.curDbDataSource = clusterDb.masterDbDataSource;
                                logger.error("HaPlus: ���ָ� , checkAndChangeThread switched to master  curDbDataSource dbs , current is :"+ clusterDb.curDbDataSource.toString());
                                MessageAlert.Factory.get().sendMessage("HaPlus: ���ָ�" + clusterDb.masterDbDataSource.getConfig().getConnetionURL());
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
     * ��jdbc�����Ӵ��з��������
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
     * �ж������Ƿ���ţ��ѷ�����
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
     * DataSourceCloud �Ĺ��캯�� ����DataSourceConfig ���г�ʼ������ ��ͬʱ��ʼ�� ���Դ�ļ���
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

        masterDbDataSource = curDbDataSource; // 2011-05-24 ����
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
     * 2011-05-24 ���ݿ�����⽵ѹ
     */
    @Override
    public Connection GetReadConnection() throws SQLException {

        return getReadDataSource().getConnection();
    }

    /**
     * ������ݿ�����
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
     * �������ݿ����ӳ�
     *
     * @param dataSourceName
     * @param dbDataSource
     */

    protected void addDataSource(DbDataSource dbDataSource) {
        if (dbDataSource == null) return;
        dbDataSources.add(dbDataSource);
    }

    protected DbDataSource getReadDataSource() throws SQLException{

        long currentTime = System.currentTimeMillis();
        // �����Ӳ�Ϊ��
        if (currentReadDataSource != null) {
            // �������ӿ�
            if ((currentTime - readSwitchedTime) < READ_SLAVE_DB_TIME)
                return currentReadDataSource;

            // ��Ҫ�л�����
//			logger.error("SWAP readable datasource RESET datasource, ClusterDataSource:" + this.toString());
//
//
//			MessageAlert.Factory.get().sendMessage("���ָ�" + this.masterConfig.getConnetionURL());

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
                    MessageAlert.Factory.get().sendMessage("���п�" + dataSource.getConfig().getConnetionURL());
                }

                return dataSource;
            }
        }

        throw new SQLException("SWAP no available datasource. " + this ,"08S01");
    }


    /**
     * ��õ�ǰ��Ч����Դ�״λ��DbDataSource����ΪĬ�����ӣ�ÿ�η���DbDataSource֮ǰ��Ҫִ��һ��
     * notNeedSwap(strategys)������ÿ���л����Ե����н������ɨ�衣
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
                            MessageAlert.Factory.get().sendMessage("�п�" + dataSource.getConfig().getConnetionURL());

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

