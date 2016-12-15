package com.chexiao.base.dao;

import java.util.List;
import java.util.Map;


/**
 * Created by fulei on 2016-12-15.
 */
public interface IDAOOperator {
    public Object insert(Object bean) throws Exception;


    public int upateEntity(Object bean) throws Exception ;


    public <I> int upateByID(Class<?> clazz, String updateStatement, I id) throws Exception ;

    public <I> Object updateByID(Class<?> clazz,I id, Map<String, Object> valuesMap) throws Exception;

    public int updateByCustom(Class<?> clazz, String updateStatement, String condition) throws Exception ;


    public <I> int deleteByID(Class<?> clazz, I id) throws Exception ;


    public <I> int deleteByIDS(Class<?> clazz, I[] ids) throws Exception ;


    public int deleteByCustom(Class<?> clazz, String condition) throws Exception ;


    public <I> Object get(Class<?> clazz, I id) throws Exception ;


    public List<?> getListByCustom(Class<?> clazz, String columns, String condition, String orderBy) throws Exception ;


    public List<?> getListByPage(Class<?> clazz, String condition, String columns, int page, int pageSize, String orderBy) throws Exception ;


    public List<Object[]> customSql(String sql, int columnCount) throws Exception ;


    public void customSqlNoReturn(String sql) throws Exception ;


    public int getCount(Class<?> clazz, String condition) throws Exception ;


    public <T,I> List<T> getListByIDS(Class<T> clazz, I[] ids) throws Exception ;


    public void updateByCustom(Class<?> clazz, Map<String, Object> kv, Map<String, Object> condition) throws Exception ;


    public void deleteByCustom(Class<?> clazz, Map<String, Object> condition) throws Exception ;


    public <T> List<T> getListByCustom(Class<T> clazz, String columns, Map<String, Object> condition, String orderBy) throws Exception ;


    public <T> List<T> getListByPage(Class<T> clazz, Map<String, Object> condition, String columns, int page, int pageSize, String orderBy) throws Exception ;


    public int getCount(Class<?> clazz, Map<String, Object> condition) throws Exception ;



    public <T> List<T> getListBySQL(Class<T> clazz, String sql, Object... param) throws Exception ;


    public int execBySQL(String sql, Object... param) throws Exception ;


    public int getCountBySQL(String sql, Object... param) throws Exception ;


    public Object execInsert(String sql, IPreparedStatementHandler handler) throws Exception ;


    public Object execWithPara(String sql, IPreparedStatementHandler handler) throws Exception ;
    public Object execQuery(String sql, IRowCallbackHandler handler) throws Exception ;

    public Object execProc(String sql, ICallableStatementHandler handler) throws Exception;

    /**
     * 开启事务(默认级别TRANSACTION_READ_COMMITTED)
     * @throws Exception
     */
    public void beginTransaction() throws Exception ;

    /**
     * 开启事务
     * @param level 事务级别
     * @throws Exception
     */
    public void beginTransaction(int level) throws Exception ;

    /**
     * 提交事务
     * @throws Exception
     */
    public void commitTransaction() throws Exception ;
    /**
     * 回滚事务
     * @throws Exception
     */
    public void rollbackTransaction() throws Exception ;

    /**
     * 结束事务
     * @throws Exception
     */
    public void endTransaction() throws Exception;
}
