package com.chexiao.base.dao;

import com.chexiao.base.dao.basedao.ConnectionHelper;
import com.chexiao.base.dao.monitor.DAOExcuteFactory;
import com.chexiao.base.dao.statementcreater.IStatementCreater;
import com.chexiao.base.dao.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by fulei on 2016-12-15.
 */
public class DAOOperator implements IDAOOperator{
    private IStatementCreater psCreater;

    private ConnectionHelper connHelper;


    private static String clusterName;

    private static IDAOExcute daoExcute;
    /**
     * 默认查询超时时间
     */
    protected int qurryTimeOut = 2;

    /**
     * 默认添加/修改超时时间
     */
    protected int insertUpdateTimeOut = 5;

    private static final Log logger = LogFactory.getLog(DAOOperator.class);

    public DAOOperator(String configPath) throws Exception {


        ConnectionHelper ch = new ConnectionHelper(configPath);
        PropertiesHelper ph = new PropertiesHelper(configPath);

        String sqlCreaterClass = "com.chexiao.base.dao.statementcreater.MysqlPSCreater"; //ph.getString("SqlCreaterClass");
        if(sqlCreaterClass != null && !sqlCreaterClass.equalsIgnoreCase("")) {

            logger.info("init SqlCreaterClass:" + sqlCreaterClass);
            IStatementCreater creater = (IStatementCreater)Class.forName(sqlCreaterClass).newInstance();

            this.psCreater = creater;
            this.connHelper = ch;

            if (System.getProperty("WF.uspcluster") != null) {
                this.clusterName = System.getProperty("WF.uspcluster");
            } else {
                this.clusterName = ph.getString("clusterName") == null ? "DAO" : ph.getString("clusterName");
            }
            this.daoExcute= DAOExcuteFactory.createDaoExcute();
            this.qurryTimeOut = ph.getInt("queryTimeout");
            this.insertUpdateTimeOut = ph.getInt("insertUpdateTimeout");
        }
    }


    public Object insert(Object bean) throws Exception{
        return insert(bean, insertUpdateTimeOut);
    }


    public int upateEntity(Object bean) throws Exception {
        return upateEntity(bean, qurryTimeOut);
    }


    public <I> int upateByID(Class<?> clazz, String updateStatement, I id) throws Exception {
        return upateByID(clazz, updateStatement, id, insertUpdateTimeOut);
    }

    public <I> Object updateByID(Class<?> clazz,I id, Map<String, Object> valuesMap) throws Exception{
        return updateByID( clazz, id, valuesMap, insertUpdateTimeOut);
    }
    public int updateByCustom(Class<?> clazz, String updateStatement, String condition) throws Exception {
        return updateByCustom(clazz, updateStatement, condition, insertUpdateTimeOut);
    }


    public <I> int deleteByID(Class<?> clazz, I id) throws Exception {
        return deleteByID(clazz, id, qurryTimeOut);
    }


    public <I> int deleteByIDS(Class<?> clazz, I[] ids) throws Exception {
        return deleteByIDS(clazz, ids, qurryTimeOut);
    }


    public int deleteByCustom(Class<?> clazz, String condition) throws Exception {
        return deleteByCustom(clazz, condition, qurryTimeOut);
    }


    public <I> Object get(Class<?> clazz, I id) throws Exception {
        return get(clazz, id, qurryTimeOut);
    }


    public List<?> getListByCustom(Class<?> clazz, String columns, String condition,String orderBy) throws Exception {
        return getListByCustom(clazz, columns, condition, orderBy, qurryTimeOut);
    }


    public List<?> getListByPage(Class<?> clazz, String condition, String columns, int page, int pageSize, String orderBy) throws Exception {
        return getListByPage(clazz, condition, columns, page, pageSize, orderBy, qurryTimeOut);
    }


    public List<Object[]> customSql(String sql, int columnCount) throws Exception {
        return customSql(sql, columnCount, qurryTimeOut);
    }


    public void customSqlNoReturn(String sql) throws Exception {
        customSqlNoReturn(sql, qurryTimeOut);
    }


    public int getCount(Class<?> clazz, String condition) throws Exception {
        return getCount(clazz, condition, qurryTimeOut);
    }


    public <T,I> List<T> getListByIDS(Class<T> clazz, I[] ids) throws Exception {
        return getListByIDS(clazz, ids, qurryTimeOut);
    }


    public void updateByCustom(Class<?> clazz, Map<String, Object> kv, Map<String, Object> condition) throws Exception {
        updateByCustom(clazz, kv, condition, insertUpdateTimeOut);
    }


    public void deleteByCustom(Class<?> clazz, Map<String, Object> condition) throws Exception {
        deleteByCustom(clazz, condition, qurryTimeOut);
    }


    public <T> List<T> getListByCustom(Class<T> clazz, String columns, Map<String, Object> condition, String orderBy) throws Exception {
        return getListByCustom(clazz, columns, condition, orderBy, qurryTimeOut);
    }


    public <T> List<T> getListByPage(Class<T> clazz, Map<String, Object> condition, String columns, int page, int pageSize, String orderBy) throws Exception {
        return getListByPage(clazz, condition, columns, page, pageSize, orderBy, qurryTimeOut);
    }


    public int getCount(Class<?> clazz, Map<String, Object> condition) throws Exception {
        return getCount(clazz, condition, qurryTimeOut);
    }



    public <T> List<T> getListBySQL(Class<T> clazz, String sql, Object... param) throws Exception {
        return getListBySQL(clazz, sql, qurryTimeOut, param);
    }


    public int execBySQL(String sql, Object... param) throws Exception {
        return execBySQL(sql, insertUpdateTimeOut, param);
    }


    public int getCountBySQL(String sql, Object... param) throws Exception {

        return getCountBySQL(sql, qurryTimeOut, param);
    }

    //----------------------------------------------------------------------


    public int getCountBySQL(String sql, int timeOut, Object... param) throws Exception {
        Connection conn = null;
        long startTime = System.currentTimeMillis();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = connHelper.getReadConnection();
            ps = conn.prepareStatement(sql);
            ps.setQueryTimeout(timeOut);

            if(param != null) {
                for(int i=0; i<param.length; i++) {
                    Common.setPara(ps, param[i], i + 1);
                }
            }

            //rs = ps.executeQuery();
            rs=daoExcute.executeQuery(sql,clusterName,ps,"",param);

            if(rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch(Exception ex) {

            throw ex;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql, param);
        }
    }


    public <T> List<T> getListBySQL(Class<T> clazz, String sql, int timeOut, Object... param) throws Exception {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<T> dataList = null;
        long startTime = System.currentTimeMillis();
        try {
            conn = connHelper.getReadConnection();
            ps = conn.prepareStatement(sql);
            ps.setQueryTimeout(timeOut);

            if(param != null) {
                for(int i=0; i<param.length; i++) {
                    Common.setPara(ps, param[i], i + 1);
                }
            }

            rs=daoExcute.executeQuery(sql,clusterName,ps,"",param);
            dataList = populateData(rs, clazz);
        } catch (SQLException e) {
            logger.error("getListByCustom error sql:" + sql, e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql, param);
        }
        return dataList;
    }


    public int execBySQL(String sql, int timeOut, Object... param) throws Exception {
        Connection conn = null;long startTime = System.currentTimeMillis();
        PreparedStatement ps = null;
        try {
            //conn = connHelper.getReadConnection();
            //modify by haoxb 2012-09-27
            conn = connHelper.get();
            ps = conn.prepareStatement(sql);
            ps.setQueryTimeout(timeOut);

            if(param != null) {
                for(int i=0; i<param.length; i++) {
                    Common.setPara(ps, param[i], i + 1);
                }
            }
            return daoExcute.executeUpdate(sql,clusterName,ps,"",param);
        } catch (SQLException e) {
            logger.error("getListByCustom error sql:" + sql, e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql, param);
        }
    }




    public void updateByCustom(Class<?> clazz, Map<String, Object> kv, Map<String, Object> condition, int timeOut) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }


    public void deleteByCustom(Class<?> clazz, Map<String, Object> condition, int timeOut) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }


    public <T> List<T> getListByCustom(Class<T> clazz, String columns, Map<String, Object> condition,String orderBy, int timeOut) throws Exception {
        //TODO
        throw new UnsupportedOperationException("Not supported");
    }


    public <T> List<T> getListByPage(Class<T> clazz, Map<String, Object> condition, String columns, int page, int pageSize, String orderBy, int timeOut) throws Exception {
        //TODO
        throw new UnsupportedOperationException("Not supported");
    }


    public int getCount(Class<?> clazz, Map<String, Object> condition, int timeOut) throws Exception {
        //TODO
        throw new UnsupportedOperationException("Not supported");
    }





    public <T,I> List<T> getListByIDS(Class<T> clazz, I[] ids, int timeOut) throws Exception {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<T> dataList = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.getReadConnection();
            ps = psCreater.createGetByIDS(clazz, conn, ids, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            for(int i=0; i<ids.length; i++) {
                params.add(i,ids[i]);
            }
            rs = daoExcute.executeQuery(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            dataList = populateData(rs, clazz);
        } catch (SQLException e) {
            logger.error("getListByCustom error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return dataList;
    }


    public Object insert(Object bean, int timeOut) throws Exception {
        Class<?> beanCls = bean.getClass();

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Object rst = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createInsert(bean, conn, sql);

            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(bean.getClass().getCanonicalName());
            daoExcute.executeUpdate(sql.getSql(),clusterName,ps,Common.getTableName(bean.getClass()),params);


            boolean isProc = false;
            Class<?>[] clsAry = ps.getClass().getInterfaces();
            for(Class<?> cls : clsAry) {
                if(cls == CallableStatement.class) {
                    isProc = true;
                    break;
                }
            }

            List<java.lang.reflect.Field> identityFields = Common.getIdentityFields(beanCls);
            if(isProc) {
                if(identityFields.size() == 1) {
                    rst = ((CallableStatement)ps).getObject(Common.getDBCloumnName(beanCls, identityFields.get(0)));
                }
            } else {
                if(identityFields.size() == 1 ) {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        List<Field> idFieldList = Common.getIdFields(beanCls);
                        if(idFieldList.size() == 1) {
                            if(idFieldList.get(0).getType() == int.class
                                    || idFieldList.get(0).getType() == Integer.class) {
                                rst = rs.getInt(1);
                            } else if(idFieldList.get(0).getType() == long.class
                                    || idFieldList.get(0).getType() == Long.class) {
                                rst = rs.getLong(1);
                            } else if(idFieldList.get(0).getType() == String.class) {
                                rst = rs.getString(1);
                            } else {
                                rst = rs.getObject(1);
                            }
                        } else {
                            rst = rs.getObject(1);
                        }
                    }
                } else if(identityFields.size() == 0) {
                    List<java.lang.reflect.Field> idFields = Common.getIdFields(beanCls);
                    if(idFields.size() == 1) {
                        Field id = idFields.get(0);
                        id.setAccessible(true);
                        rst = id.get(bean);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("insert error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);

        }

        return rst;
    }

    public int upateEntity(Object bean, int timeOut) throws Exception {
        Connection conn = null;long startTime = System.currentTimeMillis();
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        int affectNum=0;
        try {
            conn = connHelper.get();
            ps = psCreater.createUpdateEntity(bean, conn, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(bean.getClass().getCanonicalName());
            affectNum=daoExcute.executeUpdate(sql.getSql(),clusterName,ps,Common.getTableName(bean.getClass()),params);
            //affectNum=ps.executeUpdate();
        } catch (Exception e) {
            logger.error("update error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql.getSql(), bean.getClass().getName());
        }
        return affectNum;
    }

    /**
     * 修改指定id的实体对象的属性
     * @param id		           实体对象的id
     * @param valusMap		需要修改的实体对象的属性名称的映射
     * @return				实体对象的Id
     */

    public <I> Object updateByID(Class<?> clazz,I id, Map<String, Object> valuesMap,int timeout) throws Exception {
        StringBuffer updateStatement = new StringBuffer();
        boolean isFirst = true;
        for (Iterator<String> it = valuesMap.keySet().iterator(); it.hasNext();) {
            String fieldName = it.next();
            Object fieldValue = valuesMap.get(fieldName);
            if (!isFirst) {
                updateStatement.append(",");
            }
            updateStatement.append("`" + fieldName + "` = '" + fieldValue + "'");
            isFirst = false;
        }
        SqlInjectHelper.filterSql(updateStatement.toString());
        upateByID(clazz, updateStatement.toString(), id,timeout);

        return id;
    }


    public <I> int upateByID(Class<?> clazz, String updateStatement, I id, int timeOut) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        int affectNum=0;
        try {
            conn = connHelper.get();
            ps = psCreater.createUpdateByID(clazz, conn, updateStatement, id, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(id);
            affectNum=daoExcute.executeUpdate(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            //affectNum=ps.executeUpdate();
        } catch (Exception e) {
            logger.error("update error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return affectNum;
    }

    public int updateByCustom(Class<?> clazz, String updateStatement, String condition, int timeOut) throws Exception {
        condition = SqlInjectHelper.simpleFilterSql(condition);
        updateStatement = SqlInjectHelper.simpleFilterSql(updateStatement);
        int affectNum=0;
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createUpdateByCustom(clazz, conn, updateStatement, condition, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(updateStatement);
            params.add(condition);
            affectNum=daoExcute.executeUpdate(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            //affectNum=ps.executeUpdate();
        } catch (Exception e) {
            logger.error("update error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql.getSql(), condition);
        }
        return affectNum;
    }

    public <I> int  deleteByID(Class<?> clazz, I id, int timeOut) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        int affectNum=0;
        try {
            conn = connHelper.get();
            ps = psCreater.createDelete(clazz, conn, id, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(id);
            affectNum=daoExcute.executeUpdate(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            //affectNum=ps.executeUpdate();
        } catch (Exception e) {
            logger.error("delete error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return affectNum;
    }

    public <I> int deleteByIDS(Class<?> clazz, I[] ids, int timeOut) throws Exception {
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        int affectNum=0;
        try {
            conn = connHelper.get();
            ps = psCreater.createDeleteByIDS(clazz, conn, ids, sql);
            ps.setQueryTimeout(timeOut);

            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            for(int i=0; i<ids.length; i++) {
                params.add(i,ids[i]);
            }
            affectNum=daoExcute.executeUpdate(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            //affectNum=ps.executeUpdate();
        } catch (Exception e) {
            logger.error("delete error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql.getSql(), ids);
        }
        return affectNum;
    }

    public int deleteByCustom(Class<?> clazz, String condition, int timeOut) throws Exception {
        condition = SqlInjectHelper.simpleFilterSql(condition);

        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        int affectNum=0;
        try {
            conn = connHelper.get();
            ps = psCreater.createDeleteByCustom(clazz, conn, condition, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(condition);
            affectNum=daoExcute.executeUpdate(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            //affectNum=ps.executeUpdate();
        } catch (Exception e) {
            logger.error("delete error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return affectNum;
    }

    public <I> Object get(Class<?> clazz, I id, int timeOut) throws Exception {
        Connection conn = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        List<?> dataList = null;
        OutSQL sql = new OutSQL();
        try {
            // 2011-05-24 使用只读连接
//			conn = connHelper.get();
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetEntity(clazz, conn, id, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(id);
            rs=daoExcute.executeQuery(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            //rs = ps.executeQuery();
            dataList = populateData(rs, clazz);
        } catch (Exception e) {
            logger.error("get error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);

        }

        if (dataList != null && dataList.size() > 0) {
            return dataList.get(0);
        } else {
            return null;
        }
    }

    public List<?> getListByCustom(Class<?> clazz, String columns, String condition, String orderBy, int timeOut) throws Exception {
        columns = SqlInjectHelper.simpleFilterSql(columns);
        condition = SqlInjectHelper.simpleFilterSql(condition);
        orderBy = SqlInjectHelper.simpleFilterSql(orderBy);

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<?> dataList = null;
        OutSQL sql = new OutSQL();
        try {
            // 2011-05-24 使用只读连接
//			conn = connHelper.get();
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetByCustom(clazz, conn, columns, condition, orderBy, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(columns);
            params.add(condition);
            params.add(orderBy);
            rs=daoExcute.executeQuery(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);

            //rs = ps.executeQuery();
            dataList = populateData(rs, clazz);
        } catch (SQLException e) {
            logger.error("getListByCustom error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);

        }
        return dataList;
    }

    public List<?> getListByPage(Class<?> clazz, String condition, String columns, int page, int pageSize, String orderBy, int timeOut) throws Exception {

        columns = SqlInjectHelper.simpleFilterSql(columns);
        condition = SqlInjectHelper.simpleFilterSql(condition);
        orderBy = SqlInjectHelper.simpleFilterSql(orderBy);

        Connection conn = null;
        long startTime = System.currentTimeMillis();
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<?> dataList = null;
        OutSQL sql = new OutSQL();
        try {
            // 2011-05-24 使用只读连接
//			conn = connHelper.get();
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetByPage(clazz, conn, condition, columns, page, pageSize, orderBy, sql);
            ps.setQueryTimeout(timeOut);

            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(columns);
            params.add(condition);
            params.add(page);
            params.add(pageSize);
            params.add(orderBy);
            rs=daoExcute.executeQuery(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);

            //rs = ps.executeQuery();
            dataList = populateData(rs, clazz);
        } catch (Exception e) {
            logger.error("getListByPage error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return dataList;
    }

    public int getCount(Class<?> clazz, String condition, int timeOut) throws Exception{
        condition = SqlInjectHelper.simpleFilterSql(condition);

        int count = 0;
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        ResultSet rs = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            // 2011-05-24 使用只读连接
//			conn = connHelper.get();
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetCount(clazz, conn, condition, sql);
            ps.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(clazz.getClass().getCanonicalName());
            params.add(condition);

            rs=daoExcute.executeQuery(sql.getSql(),clusterName,ps,Common.getTableName(clazz),params);
            //rs = ps.executeQuery();
            if(rs.next()){
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            logger.error("getCount error sql:" + sql.getSql(), e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);

        }
        return count;
    }

    public List<Object[]> customSql(String sql, int columnCount, int timeOut) throws Exception {
        List<Object[]> list = new ArrayList<Object[]>();
        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            conn = connHelper.get();
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(sql);
            params.add(columnCount);

            rs=daoExcute.executeQuery(sql,clusterName,stmt,"",params);

            //rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Object[] objAry = new Object[columnCount];
                for(int i=0; i<columnCount; i++){
                    objAry[i] = rs.getObject(i+1);
                }
                list.add(objAry);
            }
        } catch (Exception e) {
            logger.error("sql:" + sql, e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(stmt);
            connHelper.release(conn);
        }
        return list;
    }

    public void customSqlNoReturn(String sql, int timeOut) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connHelper.get();
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeOut);
            List<Object> params = new ArrayList<Object>();
            params.add(sql);


            daoExcute.executeQuery(sql,clusterName,stmt,"",params);
            //stmt.execute(sql);
        } catch (Exception e) {
            logger.error("sql:" + sql, e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(stmt);
            connHelper.release(conn);

        }
    }

    private <T> List<T> populateData(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> dataList = new ArrayList<T>();
        List<Field> fieldList = Common.getAllFields(clazz);

        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsCount = rsmd.getColumnCount();
        List<String> columnNameList = new ArrayList<String>();
        for(int i = 0; i < columnsCount; i++){
            columnNameList.add(rsmd.getColumnLabel(i+1).toLowerCase());
        }

        while (resultSet.next()) {
            T bean = clazz.newInstance();
            for(Field f : fieldList) {
                String columnName = Common.getDBCloumnName(clazz, f).toLowerCase();
                if(columnNameList.contains(columnName)) {
                    Object columnValueObj = null;
                    Class<?> filedCls = f.getType();

                    if(filedCls == int.class || filedCls == Integer.class) {
                        columnValueObj = resultSet.getInt(columnName);
                    } else if(filedCls == String.class) {
                        columnValueObj = resultSet.getString(columnName);
                    } else if(filedCls == boolean.class || filedCls == Boolean.class) {
                        columnValueObj = resultSet.getBoolean(columnName);
                    } else if(filedCls == byte.class || filedCls == Byte.class) {
                        columnValueObj = resultSet.getByte(columnName);
                    } else if(filedCls == short.class || filedCls == Short.class) {
                        columnValueObj = resultSet.getShort(columnName);
                    } else if(filedCls == long.class || filedCls == Long.class) {
                        columnValueObj = resultSet.getLong(columnName);
                    } else if(filedCls == float.class || filedCls == Float.class) {
                        columnValueObj = resultSet.getFloat(columnName);
                    } else if(filedCls == double.class || filedCls == Double.class) {
                        columnValueObj = resultSet.getDouble(columnName);
                    } else if(filedCls == BigDecimal.class) {
                        columnValueObj = resultSet.getBigDecimal(columnName);
                    }

					/*
					 * 记住这次教训啊~~~~~~~~~~~
					else if (filedCls == java.util.Date.class) {
			            columnValueObj = resultSet.getDate(columnName);
			            if (columnValueObj != null) {
			              columnValueObj = new java.util.Date(((java.sql.Date)columnValueObj).getTime());
			            }
			        }
					*/

                    else {
                        columnValueObj = resultSet.getObject(columnName);
                    }

                    if (columnValueObj != null) {
                        Method setterMethod = Common.getSetterMethod(clazz, f);
                        setterMethod.invoke(bean, new Object[] { columnValueObj });
                    }
                }
            }
            dataList.add(bean);
        }
        return dataList;
    }

    /**
     * 从helper迁移过来的方法，统一执行入口。
     */

    public Object execInsert(String sql, IPreparedStatementHandler handler) throws Exception {
        return execWithPara(sql, handler);
    }


    public Object execWithPara(String sql, IPreparedStatementHandler handler) throws Exception {
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        PreparedStatement ps = null;
        try {
            conn = connHelper.get();
            ps = conn.prepareStatement(sql);
            return handler.exec(ps);
        } catch (Exception e) {
            logger.error("execQuery error sql:" + sql, e);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql, null);
        }
    }

    public Object execQuery(String sql, IRowCallbackHandler handler) throws Exception {
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            // 使用只读连接
//			conn = connHelper.get();
            conn = connHelper.getReadConnection();


            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            return handler.exec(rs);
        } catch (Exception e) {
            logger.error("execQuery error sql:" + sql, e);
            throw e;
        }
        finally{
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(stmt);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql, null);
        }
    }

    public Object execProc(String sql, ICallableStatementHandler handler) throws Exception {
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        CallableStatement cs = null;
        try {
            conn = connHelper.get();
            cs = conn.prepareCall(sql);
            return handler.exec(cs);
        } catch (SQLException e) {
            logger.error("execCustomProc error " + sql);
            throw e;
        }
        finally{
            JdbcUitl.closeStatement(cs);
            connHelper.release(conn);
            JdbcUitl.logCollector(System.currentTimeMillis()-startTime,sql, null);
        }
    }

    /**
     * 执行事务任务
     * @param tran
     * @throws Exception
     */
    public void execTransaction(ITransaction tran) throws Exception {
        // 事务开始
        beginTransaction();

        try {
            tran.exec();
            //事务提交
            commitTransaction();
        } catch(Exception ex) {
            //事务回滚
            rollbackTransaction();

            throw ex;
        } finally {
            //事务结束
            endTransaction();
        }
    }

    /**
     * 开启事务(默认级别TRANSACTION_READ_COMMITTED)
     * @throws Exception
     */
    public void beginTransaction() throws Exception {
        beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
    }

    /**
     * 开启事务
     * @param level 事务级别
     * @throws Exception
     */
    public void beginTransaction(int level) throws Exception {
        Connection conn = connHelper.get();
        if(conn != null) {
            try {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(level);
                connHelper.lockConn(conn);
            } catch(Exception ex) {
                logger.error(ex);
            }
        } else {
            throw new Exception("conn is null when beginTransaction");
        }
    }

    /**
     * 提交事务
     * @throws Exception
     */
    public void commitTransaction() throws Exception {
        Connection conn = connHelper.get();
        if(conn != null) {
            conn.commit();
        } else {
            throw new Exception("conn is null when commitTransaction");
        }
    }

    /**
     * 回滚事务
     * @throws Exception
     */
    public void rollbackTransaction() throws Exception {
        Connection conn = connHelper.get();
        if(conn != null) {
            conn.rollback();
        } else {
            throw new Exception("conn is null when rollbackTransaction");
        }
    }

    /**
     * 结束事务
     * @throws Exception
     */
    public void endTransaction() throws Exception {
        Connection conn = connHelper.get();
        if(conn != null) {
            try{
                //恢复默认
                conn.setAutoCommit(true);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            }finally{
                connHelper.unLockConn();
                connHelper.release(conn);
            }
        } else {
            throw new Exception("conn is null when endTransaction");
        }
    }
}
