package com.chexiao.base.dao.util;

/**
 * Created by fulei on 2016-12-15.
 */
public class OutSQL {
    private String sql;

    public OutSQL() {

    }

    public OutSQL(String sql) {
        this.sql = sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
