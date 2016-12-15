package com.chexiao.base.ConnectionPool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public abstract class AbstractDataSource  implements DataSource {

    /**
     * 2011-05-24 获得一个只需要读的数据库连接,处理主库压力大的情况下，降低主库压力
     * by
     * @return 一个只供读的数据库连接，可能是主库，也有可能是从库
     * @throws SQLException
     */
    public Connection GetReadConnection() throws SQLException{
        throw new SQLException("Not Implemented");
    }


    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public abstract Connection getConnection() throws SQLException;


    /*
     * (non-Javadoc)
     *
     * @see javax.sql.CommonDataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLException("Not Implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.CommonDataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.CommonDataSource#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        throw new SQLException("Not Implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException("Not Implemented");
    }


    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#getConnection(java.lang.String,
     * java.lang.String)
     */
    @Override
    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new SQLException("Not Implemented");
    }
}
