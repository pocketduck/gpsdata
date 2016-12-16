package com.chexiao.base.dao.monitor;

/**
 * Created by fulei on 2016-12-15.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLong;

public class SqlInfo implements Comparable {

    public static Logger LOG = LoggerFactory.getLogger(SqlInfo.class);


    private Object param;
    private String sql;

    private String tableName;

    private AtomicLong totalts = new AtomicLong(0);
    private AtomicLong totalcount = new AtomicLong(0);
    private AtomicLong totalrows = new AtomicLong(0);



    public SqlInfo(String sql, Object param,String tableName) {
        this.sql = sql;
        this.param=param;
        this.tableName=tableName;
    }


    public String getTableName(){
        return this.tableName;
    }

    public void logSqlInfo(int rows, long time,Object... param) {
        totalts.addAndGet(time);
        totalcount.getAndIncrement();
        totalrows.addAndGet(rows);
        StringBuffer paramStr = new StringBuffer();
        if (time > MonitorParameter.getInstance().getMaxts() || rows > MonitorParameter.getInstance().getMaxrows()) {
            for(Object obj:param){
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
//            l.append("tableName",this.tableName).append("sql",this.sql).append("para",paramStr.toString()).append("ts",time+"").append("rows",rows+"");
            LOG.warn("表名:" + this.tableName);
            LOG.warn("SQL:" + this.sql);
            LOG.warn("参数:" + paramStr.toString());
            LOG.warn("执行时间:" + time);
            LOG.warn("返回记录:" + rows);
        }
    }

    public String getSql() {
        return sql;
    }



    public AtomicLong getTotalts() {
        return totalts;
    }

    public AtomicLong getTotalcount() {
        return totalcount;
    }

    public AtomicLong getTotalrows() {
        return totalrows;
    }




    public int compareTo(SqlInfo o) {
        Long ts = this.getTotalcount().get();
        Long ots = o.getTotalcount().get();
        return ts.compareTo(ots);
    }

    public int compareTo(Object o) {
        return 0;
    }
}
