package com.chexiao.base.dbconnectionpool.dbms;

import com.chexiao.base.dbconnectionpool.dbms.config.ClusterConfig;
import com.chexiao.base.dbconnectionpool.dbms.config.DataSourceConfig;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Datasource的类工厂
 * Created by fulei on 2016-12-15.
 */
public class DataSourceFactory {
    /**
     * 系统当前所有的数据库池群
     */
    private static Map<String, AbstractDataSource> clouds = new HashMap<String, AbstractDataSource>();

    /**
     * 根据datasource名称获得datasource
     *
     * @param dataSourceName
     *            大小写不敏感
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
     * 设置配置文件
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
     * 私有构造函数
     */
    private DataSourceFactory() {
    }
}
