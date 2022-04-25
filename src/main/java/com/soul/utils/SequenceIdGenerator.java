package com.soul.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理序列id
 */
public abstract class SequenceIdGenerator {

    private static final AtomicInteger id = new AtomicInteger();

    /**
     * 拿到 下一个序列id
     * @return
     */
    public static int nextId() {
        return id.incrementAndGet();
    }
}
