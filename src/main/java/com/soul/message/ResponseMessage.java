package com.soul.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class ResponseMessage extends Message {

    /**
     * 返回值
     */
    private Object returnValue;

    /**
     * 异常值
     */
    private Exception exceptionValue;

    /**
     * 获取消息类型
     */
    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
