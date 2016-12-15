package com.chexiao.base.dao;

import com.chexiao.base.dao.interceptor.DAOOperatorProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Created by fulei on 2016-12-15.
 */
public class DAOOperatorFactory {
    private static final Log logger = LogFactory.getLog(DAOOperatorFactory.class);
    private static DAOOperatorProxy proxy ;

    public static IDAOOperator createDaoOperater(String configPath){
        IDAOOperator daoOperator=null;
        try {
            proxy= new DAOOperatorProxy();
            daoOperator = (IDAOOperator) proxy.bind(new DAOOperator(configPath));

        } catch (Exception e) {
            logger.error("Error happend when DaoInit, dbfonfig: " + daoOperator, e);
        }
        return daoOperator;
    }
}
