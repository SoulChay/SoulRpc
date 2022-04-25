package com.soul.registery;

import com.alibaba.nacos.api.exception.NacosException;
import com.soul.exception.ExceptionType;
import com.soul.exception.RpcException;
import com.soul.utils.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * nacos注册
 */
public class ServiceRegistryImpl implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryImpl.class);

    /**
     * 服务注册
     * @param serviceName
     * @param inetSocketAddress
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            NacosUtils.registerServer(serviceName,inetSocketAddress);
            System.out.println("注册"+serviceName);
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生:", e);
            throw new RpcException(ExceptionType.REGISTER_SERVICE_FAILED);
        }
    }

}
