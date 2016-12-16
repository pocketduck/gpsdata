package com.chexiao.base.util;

import org.apache.log4j.Logger;


public class UniqueIDUtils {
	private static final Logger logger = Logger.getLogger(UniqueIDUtils.class);
	public static IdWorker w =  null;
	static {
		long workerId = Long.valueOf(PropertyUtil.getProperty("workerId","2"));
		long datacenterId = Long.valueOf(PropertyUtil.getProperty("datacenterId","1"));
		if (workerId <= 0) {
			logger.error("server id error, please check config file!");
			System.exit(-1);
		}
		w = new IdWorker(workerId, datacenterId);
	}
	/**
	 * 获取主键ID
	 *
	 * @return
	 * @throws Exception
	 */
	public static synchronized long getUniqueID() throws Exception {
		return w.nextId();
	}
}
