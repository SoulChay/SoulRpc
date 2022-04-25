package com.soul.manager;

import com.soul.exception.RpcException;
import com.soul.message.RequestMessage;
import com.soul.utils.SequenceIdGenerator;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.Proxy;

/**
 * Client代理类
 *
 * @author soul
 */
public class ClientProxy {

    private RpcClientManager RPC_CLIENT;

    public ClientProxy(RpcClientManager client) {
        this.RPC_CLIENT = client;
    }

    /**
     * 获取代理
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        Object instance = Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(),
                (proxy, method, args) -> {
                    int sequenceId = SequenceIdGenerator.nextId();
                    /* 将方法调用封装为 RequestMessage 对象 */
                    RequestMessage msg = new RequestMessage(
                            sequenceId, clazz.getName(),
                            method.getName(), method.getReturnType(),
                            method.getParameterTypes(), args
                    );

                    /* 将结果存入一个 Promise 对象 */
                    DefaultPromise<Object> promise = new DefaultPromise(RpcClientManager.group.next());
                    RpcClientManager.PROMISES.put(sequenceId, promise);

                    /* 把 RequestMessage 发送给服务端 */
                    RPC_CLIENT.sendRpcRequest(msg);

                    /* 阻塞,等待结果 */
                    promise.await();
                    if (promise.isSuccess()) {
                        return promise.getNow();// 调用正常
                    } else {
                        throw new RpcException(promise.cause()); //调用失败
                    }
                });
        return (T) instance;
    }

}
