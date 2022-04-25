package com.soul;

import com.soul.manager.ClientProxy;
import com.soul.manager.RpcClientManager;
import com.soul.services.hello.HelloService;
import com.soul.services.hello.HelloServiceImpl;

public class RpcClient {
    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy(new RpcClientManager());
        HelloService service = clientProxy.getProxy(HelloServiceImpl.class);
        service.sayHello("soul");
    }
}
