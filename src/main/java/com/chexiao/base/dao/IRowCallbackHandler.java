package com.chexiao.base.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by fulei on 2016-12-15.
 */
public interface IRowCallbackHandler {
    public Object exec(ResultSet rs) throws SQLException;
}
