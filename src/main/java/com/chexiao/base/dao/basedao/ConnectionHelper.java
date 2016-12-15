package com.chexiao.base.dao.basedao;

import java.io.File;
import java.sql.Connection;

import com.chexiao.base.ConnectionPool.ConnectionPool;
import com.chexiao.base.ConnectionPool.ConnectionPoolFactory;
import com.chexiao.base.ConnectionPool.DBConfig;
import com.chexiao.base.dao.util.PropertiesHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by fulei on 2016-12-15.
 */
public class ConnectionHelper {

    private ConnectionPool connPool;

    private final ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();

    private static final Log logger = LogFactory.getLog(ConnectionHelper.class);

    public ConnectionHelper(String configPath) throws Exception {
        logger.info("creating DAOHelper configPath:" + configPath);
        PropertiesHelper ph = new PropertiesHelper(configPath);
        logger.info("init ConnectionPool...");

        /**
         * �ṩ���ݿ��л�����
         */
//		String swapDataSource = ph.getString("swapDataSource");
//		if(swapDataSource!=null){
//
//			connPool = getDataSource(configPath, swapDataSource);
//			if(connPool == null)
//				throw new Exception("conn pool is null: " + configPath);
//
//		} else {
//			connPool = createConnPool(ph);
//		}

        connPool = getDataSource(configPath);
        logger.info("init ConnectionPool success connection count:" + connPool.GetAllCount());
        if(connPool.GetAllCount() == 0) {
            logger.warn("success create 0 connection, please check config!!!");
        }
    }

    /**
     * ����Utility�������ļ��������ݿ��л����������ConnectionPool
     * @param configPath Utility�������ļ�
     * @return
     * @throws Exception
     * @author
     */
    private ConnectionPool getDataSource(String configPath) throws Exception{

//		String realPath =(new File(configPath)).getParent() + "/" + swapDataSource;

        return ConnectionPoolFactory.createPool(configPath);
    }


    /**
     * ��ȡ����
     * @return
     * @throws Exception
     */
    public Connection get() throws Exception {
        Connection conn = threadLocal.get();
        if(conn == null) {
            conn = connPool.Get();
        }
        return conn;
    }

    /**
     * ��ÿ���ֻ�������ݿ�����
     * @return
     * @throws Exception
     */
    public Connection getReadConnection() throws Exception {
        Connection conn = threadLocal.get();
        if(conn == null) {
            conn = connPool.GetReadConnection();
        }
        return conn;
    }

    /**
     * �ͷ�����
     * @param conn
     * @throws Exception
     */
    public void release(Connection conn) {
        Connection tconn = threadLocal.get();
        if(tconn == null
                || (tconn != null && (tconn.hashCode() != conn.hashCode()))) {
            logger.debug("this conn is release "+conn);
            connPool.Release(conn);
        }
    }

    public void lockConn(Connection conn) {
        threadLocal.set(conn);
    }

    public void unLockConn() {
        threadLocal.set(null);
    }

    public ConnectionPool getConnPool() {
        return connPool;
    }

    /**
     * �������ӳ�
     * @throws Exception
     * @throws Exception
     */
    private ConnectionPool createConnPool(PropertiesHelper ph) throws Exception {
        logger.debug("ConnectionPool ConnetionURL:" + ph.getString("ConnetionURL"));
        logger.debug("ConnectionPool DriversClass:" + ph.getString("DriversClass"));
        logger.debug("ConnectionPool UserName:***");
        logger.debug("ConnectionPool PassWord:***");
        logger.debug("ConnectionPool MinPoolSize:" + ph.getInt("MinPoolSize"));
        logger.debug("ConnectionPool MaxPoolSize:" + ph.getInt("MaxPoolSize"));
        logger.debug("ConnectionPool IdleTimeout:" + ph.getInt("IdleTimeout"));
        logger.debug("ConnectionPool AutoShrink:" + ph.getBoolean("AutoShrink"));

        DBConfig config = new DBConfig();
        config.setConnetionUrl(ph.getString("ConnetionURL"));
        config.setDriversClass(ph.getString("DriversClass"));
        config.setUserName(ph.getString("UserName"));
        config.setPassWord(ph.getString("PassWord"));
        config.setMinPoolSize(ph.getInt("MinPoolSize"));
        config.setMaxPoolSize(ph.getInt("MaxPoolSize"));
        config.setIdleTimeout(ph.getInt("IdleTimeout"));
        config.setAutoShrink(ph.getBoolean("AutoShrink"));
        return new ConnectionPool(config);
    }
}
