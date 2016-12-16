package com.chexiao.base.dbconnectionpool.dbms.config;

/**
 * Created by fulei on 2016-12-15.
 */
/**
 * һ���������ݿ���������
 * @author
 *
 */
public class DbConfig {
    /**
     * ����������
     */
    private String driversClass;
    /**
     * �����ַ���
     */
    private String connetionURL;
    /**
     * �û���
     */
    private String username;
    /**
     * ����
     */
    private String password;
    /**
     * ���������������
     */
    private int maxPoolSize;
    /**
     * ����������С����
     */
    private int minPoolSize;
    /**
     * ���ó�ʱʱ��(����������ʱ�䳬����ǰ��������л���)
     */
    private int idleTimeout;
    /**
     * ��ѯ�ȴ���ʱ����(�ȴ���ѯ������ʱ���򷵻�)
     */
    private long queryTimeout;

    /**
     * ����/���³�ʱʱ��(�ȴ�����/���³�����ʱ���򷵻�)
     */
    private long insertUpdateTimeout;
    /**
     * ���ݿ��Ƿ�ֻ��
     */
    private boolean readonly = false;

    private int max_threads_per_ds;



    /**
     * ֧��ϵͳ����洢���̵����ݿ����� �����Խ��� IndieDataSource ���ӳ�ʹ��
     */
    private DbConfig managerDbConfig;

    private long releaseInterval;

    private int releaseStrategyValve;

    public DbConfig getManagerDbConfig() {
        return managerDbConfig;
    }
    public void setManagerDbConfig(DbConfig managerDbConfig) {
        this.managerDbConfig = managerDbConfig;
    }
    public String getDriversClass() {
        return driversClass;
    }
    public void setDriversClass(String driversClass) {
        this.driversClass = driversClass;
    }
    public String getConnetionURL() {
        return connetionURL;
    }
    public void setConnetionURL(String connetionURL) {
        this.connetionURL = connetionURL;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    public int getMinPoolSize() {
        return minPoolSize;
    }
    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }
    public int getIdleTimeout() {
        return idleTimeout;
    }
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
    public long getQueryTimeout() {
        return queryTimeout;
    }
    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
    public long getInsertUpdateTimeout() {
        return insertUpdateTimeout;
    }
    public void setInsertUpdateTimeout(long insertUpdateTimeout) {
        this.insertUpdateTimeout = insertUpdateTimeout;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
    public long getReleaseInterval() {
        return releaseInterval;
    }
    public void setReleaseInterval(long releaseInterval) {
        this.releaseInterval = releaseInterval;
    }
    public int getReleaseStrategyValve() {
        return releaseStrategyValve;
    }
    public void setReleaseStrategyValve(int releaseStrategyValve) {
        this.releaseStrategyValve = releaseStrategyValve;
    }

    public int getMax_threads_per_ds() {
        return max_threads_per_ds;
    }
    public void setMax_threads_per_ds(int max_threads_per_ds) {
        this.max_threads_per_ds = max_threads_per_ds;
    }
}
