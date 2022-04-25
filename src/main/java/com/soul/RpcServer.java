package com.soul;

import com.soul.annotation.RpcServiceScan;
import com.soul.config.Config;
import com.soul.manager.RpcServerManager;

@RpcServiceScan
public class RpcServer {
    public static void main(String[] args) {
        new RpcServerManager("127.0.0.1", Config.getServerPort()).start();
    }
}
