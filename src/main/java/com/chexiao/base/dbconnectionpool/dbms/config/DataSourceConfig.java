package com.chexiao.base.dbconnectionpool.dbms.config;

/**
 * Created by fulei on 2016-12-15.
 */
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceConfig {
    /**
     * ϵͳ��ǰ����������Դ������Ϊ����Դ�ķ������ƣ�ֵΪ�÷����������Ķ�����Դ���󼯺ϡ�
     */
    Map<String, ClusterConfig> clusterConfigMap = new ConcurrentHashMap<String, ClusterConfig>();

    public void addClusterConfig(ClusterConfig clusterConfig){
        if (clusterConfig == null) return;
        clusterConfigMap.put(clusterConfig.getName(), clusterConfig);
    }



    /**
     * ����datasource����ö�Ӧ�����ļ�
     * @param datasourceName
     * @return
     */


    public ClusterConfig getDataSourceConfig(String datasourceName){
        return clusterConfigMap.get(datasourceName);
    }
    /**
     * ����ȫ��ClusterConfig����
     * @return
     */
    public Map<String, ClusterConfig> getDataSourceConfig(){
        return clusterConfigMap;
    }
}
