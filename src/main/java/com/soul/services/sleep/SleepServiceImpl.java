package com.soul.services.sleep;

import com.soul.annotation.RpcService;

@RpcService
public class SleepServiceImpl implements SleepService {
    @Override
    public String sayGoodNight(String msg) {
        return "晚安呀, " + msg;
    }
}