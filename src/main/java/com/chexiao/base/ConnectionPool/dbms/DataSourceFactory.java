package com.chexiao.base.ConnectionPool.dbms;

import com.chexiao.base.ConnectionPool.dbms.config.ClusterConfig;
import com.chexiao.base.ConnectionPool.dbms.config.DataSourceConfig;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fulei on 2016-12-15.
 */
public class DataSourceFactory {
    /**
     * ϵͳ��ǰ���е����ݿ��Ⱥ
     */
    private static Map<String, AbstractDataSource> clouds = new HashMap<String, AbstractDataSource>();

    /**
     * ����datasource���ƻ��datasource
     *
     * @param dataSourceName
     *            ��Сд������
     * @return
     * @throws Exception
     */

    public static DataSource getDataSource(String dataSourceName) throws Exception {

        return getAbstractDataSource(dataSourceName);

        // ClusterDataSource dataSource = clouds.get(dataSourceName);
        // if (dataSource != null)
        // return dataSource;
        //
        // throw new
        // Exception("there is no dataSourceConfig for "+dataSourceName);
    }

    public static AbstractDataSource getAbstractDataSource(String dataSourceName) throws Exception {
        AbstractDataSource dataSource = clouds.get(dataSourceName);

        if (dataSource != null)
            return dataSource;

        throw new Exception("there is no dataSourceConfig for " + dataSourceName);
    }

    /**
     * ���������ļ�
     *
     * @param config
     * @throws Exception
     */
    public synchronized static void setConfig(DataSourceConfig config) throws Exception {

        if (clouds.size() != 0) {
            clouds.clear();
        }

        Map<String, ClusterConfig> clusters = config.getDataSourceConfig();
        for (String key : clusters.keySet()) {
            String clazzName = clusters.get(key).getDataSource();
            Object obj = Class.forName(clazzName).getConstructor(ClusterConfig.class).newInstance(clusters.get(key));
            if (obj instanceof AbstractDataSource) {
                clouds.put(key, (AbstractDataSource) obj);
            }
        }
    }

    /**
     * ˽�й��캯��
     */
    private DataSourceFactory() {
    }
}
