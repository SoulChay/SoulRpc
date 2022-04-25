package com.soul.registery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.soul.balance.LoadBalancer;
import com.soul.balance.RoundRobinRule;
import com.soul.exception.ExceptionType;
import com.soul.exception.RpcException;
import com.soul.utils.NacosUtils;

import java.net.InetSocketAddress;
import java.util.List;

public class ServiceDiscoveryImpl implements ServiceDiscovery{

    /**
     * 负载均衡算法
     */
    private final LoadBalancer loadBalancer;

    public ServiceDiscoveryImpl(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer == null ? new RoundRobinRule() : loadBalancer;
    }

    /**
     * 根据服务名称查找 InetSocketAddress
     * @param serviceName
     * @return
     */
    @Override
    public InetSocketAddress lookupServerAddress(String serviceName){
        try {
            List<Instance> instanceList = NacosUtils.getAllInstance(serviceName);
            if (instanceList.size() == 0) {
                throw new RpcException(ExceptionType.SERVICE_NOT_FOUND, serviceName);
            }
            Instance instance = loadBalancer.getInstance(instanceList);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        }catch (NacosException e){
            throw new RpcException(ExceptionType.UNKNOWN_ERROR,e.getMessage());
        }
    }
}
