package com.soul.message;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class RequestMessage extends Message {

    /**
     * 调用的接口全限定名，服务端根据它找到实现
     */
    private String interfaceName;

    /**
     * 调用接口中的方法名
     */
    private String methodName;

    /**
     * 方法返回类型
     */
    private Class<?> returnType;

    /**
     * 方法参数类型数组
     */
    private Class[] parameterTypes;

    /**
     * 方法参数值数组
     */
    private Object[] parameterValue;

    /**
     * 获取消息类型
     */
    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }


    public RequestMessage(int sequenceId, String interfaceName, String methodName,
                          Class<?> returnType, Class[] parameterTypes, Object[] parameterValue) {
        super.setSequenceId(sequenceId);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
    }
}
