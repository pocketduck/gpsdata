package com.chexiao.base.ConnectionPool.dbms.config;

/**
 * Created by fulei on 2016-12-15.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.chexiao.base.ConnectionPool.MessageAlert;
import com.chexiao.base.ConnectionPool.dbms.AbstractDataSource;
import org.ho.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据yaml配置文件获得DataSourceConfig的配置文件
 *
 * @author
 *
 */
public class ConfigUtil {
    private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    /**
     * 根据yaml配置文件获得DataSourceConfig的配置文件
     *
     * @param filePath
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static DataSourceConfig getDataSourceConfig(String filePath) throws SQLException {

        // 2011-06-11
        MessageAlert.Factory.setConfig((new File(filePath)).getParent());

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        ClusterConfig clusterConfig = new ClusterConfig();

        List<HashMap<String, Object>> obj;
        try {
            obj = (List<HashMap<String, Object>>) Yaml.loadType(new File(filePath), ArrayList.class);
            for (HashMap<String, Object> config : obj) {
                DbConfig dbConfig = assemblyDbConfig(config);
                String dataSource = String.valueOf(config.get("DataSource"));
                if (!dataSource.equals("null")) {
                    try {
                        Class dsclazz = Class.forName(dataSource);
                        if (AbstractDataSource.class.isAssignableFrom(dsclazz)) {
                            clusterConfig.setDataSource(dataSource);
                            if (dataSource.equals("com.bj58.spat.core.dbms.IndieDataSource")) {
                                if (!dbConfig.getDriversClass().startsWith("com.microsoft.sqlserver")) {
                                    throw new SQLException("当前使用的数据库连接池仅仅支持 SQL Server 数据库");
                                }
                                Object mobj = config.get("managerDbConfig");
                                if (mobj != null) {
                                    DbConfig managerDbConfig = null;
                                    for (HashMap<String, Object> mconfig : (List<HashMap<String, Object>>) mobj) {
                                        managerDbConfig = assemblyDbConfig(mconfig);
                                        break;
                                    }
                                    if (managerDbConfig != null) {
                                        String conulr = dbConfig.getConnetionURL();
                                        String mconurl = managerDbConfig.getConnetionURL();
                                        String conhost = conulr.replaceAll("DatabaseName=[\\s\\S]+", "");
                                        String mconhost = mconurl.replaceAll("DatabaseName=[\\s\\S]+", "");
                                        if (!conulr.equals(mconurl) && conhost.equals(mconhost)) {
                                            dbConfig.setManagerDbConfig(managerDbConfig);
                                        } else {
                                            logger.warn("配置'P_WakeUP'存储过程所在数据库不合法,当主数据库发生异常时将无法自动切换到从库.");
                                        }
                                    }
                                }
                                if (dbConfig.getManagerDbConfig() == null) {
                                    logger.warn("未配置'P_WakeUP'存储过程所在数据库,当主数据库发生异常时将无法自动切换到从库.");
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
                clusterConfig.addDbConfig(dbConfig);
                clusterConfig.setName("_DEFAULT");
            }
            dataSourceConfig.addClusterConfig(clusterConfig);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dataSourceConfig;
    }

    private static DbConfig assemblyDbConfig(HashMap<String, Object> config) {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setConnetionURL((String) config.get("connectionURL"));
        dbConfig.setDriversClass((String) config.get("driversClass"));
        dbConfig.setUsername(String.valueOf(config.get("username")));
        dbConfig.setPassword(String.valueOf(config.get("password")));
        dbConfig.setIdleTimeout(Integer.parseInt(String.valueOf(config.get("idleTimeout"))));
        dbConfig.setInsertUpdateTimeout(Integer.parseInt(String.valueOf(config.get("insertUpdateTimeout"))));
        dbConfig.setMaxPoolSize(Integer.parseInt(String.valueOf(config.get("maxPoolSize"))));
        dbConfig.setMinPoolSize(Integer.parseInt(String.valueOf(config.get("minPoolSize"))));
        dbConfig.setQueryTimeout(Integer.parseInt(String.valueOf(config.get("queryTimeout"))));
        String maxThreadsPerDs=String.valueOf(config.get("maxThreadsPerDs")==null?5:config.get("maxThreadsPerDs"));
        dbConfig.setMax_threads_per_ds(Integer.parseInt(maxThreadsPerDs));


        boolean readonlyValue = false;
        String readonly = String.valueOf(config.get("readonly"));
        if (readonly != null && readonly.equals("true"))
            readonlyValue = true;


        dbConfig.setReadonly(readonlyValue);
        if(config.get("releaseInterval") != null)
            dbConfig.setReleaseInterval(Long.parseLong(String.valueOf(config.get("releaseInterval"))));

        if(config.get("releaseStrategyValve") != null)
            dbConfig.setReleaseStrategyValve(Integer.parseInt(String.valueOf(config.get("releaseStrategyValve"))));

        dbConfig.setReadonly(readonlyValue);
        return dbConfig;
    }

    /**
     * 根据yaml配置文件获得DataSourceConfig的配置文件
     *
     * @param filePath
     * @return
     */
    public static DataSourceConfig getDataSourceConfig(List<HashMap<String, Object>> obj) {

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        ClusterConfig clusterConfig = new ClusterConfig();
        for (HashMap<String, Object> config : obj) {
            DbConfig dbConfig = new DbConfig();
            dbConfig.setConnetionURL((String) config.get("connectionURL"));
            dbConfig.setDriversClass((String) config.get("driversClass"));
            dbConfig.setUsername(String.valueOf(config.get("username")));
            dbConfig.setPassword(String.valueOf(config.get("password")));
            dbConfig.setIdleTimeout(Integer.parseInt(String.valueOf(config.get("idleTimeout"))));
            dbConfig.setInsertUpdateTimeout(Integer.parseInt(String.valueOf(config.get("insertUpdateTimeout"))));
            dbConfig.setMaxPoolSize(Integer.parseInt(String.valueOf(config.get("maxPoolSize"))));
            dbConfig.setMinPoolSize(Integer.parseInt(String.valueOf(config.get("minPoolSize"))));
            dbConfig.setQueryTimeout(Integer.parseInt(String.valueOf(config.get("queryTimeout"))));

            boolean readonlyValue = false;
            String readonly = String.valueOf(config.get("readonly"));
            if (readonly != null && readonly.equals("true"))
                readonlyValue = true;

            dbConfig.setReadonly(readonlyValue);
            if(config.get("releaseInterval") != null)
                dbConfig.setReleaseInterval(Long.parseLong(String.valueOf(config.get("releaseInterval"))));

            if(config.get("releaseStrategyValve") != null)
                dbConfig.setReleaseStrategyValve(Integer.parseInt(String.valueOf(config.get("releaseStrategyValve"))));

            clusterConfig.addDbConfig(dbConfig);
            clusterConfig.setName("_DEFAULT");
        }
        dataSourceConfig.addClusterConfig(clusterConfig);

        return dataSourceConfig;
    }

    public static ClusterConfig getClusterConfig(String filePath) {
        // TODO: getClusterConfig,现在就需要实现
        return null;
    }

    public static DbConfig getDbConfig(String filePath) {
        // TODO: getDbConfig
        return null;

    }
}
