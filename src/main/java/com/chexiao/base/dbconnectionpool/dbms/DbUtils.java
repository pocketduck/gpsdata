package com.chexiao.base.dbconnectionpool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 用于合适的方式关闭Connection,Statement,ResultSet.
 * </br>
 * close by the right way
 *
 * @author
 *
 */
public class DbUtils {
    private static Logger logger = LoggerFactory.getLogger(DbUtils.class);

    /**
     * close a connection
     * @param connection
     */
    public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            logger.info("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
            logger.info("Unexpected exception on closing JDBC Connection", ex);
        }
    }

    /**
     * close a statement
     * @param stmt
     */
    public static void closeStatement(Statement stmt) {
        if (stmt == null) {
            return;
        }
        try {
            stmt.close();
        } catch (SQLException ex) {
            logger.info("Could not close JDBC Statement", ex);
        } catch (Throwable ex) {
            logger.info("Unexpected exception on closing JDBC Statement", ex);
        }
    }

    /**
     * close a ResultSet
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.close();
        } catch (SQLException ex) {
            logger.info("Could not close JDBC ResultSet", ex);
        } catch (Throwable ex) {
            logger.info("Unexpected exception on closing JDBC ResultSet", ex);
        }
    }

    /**
     * close a series of database-related activities(resultset, statement, connection).
     * @param rs
     * @param stmt
     * @param connection
     */
    public static void close(ResultSet rs, Statement stmt, Connection connection){
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(connection);

    }
}
