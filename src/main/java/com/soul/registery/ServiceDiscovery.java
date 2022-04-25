package com.soul.registery;

import com.alibaba.nacos.api.exception.NacosException;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找 InetSocketAddress
     */
    InetSocketAddress lookupServerAddress(String serviceName) throws NacosException;

}
