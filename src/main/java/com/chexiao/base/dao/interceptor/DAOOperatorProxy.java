package com.chexiao.base.dao.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by fulei on 2016-12-15.
 */
public class DAOOperatorProxy implements InvocationHandler{
    private static final Log logger = LogFactory.getLog(DAOOperatorProxy.class);
    private Object target;

    /**
     * 绑定委托对象并返回一个代理类
     * @param target
     * @return
     */
    public Object bind(Object target) {
        this.target = target;
        //取得代理对象
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result=null;
        // System.out.println("事物开始"+target.getClass().getCanonicalName()+" mechtod: "+method.getName());

//        for(Object obj:args){
//            Class clazz =obj.getClass();//得到类型对应的Class对象
//            if(clazz.isArray()){//判断是否是数组类型
//                int len=Array.getLength(obj);
//                for(int i=0;i<len;i++){
//                    System.out.println(Array.get(obj,i));
//                }
//            }
//            else{//不是数组类型
//                System.out.println( obj);
//            }
//
//        }
        //执行方法
        result=method.invoke(target, args);

        //   System.out.println("�������");
        return result;
    }
}
