package com.soul.registery;

import com.soul.utils.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 钩子函数
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    private static final ShutdownHook shutdownHook = new ShutdownHook();

    public static ShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    public void addClearServicesHook() {
        logger.info("关闭后将自动注销所有服务");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            NacosUtils.clearRegister();
        }));
    }
}
