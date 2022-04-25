package com.soul.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionType {

    CLEAR_REGISTER_FAILURE("注销服务失败"),
    UNKNOWN_ERROR("出现未知错误"),
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    REGISTER_SERVICE_FAILED("注册服务失败"),
    SERIALIZER_FAILURE("序列化失败"),
    DESERIALIZER_FAILURE("反序列化失败"),
    FAILED_TO_CONNECT_TO_SERVICE_REGISTRY("连接注册中心失败"),
    SERVICE_NOT_FOUND("找不到对应的服务"),
    SERVICE_INVOCATION_FAILURE("服务调用出现失败"),
    SERVICE_SCAN_PACKAGE_NOT_FOUND("启动类未添加@ServiceScan注解"),
    START_CLASS_NOT_FOUND("未找到启动类");

    private final String message;

}
