package com.chexiao.base.ConnectionPool.dbms;

/**
 * Created by fulei on 2016-12-15.
 */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.chexiao.base.ConnectionPool.MessageAlert;
import com.chexiao.base.ConnectionPool.dbms.config.ClusterConfig;
import com.chexiao.base.ConnectionPool.dbms.config.DbConfig;
import com.chexiao.base.ConnectionPool.jsr166.LinkedTransferQueue;
import com.chexiao.base.ConnectionPool.jsr166.TransferQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndieDataSource extends AbstractDataSource {

    private static Logger logger = LoggerFactory.getLogger(IndieDataSource.class);
    private static final int DB_CONNECTION_FAILD = 8001;
    private TransferQueue<DbConfig> dbconfigs;
    private volatile DbDataSource currentDataSource;
    private static int configCount;
    private ConcurrentHashMap<String, DbConfig> managerDataSources;
    private static final String CHECK_DB_STATE_SQL = "{call P_GetMSState(?)}";
    private static final String CHANGE_MASTER_DB_SQL = "{call P_WakeUP(?)}";

    // private static ConcurrentHashMap<String, DbMonitor> monitorMap = new
    // ConcurrentHashMap<String, DbMonitor>();

    // P_Resume @dbName

    public IndieDataSource(ClusterConfig clusterConfig) throws SQLException {
        logger.debug("��ʼ��ʼ���̳߳�");
        dbconfigs = new LinkedTransferQueue<DbConfig>();
        managerDataSources = new ConcurrentHashMap<String, DbConfig>();
        configCount = clusterConfig.getDbConfigList().size();
        for (DbConfig dbconfig : clusterConfig.getDbConfigList()) {
            if (!dbconfig.getDriversClass().startsWith("com.microsoft.sqlserver")) {
                throw new SQLException("��ǰ���ݿ����ӳؽ���֧�� SQL Server ���ݿ�");
            }
            if (dbconfig.getManagerDbConfig() != null) {
                managerDataSources.put(dbconfig.getManagerDbConfig().getConnetionURL(), dbconfig.getManagerDbConfig());
            }
            // monitorMap.put(dbconfig.getConnetionURL(), new DbStateMonitor());
            dbconfigs.add(dbconfig);
        }
        try {
            currentDataSource = createDataSource();
            // currentDataSource.addMonitor(monitorMap.get(currentDataSource.getName()));
        } catch (Exception e) {
            throw new SQLException("ָ����" + clusterConfig.getName() + "�����ļ������쳣:" + e.getMessage());
        }
    }

    private class DbState {
        /**
         * 0 = �ѹ��� �������ݿ��������У���û����������������κ���־�����ݿ�ľ��񸱱������á�
         *
         * 1 = �ѶϿ� ���������ʵ���޷����ӵ�����ʵ�����֤������ʵ����������ڣ�
         *
         * 2 = ����ͬ��
         * �������ݿ�������ͺ����������ݿ�����ݡ����������ʵ���������������ʵ��������־��¼�����Ծ������ݿ�Ӧ�ø��ģ�ʹ��ǰ����
         * �����ݿ⾵��Ự��ʼʱ���������ݿ���������ݿ⴦��ͬ��״̬��
         *
         * 3 = �������ת�� �����������ʵ���ϣ��ֶ�����ת�ƣ���ɫ�л����Ѿ���ʼ�������������ʵ����δ���ܡ�
         *
         * 4 = ��ͬ�� �������ݿ�������������������ݿ���ͬ��ֻ����ͬ��״̬�£����ܽ����ֶ����Զ�����ת�ơ�
         *
         * NULL(-1) = ���ݿ�û�����������������ݿ⾵��Ự������û��Ҫ�ڡ�����ҳ�ϱ���Ļ��
         */
        private int mirroringState = -1;
        /**
         * 1 = �������ݿ� 2 = �������ݿ� NULL(-1) = ���ݿ�û��������
         */
        private int mirroringRole = -1;

        public DbState(int mirroring_state, int mirroring_role) {
            this.mirroringState = mirroring_state;
            this.mirroringRole = mirroring_role;
        }

        public boolean isMasterDb() {
            if (mirroringRole == 1) {
                return true;
            } else {
                return false;
            }
        }

        public boolean canChange() {
            if (mirroringState == 1) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return String.format("mirroringRole:%s mirroringState%s", mirroringRole, mirroringState);
        }

    }

    /**
     * @return ������ݿ�״̬
     * @throws SQLException
     */
    private DbState checkDbState(Connection conn, String dbname) throws SQLException {
        CallableStatement cs = null;
        try {
            cs = conn.prepareCall(CHECK_DB_STATE_SQL);
            cs.setString(1, dbname);
            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                return new DbState(rs.getInt(1), rs.getInt(2));
            } else {
                return new DbState(-1, -1);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    private String getDbName(DbConfig config) {
        String[] vs = config.getConnetionURL().split("DatabaseName=");
        return vs[1];
    }

    /**
     * ִ��������������л�
     */
    private boolean changeMasterDb(Connection conn, String dbname) {
        CallableStatement cs = null;
        try {
            cs = conn.prepareCall(CHANGE_MASTER_DB_SQL);
            cs.setString(1, dbname);
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isSameDbHost(DbConfig config1, DbConfig config2) {
        String host1 = config1.getConnetionURL().split("DatabaseName=")[0];
        String host2 = config2.getConnetionURL().split("DatabaseName=")[0];
        if (host1.equals(host2)) {
            return true;
        } else {
            return false;
        }
    }

    protected DbDataSource createDataSource() throws SQLException {
        DbDataSource datasource = null;
        int i = 0;
        while (datasource == null) {
            DbConfig mconfig = dbconfigs.poll();
            if (mconfig != null) {
                if (datasource == null) {
                    datasource = createDataSource(mconfig);
                }
            }
            dbconfigs.offer(mconfig);
            i++;
            if (i >= configCount) {
                break;
            }
        }
        if (datasource == null) {
            throw new SQLException("δ������Ч�����ݿ�����.", null, DB_CONNECTION_FAILD);
        }
        return datasource;
    }

    private DbDataSource createDataSource(DbConfig dbconfig) {
        long start = System.currentTimeMillis();
        // DbDataSource dbds = new DbDataSource(dbconfig, monitor);
        DbDataSource dbds = new DbDataSource(dbconfig);
        if (dbds.isAlive()) {
            dbds.setName(dbconfig.getConnetionURL());
            // dbds.removeMonitor(monitor);
            logger.info("�������ӳɹ�" + dbconfig.getConnetionURL() + "��ʱ:" + (System.currentTimeMillis() - start));
            return dbds;
        } else {
            logger.info("��������ʧ��" + dbconfig.getConnetionURL() + "��ʱ:" + (System.currentTimeMillis() - start));
            return null;
        }
    }

    @Override
    public Connection GetReadConnection() throws SQLException {
        // TODO Auto-generated method stub
        try {
            Connection con = getDataSource().getConnection();
            return con;
        } catch (SQLException e) {
            if (e.getSQLState().equals("08S01")) {
                currentDataSource.setAlive(false);
            }
            throw new SQLException(e);
        }
    }

    private void reConnection() throws SQLException {
        DbDataSource newDataSource = null;
        // �������е������ļ�,���ҽ�������,ֱ���ҵ�һ�����õ�����
        // ������е����Ӿ����������׳��쳣
        logger.debug("��ʼ�����µ����ݿ�����");
        newDataSource = createDataSource();
        // ���μ��
        if (currentDataSource == null || !currentDataSource.isAlive()) {
            DbConfig mcfg = managerDataSources.get(newDataSource.getConfig().getManagerDbConfig().getConnetionURL());
            DbDataSource mds = new DbDataSource(mcfg);
            String dbname = getDbName(newDataSource.getConfig());
            DbState dstate = checkDbState(mds.getConnection(), dbname);
            mds.shutdown();
            // ��ֹ�п�ָ��ʧЧ ���� �ʹӿⶼ�ܹ�����, ��ʱ����down д��ӿ� �Ӷ����˫д�Ŀ�����
            if (dstate.isMasterDb()) {
                // �Ƴ�ԭ�������ӳصļ��
                if (!newDataSource.getName().equals(currentDataSource.getName())) {
                    MessageAlert.Factory.get().sendMessage("д�п⵽:" + newDataSource.getConfig().getConnetionURL());
                    currentDataSource.shutdown();
                }
                // newDataSource.addMonitor(monitorMap.get(newDataSource.getName()));
                currentDataSource = newDataSource;
                currentDataSource.setAlive(true);
            } else {
                throw new SQLException("��⵱ǰ���ӹ�ϵ�쳣,��ǰ����ΪSlave���ݿ�, Master���ݿ��޷�����.", null, 1455);
            }
        }
    }

    private void changeMasterDb() throws SQLException {
        logger.info("�������ݿ�����ʧ�ܣ������п�");
        for (String key : managerDataSources.keySet()) {
            DbConfig mcfg = managerDataSources.get(key);
            if (!isSameDbHost(currentDataSource.getConfig(), mcfg)) {
                DbDataSource mds = null;
                try {
                    logger.debug("��ʼ����DBManager����:" + mcfg.getConnetionURL());
                    mds = new DbDataSource(mcfg);
                    logger.debug("����DBManager���:" + mds.isAlive());
                    String dbname = getDbName(currentDataSource.getConfig());
                    DbState dstate = checkDbState(mds.getConnection(), dbname);
                    logger.info("������ݿ�: " + mds.getName() + "����״̬--" + dstate.toString());
                    if (!dstate.isMasterDb() && dstate.canChange()) {
                        if (currentDataSource != null && currentDataSource.isAlive()) {
                            logger.info("�������ݿ�����ˣ������л��ˡ�");
                        } else {
                            boolean state = changeMasterDb(mds.getConnection(), dbname);
                            logger.info("ִ����д�п�洢����,ִ��״̬:" + (state ? "�ɹ�" : "ʧ��"));
                            if (state) {
                                MessageAlert.Factory.get().sendMessage("�������ݿ�[" + dbname + "]�п�洢����ִ�гɹ�,�����½������ӹ�ϵ.");
                            }
                        }
                    }
                } finally {
                    mds.shutdown();
                }
            }
        }
    }

    AtomicBoolean locked = new AtomicBoolean(false);

    public DbDataSource getDataSource() throws SQLException {
        logger.debug("��ǰ���ݿ����ӣ�" + currentDataSource.toString());
        if (currentDataSource != null && currentDataSource.isAlive()) {
            return currentDataSource;
        }

        if (locked.get()) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            if (currentDataSource != null && currentDataSource.isAlive()) {
                return currentDataSource;
            }
            throw new SQLException("���ݿ��쳣,���ڼ����,��ǰ�޿�������", "08S01");
        }

        synchronized (this) {
            logger.debug("���ǰ���� ,��ʼ���������ݿ�����");
            if (currentDataSource != null && currentDataSource.isAlive()) {
                return currentDataSource;
            }
            locked.set(true);
            try {
                reConnection();
                return currentDataSource;
            } catch (SQLException e) {
                // ���е����Ӿ�������, ����: master �������쳣��, ����slave��û������
                // �ֻ��� �����������쳣,master �� slave�����ܷ���. �˴����뱣֤ ���pmc�������ظ��ķ���ָ��

                if (e.getErrorCode() != DB_CONNECTION_FAILD)
                    throw new SQLException("�޿�������", e.getSQLState() + "_" + e.getErrorCode(), e);
                try {
                    changeMasterDb();
                    reConnection();
                    return currentDataSource;
                } catch (SQLException e1) {
                    throw new SQLException("�п�����޿�������", e1.getSQLState() + "_" + e1.getErrorCode(), e1);
                } finally {
                    locked.set(false);
                }
            } finally {
                locked.set(false);
            }
        }

    }

    public Connection getConnection() throws SQLException {
        return this.getDataSource().getConnection();
    }
}
