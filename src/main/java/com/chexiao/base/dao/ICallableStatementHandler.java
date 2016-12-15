package com.chexiao.base.dao;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Created by fulei on 2016-12-15.
 */
public interface ICallableStatementHandler {
    public Object exec(CallableStatement cs) throws SQLException;
}
