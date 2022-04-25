package com.soul.services.hello;

import com.soul.annotation.RpcService;

@RpcService
public class HelloServiceImpl implements HelloService{

    @Override
    public String sayHello(String name) {
        return "Hello!" + name;
    }
}
