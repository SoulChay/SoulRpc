package com.soul.manager;

import com.soul.annotation.RpcService;
import com.soul.annotation.RpcServiceScan;
import com.soul.exception.ExceptionType;
import com.soul.exception.RpcException;
import com.soul.manager.handler.MessageCodecHandler;
import com.soul.manager.handler.ProtocolDecoderHandler;
import com.soul.manager.handler.RpcRequestMessageHandler;
import com.soul.registery.ServiceRegistry;
import com.soul.registery.ServiceRegistryImpl;
import com.soul.registery.ShutdownHook;
import com.soul.services.ServicesFactory;
import com.soul.utils.ServiceScanUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 服务端
 */
public class RpcServerManager{

    /**
     * 端口号  + ip地址
     */
    protected int port;
    protected String host;

    /**
     * service工厂
     */
    protected ServicesFactory servicesFactory;

    /**
     * 服务注册
     */
    protected ServiceRegistry serviceRegistry;

    public RpcServerManager(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = new ServiceRegistryImpl();
        this.servicesFactory = new ServicesFactory();
        scanServices();
    }

    /**
     * 扫描服务
     */
    public void scanServices() {

        /* 找到启动类 */
        String mainClassPath = ServiceScanUtils.getStackTrace();
        Class<?> mainClass;
        try {
            mainClass = Class.forName(mainClassPath);
        } catch (ClassNotFoundException e) {
            throw new RpcException(ExceptionType.START_CLASS_NOT_FOUND);
        }
        if (!mainClass.isAnnotationPresent(RpcServiceScan.class)) {
            throw new RpcException(ExceptionType.SERVICE_SCAN_PACKAGE_NOT_FOUND);
        }

        /* 拿到启动类上@RpcServiceScan注解的值 */
        String annotationValue = mainClass.getAnnotation(RpcServiceScan.class).value();

        /* 如果注解的值为空，则将annotationValue 赋值为启动类的父路径 */
        if ("".equals(annotationValue)) {
            annotationValue = mainClassPath.substring(0, mainClassPath.lastIndexOf("."));
        }

        /* 获取启动类类所在包内的所有类 */
        Set<Class<?>> set = ServiceScanUtils.getClasses(annotationValue);
        for (Class<?> c : set) {
            if (c.isAnnotationPresent(RpcService.class)) {
                String ServiceNameValue = c.getAnnotation(RpcService.class).name();
                Object object;
                try {
                    object = c.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    System.err.println("创建对象" + c + "发生错误");
                    continue;
                }
                /* 注解的值为空使用类名，否则使用注解的值 */
                if ("".equals(ServiceNameValue)) {
                    addService(object,c.getCanonicalName());
                } else {
                    addService(object, ServiceNameValue);
                }
            }
        }
    }

    /**
     * 添加对象到工厂和注册到注册中心
     * @param service
     * @param serviceName
     * @param <T>
     */
    public <T> void addService(T service, String serviceName) {
        servicesFactory.addService(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }


    /**
     * boss + worker 线程池
     */
    private NioEventLoopGroup boss = new NioEventLoopGroup();
    private NioEventLoopGroup worker = new NioEventLoopGroup();

    private ServerBootstrap bootstrap = new ServerBootstrap();

    /**
     * 开启服务
     */
    public void start() {
        ShutdownHook.getShutdownHook().addClearServicesHook(); // 添加用来注销所有服务的钩子函数
        LoggingHandler LOGGING = new LoggingHandler(LogLevel.DEBUG);//日志处理器
        MessageCodecHandler MESSAGE_CODEC_HANDLER = new MessageCodecHandler();//加码解码器
        RpcRequestMessageHandler REQUEST_HANDLER = new RpcRequestMessageHandler();//RPC请求处理器
        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 512)    //指定等待队列的大小
                    .option(ChannelOption.TCP_NODELAY, true)  //禁止使用Nagle算法,适用于小数据的即时传输
                    .option(ChannelOption.SO_KEEPALIVE, true) //两小时内没有收到数据，TCP会自动发送一个活动探测数据报文。
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(2, 0, 0, TimeUnit.MINUTES));
                            ch.pipeline().addLast(new ProtocolDecoderHandler());
                            ch.pipeline().addLast(LOGGING);
                            ch.pipeline().addLast(MESSAGE_CODEC_HANDLER);
                            ch.pipeline().addLast(REQUEST_HANDLER);
                        }
                    });
            //绑定端口
            Channel channel = bootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("启动服务出错");
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
