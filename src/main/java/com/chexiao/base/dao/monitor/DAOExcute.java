package com.chexiao.base.dao.monitor;

/**
 * Created by fulei on 2016-12-15.
 */

import com.chexiao.base.dao.IDAOExcute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;



public class DAOExcute implements IDAOExcute {

    DAOExcute(){

    }

    @Override
    public ResultSet executeQuery(String sql, String clusterName, PreparedStatement ps, String tableName,Object... param) throws Exception {
        ResultSet rs;
        try {
            rs= ps.executeQuery();

        }catch (Exception e){
            throw e;
        }
        return rs;
    }

    @Override
    public int executeUpdate(String sql, String clusterName, PreparedStatement ps, String tableName,Object... param) throws Exception {
        int rs=0;
        try {
            rs= ps.executeUpdate();

        }catch (Exception e){
            throw e;
        }
        return rs;
    }

    @Override
    public ResultSet executeQuery(String sql, String clusterName, Statement ps,String tableName, Object... param) throws Exception {
        ResultSet rs;
        try {
            rs= ps.executeQuery(sql);
        }catch (Exception e){
            throw e;
        }
        return rs;
    }

    @Override
    public int executeUpdate(String sql, String clusterName, Statement ps,String tableName, Object... param) throws Exception {
        int rs=0;
        try {
            rs= ps.executeUpdate(sql);

        }catch (Exception e){
            throw e;
        }
        return rs;
    }
}
