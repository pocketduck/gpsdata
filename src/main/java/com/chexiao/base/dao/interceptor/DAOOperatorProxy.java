package com.chexiao.base.dao.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by fulei on 2016-12-15.
 */
public class DAOOperatorProxy {
    private static final Log logger = LogFactory.getLog(DAOOperatorProxy.class);
    private Object target;

    /**
     * ��ί�ж��󲢷���һ��������
     * @param target
     * @return
     */
    public Object bind(Object target) {
        this.target = target;
        //ȡ�ô������
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result=null;
        // System.out.println("���￪ʼ"+target.getClass().getCanonicalName()+" mechtod: "+method.getName());

//        for(Object obj:args){
//            Class clazz =obj.getClass();//�õ����Ͷ�Ӧ��Class����
//            if(clazz.isArray()){//�ж��Ƿ�����������
//                int len=Array.getLength(obj);
//                for(int i=0;i<len;i++){
//                    System.out.println(Array.get(obj,i));
//                }
//            }
//            else{//������������
//                System.out.println( obj);
//            }
//
//        }
        //ִ�з���
        result=method.invoke(target, args);

        //   System.out.println("�������");
        return result;
    }
}
