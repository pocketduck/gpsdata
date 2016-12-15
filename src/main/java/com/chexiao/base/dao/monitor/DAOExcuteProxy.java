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
        //统计一段时间内的请求的sql和执行次数 平均执行时间和平均返回记录数
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
                      logger.info("SQL :" + sql.getSql() + ",一共执行" + sql.getTotalcount()
                                 + "次,平均时间" + avgts + "毫秒," + "平均返回记录数" + avgrows);
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
     * 绑定委托对象并返回一个代理类
     * @param target
     * @return
     */
    public Object bind(Object target) {
        this.target = target;
        //取得代理对象
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result=null;
        // System.out.println("事物开始"+target.getClass().getCanonicalName()+" mechtod: "+method.getName());


        long startTime = System.currentTimeMillis();
        //执行方法
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
//                System.out.println(pmd.getParameterClassName(1));//参数CLASS名字
//                System.out.println(pmd.getParameterType(i));//参数类型
//                System.out.println(pmd.getParameterTypeName(i));//参数类型名字
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


        //System.out.println("事物结束");
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
                    Class clazz =obj.getClass();//得到类型对应的Class对象
                    if(clazz.isArray()){//判断是否是数组类型
                        int len= Array.getLength(obj);
                        for(int i=0;i<len;i++){
                            Object o=Array.get(obj,i);
                            paramStr.append(Array.get(obj,i)).append(";");
                        }
                    }
                    else{//不是数组类型
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
