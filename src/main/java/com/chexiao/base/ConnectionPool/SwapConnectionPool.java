package com.chexiao.base.ConnectionPool;

/**
 * Created by fulei on 2016-12-15.
 */
import com.chexiao.base.ConnectionPool.dbms.AbstractDataSource;
import com.chexiao.base.ConnectionPool.dbms.DbUtils;

import java.sql.Connection;


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
