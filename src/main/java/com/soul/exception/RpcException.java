package com.soul.exception;

/**
 * 自定义Rpc异常
 */
public class RpcException extends RuntimeException {

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(ExceptionType type) {
        super(type.getMessage());
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(ExceptionType type, String detail) {
        super(type.getMessage() + ": " + detail);
    }


}
