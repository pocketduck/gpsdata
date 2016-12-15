package com.chexiao.base.ConnectionPool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */
import com.chexiao.base.ConnectionPool.dbms.config.ClusterConfig;

import java.sql.SQLException;
public class ReadAndWriteDataSource extends ClusterDataSource {

    /**
     * DataSourceCloud �Ĺ��캯�� ����DataSourceConfig ���г�ʼ������ ��ͬʱ��ʼ�� ���Դ�ļ���
     *
     * @param dataSourceConfig
     * @throws Exception
     */
    public ReadAndWriteDataSource(ClusterConfig dataSourceConfig) throws Exception {
        super(dataSourceConfig);
        for(DbDataSource ds : dbDataSources){
            if(ds.getConfig().isReadonly()){
                currentReadDataSource = ds;
                break;
            }
        }
    }

    protected DbDataSource getReadDataSource() throws SQLException{
        if (currentReadDataSource != null && currentReadDataSource.isAlive()) {
            return currentReadDataSource;
        }

        for(int index = 0; index < dbDataSources.size(); index ++) {
            DbDataSource dataSource = dbDataSources.get(index);
            if(dataSource == null) continue;
            if (dataSource.isAlive() && (!dataSource.isFull())){
                return dataSource;
            }
        }

        throw new SQLException("SWAP no available datasource. " + this ,"08S01");
    }
}

