package com.soul.message;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 定义消息模板
 */
@Data
public abstract class Message implements Serializable {

    /**
     * 消息类型
     */
    private int messageType;

    /**
     * 消息序列id
     */
    private int sequenceId;

    /**
     * 请求和响应类型 byte 值
     */
    public static final int RPC_MESSAGE_TYPE_REQUEST = 1;
    public static final int RPC_MESSAGE_TYPE_RESPONSE = 2;

    /**
     * 存储 消息类型 + 消息
     */
    private static final Map<Integer, Class<? extends Message>> messageClasses = new HashMap<>();

    static {
        messageClasses.put(RPC_MESSAGE_TYPE_REQUEST,RequestMessage.class);
        messageClasses.put(RPC_MESSAGE_TYPE_RESPONSE,ResponseMessage.class);
    }

    /**
     * 获取消息类型
     */
    public abstract int getMessageType();

    /**
     * 获取消息类型
     */
    public static Class<? extends Message> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }


}
