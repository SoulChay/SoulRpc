package com.soul.balance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡算法
 */
public class RoundRobinRule implements LoadBalancer {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 轮询获取实例
     * @param list
     * @return
     */
    @Override
    public Instance getInstance(List<Instance> list) {
        return list.get(getAndIncrement() % list.size());
    }

    /**
     * 防止超过Integer最大值
     *
     * @return
     */
    private final int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.atomicInteger.get();
            next = current < 0x7fffffff ? current + 1 : 0;
        } while (!this.atomicInteger.compareAndSet(current, next));
        return next;
    }
}

