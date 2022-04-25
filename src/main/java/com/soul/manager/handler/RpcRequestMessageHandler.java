package com.soul.manager.handler;

import com.soul.exception.ExceptionType;
import com.soul.exception.RpcException;
import com.soul.message.RequestMessage;
import com.soul.message.ResponseMessage;
import com.soul.services.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Rpc请求处理器
 *
 * @author chenlei
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RequestMessage> {

    /**
     * 读事件
     *
     * @param ctx
     * @param msg
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        ResponseMessage response = new ResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        Object result;
        try {
            Object service = ServicesFactory.getService(msg.getInterfaceName());
            Method method = service.getClass().getMethod(msg.getMethodName(),msg.getParameterTypes());
            result = method.invoke(service, msg.getParameterValue());
            response.setReturnValue(result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            response.setExceptionValue(new RpcException(ExceptionType.SERVICE_INVOCATION_FAILURE));
        } finally {
            ctx.writeAndFlush(response);
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("出现异常 : " + cause);
        if (ctx.channel().isActive()){
            ctx.close();
        }
    }
}

