package com.chexiao.base.dao.statementcreater;

/**
 * Created by fulei on 2016-12-15.
 */

import com.chexiao.base.dao.util.Common;
import com.chexiao.base.dao.util.OutSQL;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class PSCreaterBase implements IStatementCreater {

    @Override
    public <I> PreparedStatement createGetByIDS(Class<?> clazz,
                                                Connection conn,
                                                I[] ids,
                                                OutSQL sql) throws Exception {
        StringBuffer sbSql = new StringBuffer("SELECT * FROM ");
        sbSql.append(Common.getTableName(clazz));
        sbSql.append(" WHERE ");

        List<Field> fieldList = Common.getIdFields(clazz);
        if (fieldList.size() != 1) {
            throw new Exception("�޷���������ID��ȡ���ݣ����������� �� ���������ϵ�����");
        } else {
            sbSql.append(Common.getDBCloumnName(clazz, fieldList.get(0)));
        }
        sbSql.append(" IN (");
        for(int i=0; i<ids.length; i++) {
            if(i > 0) {
                sbSql.append(",");
            }
            sbSql.append("?");
        }
        sbSql.append(")");

        sql.setSql(sbSql.toString());
        PreparedStatement ps = conn.prepareStatement(sql.getSql());
        int index = 1;
        for(int i=0; i<ids.length; i++, index++) {
            Common.setPara(ps, ids[i], index);
        }

        return ps;
    }

    @Override
    public PreparedStatement createDeleteByCustom(Class<?> clazz,
                                                  Connection conn,
                                                  Map<String, Object> condition,
                                                  OutSQL sql) throws Exception {

        if(condition == null || condition.size() <=0) {
            throw new Exception("delete���������");
        }

        StringBuffer sbSql = new StringBuffer("DELETE FROM ");
        sbSql.append(Common.getTableName(clazz));
        sbSql.append(" WHERE ");
        Set<String> keys = condition.keySet();
        int index = 0;
        for(String key : keys) {
            if(index != 0) {
                sbSql.append(" AND ");
            }
            sbSql.append(key);
            sbSql.append("=?");
        }

        sql.setSql(sbSql.toString());
        PreparedStatement ps = conn.prepareStatement(sql.getSql());
        index = 1;
        for(String key : keys) {
            Common.setPara(ps, condition.get(key), index);
            index++;
        }

        return ps;
    }

    @Override
    public PreparedStatement createGetByCustom(Class<?> clazz,
                                               Connection conn,
                                               String columns,
                                               Map<String, Object> condition,
                                               String orderBy,
                                               OutSQL sql) throws Exception {

        StringBuffer sbSql = new StringBuffer("SELECT ");
        if (columns == null || columns.trim().equals("")) {
            sbSql.append("*");
        } else {
            sbSql.append(columns);
        }
        sbSql.append(" FROM ");
        sbSql.append(Common.getTableName(clazz));

        int index = 0;
        Set<String> keys = null;
        if(condition != null) {
            sbSql.append(" WHERE ");
            keys = condition.keySet();
            for(String key : keys) {
                if(index != 0) {
                    sbSql.append(" AND ");
                }
                sbSql.append(key);
                sbSql.append("=?");
            }
        }

        if(orderBy != null && !orderBy.trim().equals("")) {
            sbSql.append(" ORDER BY ");
            sbSql.append(orderBy);
        }

        sql.setSql(sbSql.toString());

        PreparedStatement ps = conn.prepareStatement(sql.getSql());
        if(condition != null) {
            index = 1;
            for(String key : keys) {
                Common.setPara(ps, condition.get(key), index);
                index++;
            }
        }
        return ps;
    }




    @Override
    public PreparedStatement createGetByPage(Class<?> clazz,
                                             Connection conn,
                                             Map<String, Object> condition,
                                             String columns,
                                             int page,
                                             int pageSize,
                                             String orderBy,
                                             OutSQL sql) throws Exception {
        throw new Exception("�ñ��治֧��");
    }

    @Override
    public PreparedStatement createUpdateByCustom(Class<?> clazz,
                                                  Connection conn,
                                                  Map<String, Object> updateStatement,
                                                  Map<String, Object> condition,
                                                  OutSQL sql) throws Exception {
        throw new Exception("�ñ��治֧��");
    }

    @Override
    public PreparedStatement createGetCount(Class<?> clazz,
                                            Connection conn,
                                            Map<String, Object> condition,
                                            OutSQL sql) throws Exception {
        throw new Exception("�ñ��治֧��");
    }
}
