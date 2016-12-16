package com.chexiao.base.dao.util;

/**
 * Created by fulei on 2016-12-15.
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JdbcUitl {
    protected static final Log logger = LogFactory.getLog(JdbcUitl.class);

    /**
     * close a connection
     * @param con
     */
    public static void closeConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.debug("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
            logger.debug("Unexpected exception on closing JDBC Connection", ex);
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
            logger.trace("Could not close JDBC Statement", ex);
        } catch (Throwable ex) {
            logger.trace("Unexpected exception on closing JDBC Statement", ex);
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
            logger.trace("Could not close JDBC ResultSet", ex);
        } catch (Throwable ex) {
            logger.trace("Unexpected exception on closing JDBC ResultSet", ex);
        }
    }
    /**
     * close a ResultSet
     * @param
     */
    public static void logCollector(long time ,String sql ,Object... params) {

//        LogCollectorMessage l =new LogCollectorMessage();
        StringBuffer paramStr =new StringBuffer();
        if(time>=200) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    if(params[i]!=null)
                        paramStr.append(params[i].toString()).append(";");

                }
//                l.append("runTime", time + "").append("sqlStr", sql).append("params", paramStr.toString());
//                DJFrameworkLogCollector.writeDjFrameworkLog(l, DjFrameworkAppType.DJDAO);
            } else {
//                l.append("runTime", time + "").append("sqlStr", sql).append("params", "null");
//                DJFrameworkLogCollector.writeDjFrameworkLog(l, DjFrameworkAppType.DJDAO);
            }
        }

    }
}
