package com.chexiao.base.dao.monitor;

import com.chexiao.base.ConnectionPool.dbms.PreparedStatementWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by fulei on 2016-12-15.
 */
public class DAOExcuteProxy implements InvocationHandler{

    private static final Log logger = LogFactory.getLog(DAOExcuteProxy.class);
    private Object target;

    private static Map MAP = new ConcurrentHashMap();
    static{
        //ͳ��һ��ʱ���ڵ������sql��ִ�д��� ƽ��ִ��ʱ���ƽ�����ؼ�¼��
        Runnable r = new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Map oldMap = MAP;
                        MAP = new ConcurrentHashMap();
                        List list = new ArrayList(oldMap.values());
                        Collections.sort(list);
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                            SqlInfo sql = (SqlInfo) it.next();
                            long avgts = (sql.getTotalts().get() / (sql.getTotalcount().get()==0?1: sql.getTotalcount().get()));
                            long avgrows = (sql.getTotalrows().get() / (sql.getTotalcount().get()==0?1:sql.getTotalcount().get()));
                      logger.info("SQL :" + sql.getSql() + ",һ��ִ��" + sql.getTotalcount()
                                 + "��,ƽ��ʱ��" + avgts + "����," + "ƽ�����ؼ�¼��" + avgrows);
                        }
                        Thread.sleep(MonitorParameter.getInstance().getMonitorInterval() * 1000);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

        };
        new Thread(r).start();
    }
    /**
     * ��ί�ж��󲢷���һ��������
     * @param target
     * @return
     */
    public Object bind(Object target) {
        this.target = target;
        //ȡ�ô������
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result=null;
        // System.out.println("���￪ʼ"+target.getClass().getCanonicalName()+" mechtod: "+method.getName());


        long startTime = System.currentTimeMillis();
        //ִ�з���
        result=method.invoke(target, args);
        long endTime = System.currentTimeMillis();
        String sql=args[0].toString();
        String clusterName=args[1].toString();
        PreparedStatementWrapper ps =(PreparedStatementWrapper)args[2];
        //  ParameterMetaData pmd=ps.getParameterMetaData();
        String tableName=args[3].toString();
        Object params=args[4];
        // logCollector(endTime-startTime,args[0].toString(),args[1].toString(),args[3]);
//        for(int i=0;i<pmd.getParameterCount();i++) {
//
//                System.out.println(pmd.getParameterClassName(1));//����CLASS����
//                System.out.println(pmd.getParameterType(i));//��������
//                System.out.println(pmd.getParameterTypeName(i));//������������
//
//
//        }

        SqlInfo info = (SqlInfo) MAP.get(sql);
        if (info == null) {
            info = new SqlInfo(sql, params,tableName);
            MAP.put(sql, info);
        }

        Class clazz = result.getClass();

        if (!clazz.isInstance(1)) {
            ResultSet rs=(ResultSet) result;
            rs.last();
            int rowCount=rs.getRow();
            rs.beforeFirst();
            info.logSqlInfo( rowCount, endTime - startTime,params);
        } else {
            info.logSqlInfo(1, endTime - startTime,params);
        }


        //System.out.println("�������");
        return result;
    }
    /**
     * close a ResultSet
     * @param
     */
    public static void logCollector(long time ,String sql ,String clusterName,Object... params) {
        StringBuffer paramStr = new StringBuffer();
        if (time >= 0) {
                for(Object obj:params){
                    Class clazz =obj.getClass();//�õ����Ͷ�Ӧ��Class����
                    if(clazz.isArray()){//�ж��Ƿ�����������
                        int len= Array.getLength(obj);
                        for(int i=0;i<len;i++){
                            Object o=Array.get(obj,i);
                            paramStr.append(Array.get(obj,i)).append(";");
                        }
                    }
                    else{//������������
                        paramStr.append(obj).append(";");
                    }
                }
//                l.append("clusterName", clusterName).append("runTime", time + "").append("sqlStr", sql).append("params", paramStr.toString());
                logger.info(l.toString());
            } else {
//                l.append("clusterName", clusterName).append("runTime", time + "").append("sqlStr", sql).append("params", "null");
//                logger.info(l.toString());
            }
        }

    }
}
