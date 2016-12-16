package com.chexiao.base.dbconnectionpool;

import com.chexiao.base.dbconnectionpool.dbms.AbstractDataSource;
import com.chexiao.base.dbconnectionpool.dbms.DataSourceFactory;
import com.chexiao.base.dbconnectionpool.dbms.config.ConfigUtil;
import com.chexiao.base.dbconnectionpool.dbms.config.DataSourceConfig;

import javax.sql.DataSource;

/**
 * Created by fulei on 2016-12-15.
 */
public class ConnectionPoolFactory {

    /**
     * 根据配置文件，得到对应的ConnectionPool，该ConnectionPool封装SWAP DataSource实现
     * @param configPath
     * @return
     * @throws Exception
     */
    public synchronized static ConnectionPool createPool(String configPath) throws Exception{
        //获取DataSource 配置
        DataSourceConfig dataSourceConfig = ConfigUtil.getDataSourceConfig(configPath);
        //设置DataSource连接池的Map
        DataSourceFactory.setConfig(dataSourceConfig);
        //得到默认的数据库连接
        DataSource datasource = DataSourceFactory.getDataSource("_DEFAULT");
        //转化为抽象的DataSource
        AbstractDataSource aDataSource = (AbstractDataSource) datasource;

        SwapConnectionPool dbConnectionPool = new SwapConnectionPool(aDataSource);

        return dbConnectionPool;

    }


    private ConnectionPoolFactory(){

    }
}
