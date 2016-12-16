package com.chexiao.base.dbconnectionpool;

/**
 * Created by fulei on 2016-12-15.
 */
import com.chexiao.base.dbconnectionpool.dbms.AbstractDataSource;
import com.chexiao.base.dbconnectionpool.dbms.DbUtils;

import java.sql.Connection;

/**
 * 该ConnectionPool封装SWAP DataSource实现
 * @author
 *
 */
public class SwapConnectionPool  extends ConnectionPool{

        private AbstractDataSource datasource;

        public SwapConnectionPool(AbstractDataSource datasource){
            this.datasource = datasource;
        }

    public int GetAllCount()
    {
        return -1;
    }

    public int GetFreeConnCount()
    {
        return -1;
    }

    public synchronized Connection Get() throws Exception
    {
        return datasource.getConnection();
    }

    public synchronized void Release(Connection connection)
    {
        DbUtils.closeConnection(connection);
    }

    public Connection GetReadConnection() throws Exception {
        return datasource.GetReadConnection();
    }
}
