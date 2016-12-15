package com.chexiao.base.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by fulei on 2016-12-15.
 */
public interface IDAOExcute {
    public ResultSet executeQuery(String sql, String clusterName, PreparedStatement ps,String tableName, Object... param)throws Exception ;

    public int executeUpdate(String sql, String clusterName, PreparedStatement ps,String tableName, Object... param) throws Exception ;


    public ResultSet executeQuery(String sql, String clusterName, Statement ps, String tableName,Object... param)throws Exception ;

    public int executeUpdate(String sql, String clusterName, Statement ps,String tableName, Object... param) throws Exception ;
}
