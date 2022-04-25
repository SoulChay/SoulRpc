package com.soul.manager;

import com.soul.balance.RoundRobinRule;
import com.soul.exception.ExceptionType;
import com.soul.exception.RpcException;
import com.soul.manager.handler.MessageCodecHandler;
import com.soul.manager.handler.ProtocolDecoderHandler;
import com.soul.manager.handler.RpcResponseMessageHandler;
import com.soul.message.RequestMessage;
import com.soul.registery.ServiceDiscoveryImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 客户端
 */
@Slf4j
public class RpcClientManager {

    /**
     * 存储 客户端的 channel
     */
    public static Map<String, Channel> channels;

    /**
     * key: 序列号
     * value: Promise(用来接收结果)
     */
    public static final Map<Integer, Promise<Object>> PROMISES;

    /**
     * 服务发现
     */
    private static ServiceDiscoveryImpl serviceDiscovery;

    public static NioEventLoopGroup group;
    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        initChannel();
        channels = new ConcurrentHashMap<>();
        PROMISES = new ConcurrentHashMap<>();
    }

    public RpcClientManager() {
        this.serviceDiscovery = new ServiceDiscoveryImpl(new RoundRobinRule());
    }

    static void sendRpcRequest(RequestMessage msg){
        InetSocketAddress service = serviceDiscovery.lookupServerAddress(msg.getInterfaceName());
        Channel channel = get(service);
        if (!channel.isActive() || !channel.isRegistered()) {
            group.shutdownGracefully();
            return;
        }
        channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("客户端发送消息成功");
            }
        });
    }

    private static Bootstrap initChannel() {
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);//日志handler
        MessageCodecHandler MESSAGE_CODEC = new MessageCodecHandler();//消息处理handler
        RpcResponseMessageHandler RESPONSE_HANDLER = new RpcResponseMessageHandler();//处理相应handler
        bootstrap.channel(NioSocketChannel.class)
                .group(group)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) //连接超时毫秒数
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(0, 1, 0, TimeUnit.MINUTES));
                        ch.pipeline().addLast(new ProtocolDecoderHandler());
                        ch.pipeline().addLast(MESSAGE_CODEC);
                        ch.pipeline().addLast(LOGGING_HANDLER);
                        ch.pipeline().addLast(RESPONSE_HANDLER);
                    }
                });
        return bootstrap;
    }

    /**
     * 获取channel，没有则建立连接
     * @param inetSocketAddress
     * @return
     */
    public static Channel get(InetSocketAddress inetSocketAddress) {
        String addressStr = inetSocketAddress.toString();
        // 判断 channel 是否已存在
        if (channels.containsKey(addressStr)) {
            Channel channel = channels.get(addressStr);
            if (channels != null && channel.isActive()) {
                return channel;
            }
            channels.remove(addressStr);
        }

        // 建立连接
        Channel channel = null;
        try {
            channel = bootstrap.connect(inetSocketAddress).sync().channel();
            channel.closeFuture().addListener(future -> log.debug("连接断开"));
            channels.put(addressStr, channel);
        } catch (InterruptedException e) {
            channel.close();
            throw new RpcException(ExceptionType.CLIENT_CONNECT_SERVER_FAILURE , e.getMessage());
        }
        return channel;
    }

}
