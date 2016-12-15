package com.chexiao.base.dao;
import com.chexiao.base.dao.statementcreater.IStatementCreater;

import java.sql.Connection;
/**
 * Created by fulei on 2016-12-15.
 */
public interface ITransactionHandler {
    public Object exec(Connection conn, IStatementCreater sqlServerCreater, IStatementCreater mysqlCreater) throws Exception;
}
