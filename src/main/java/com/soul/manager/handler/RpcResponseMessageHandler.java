package com.soul.manager.handler;

import com.soul.manager.RpcClientManager;
import com.soul.message.ResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理RpcResponse相应的处理器
 *
 * @author chenlei
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<ResponseMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseMessage msg) throws Exception {
        try {
            log.debug("收到：{}", msg);
            Promise<Object> promise = RpcClientManager.PROMISES.remove(msg.getSequenceId());
            if (promise != null) {
                Object returnValue = msg.getReturnValue();
                Exception exceptionValue = msg.getExceptionValue();
                if (exceptionValue != null) {
                    promise.setFailure(exceptionValue);
                } else {
                    promise.setSuccess(returnValue);
                }
            } else {
                promise.setFailure(new Exception("promise不存在"));
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


        @Override
        public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.debug("出现异常" + cause);
            if (ctx.channel().isActive()){
                ctx.close();
            }
        }
    }
