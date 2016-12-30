package com.chexiao.gpsdata.dao.helper;


import com.chexiao.base.dao.DAOOperator;
import com.chexiao.gpsdata.tools.ConfigLoadTool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class GpsDataDBHelper {
	private static final Log logger = LogFactory.getLog(GpsDataDBHelper.class);

	public static DAOOperator[] daoHelperArr = null;


	static {
		try {
			daoHelperArr = new DAOOperator[2];
			daoHelperArr[0] = new DAOOperator(ConfigLoadTool.getConfigFilePath("gpsdb_0.properties"));
			daoHelperArr[1] = new DAOOperator(ConfigLoadTool.getConfigFilePath("gpsdb_1.properties"));

			logger.info("gpsdb初始化ok");
		} catch (Exception ex) {
			logger.error("初始化gpsdb Error  ", ex);
		}
	}

	/**
	 * 根据用户设备iid返回 数据库
	 * @param deviceId
	 * @return DAOOperator
	 */
	public static DAOOperator getDAOHelperByDeviceId(long deviceId) {
		if (null == daoHelperArr || deviceId < 0) {
			throw new IllegalArgumentException("getDAOHelper error ");
		}
		DAOOperator operator = daoHelperArr[mod(deviceId, daoHelperArr.length)];
		return operator;
	}


	public static DAOOperator[] getAllDAOHelper() {
		return daoHelperArr;
	}


	/**
	 * 根据id取模
	 * @param id
	 * @param len
	 * @return
	 */
	public static int mod(long id, int len) {
		return Math.abs((Long.valueOf(id).hashCode()) % len);
	}

}
