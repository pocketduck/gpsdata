package com.chexiao.base.dao.monitor;

import com.chexiao.base.dao.DAOOperatorFactory;
import com.chexiao.base.dao.IDAOExcute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Created by fulei on 2016-12-15.
 */
public class DAOExcuteFactory {

    private static final Log logger = LogFactory.getLog(DAOOperatorFactory.class);
    private static DAOExcuteProxy proxy ;

    public static IDAOExcute createDaoExcute(){
        IDAOExcute daoExcute=null;
        try {
            proxy= new DAOExcuteProxy();
            daoExcute = (IDAOExcute) proxy.bind(new DAOExcute());

        } catch (Exception e) {
            logger.error("Error : " + daoExcute, e);
        }
        return daoExcute;
    }
}
