package com.chexiao.base.dao;


import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by fulei on 2016-12-15.
 */
public interface IPreparedStatementHandler {
    public Object exec(PreparedStatement ps) throws SQLException;
}
