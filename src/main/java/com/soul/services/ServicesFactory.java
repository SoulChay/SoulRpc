package com.soul.services;

import com.soul.exception.ExceptionType;
import com.soul.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServicesFactory {

    /**
     * 存储service
     */
    private static final Map<String, Object> servicesFactory = new ConcurrentHashMap<>();


    /**
     * 添加已注解的类进入工厂
     */
    public static <T> void addService(T service, String serviceName) {
        if (servicesFactory.containsKey(serviceName)) {
            return;
        }
        servicesFactory.put(serviceName, service);
        log.debug("工厂添加服务 {}", serviceName);
    }


    /**
     * 从工厂里获取Service
     *
     * @param serviceName
     * @return
     */
    public static Object getService(String serviceName) {
        Object service = servicesFactory.get(serviceName);
        if (service == null) {
            log.debug("工厂内未发现 {} 服务");
            throw new RpcException(ExceptionType.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
