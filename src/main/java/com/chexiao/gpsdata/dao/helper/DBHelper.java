package com.chexiao.gpsdata.dao.helper;


import com.chexiao.base.dao.DAOOperator;
import com.chexiao.gpsdata.tools.ConfigLoadTool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DBHelper {
	private static DAOOperator daoOperator = null;
	private static final Log logger = LogFactory.getLog(DBHelper.class);
	static {
		try {

			String configFile = ConfigLoadTool.getConfigFilePath("db.properties");
			logger.info("db初始化 DBPath:"+configFile);
			daoOperator = new DAOOperator(configFile);
			logger.info("db初始化ok");
		} catch (Exception ex) {
			logger.error("初始化DB连接Error ", ex);
		}
	}
	
	public static DAOOperator getDaoOperator(){
		return daoOperator;
	}
}
