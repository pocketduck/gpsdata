package com.chexiao.base.ConnectionPool;

import com.chexiao.base.ConnectionPool.dbms.AbstractDataSource;
import com.chexiao.base.ConnectionPool.dbms.DataSourceFactory;
import com.chexiao.base.ConnectionPool.dbms.config.ConfigUtil;
import com.chexiao.base.ConnectionPool.dbms.config.DataSourceConfig;

import javax.sql.DataSource;

/**
 * Created by fulei on 2016-12-15.
 */
public class ConnectionPoolFactory {

    /**
     * ���������ļ����õ���Ӧ��ConnectionPool����ConnectionPool��װSWAP DataSourceʵ��
     * @param configPath
     * @return
     * @throws Exception
     */
    public synchronized static ConnectionPool createPool(String configPath) throws Exception{
        //��ȡDataSource ����
        DataSourceConfig dataSourceConfig = ConfigUtil.getDataSourceConfig(configPath);
        //����DataSource���ӳص�Map
        DataSourceFactory.setConfig(dataSourceConfig);
        //�õ�Ĭ�ϵ����ݿ�����
        DataSource datasource = DataSourceFactory.getDataSource("_DEFAULT");
        //ת��Ϊ�����DataSource
        AbstractDataSource aDataSource = (AbstractDataSource) datasource;

        SwapConnectionPool dbConnectionPool = new SwapConnectionPool(aDataSource);

        return dbConnectionPool;

    }


    private ConnectionPoolFactory(){

    }
}
