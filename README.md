# RpcSoul
## 写在前面

​	本项目参考于 何人听我楚狂声 与 黑马程序员 的 rpc 项目代码，用于本人学习 Netty + Rpc 。

​	目的是通过这个项目 巩固自己对 netty 网络编程的理解。

## 项目介绍

- 基于 Netty传输的网络传输方式
- 实现自定义的通信协议
- 使用 Nacos 作为注册中心，管理服务提供者信息
- 实现了两种负载均衡算法：随机算法与轮转算法
- 采用读取 properties 文件来设置服务端口号 和 序列化算法 
- 服务端实现了自动注册服务
- 采用了 Netty 的心跳机制
- 实现了三种序列化算法，Jdk原生方式、Json 方式、Kryo 方式（默认采用 Json方式序列化）

## 执行流程

​	图片来自Guide哥

![输入图片说明](https://images.gitee.com/uploads/images/2021/0516/130825_6cab1cf2_8044183.png "屏幕截图.png")



## 自定义传输协议

调用参数与返回值的传输采用了如下 协议以避免粘包半包问题：

1. 前4个字节为 自定义的 魔数（比如我的魔数 ：soul ）
2. 第5个字节为 版本
3. 第6个字节为 序列化方式
4. 第七个字节为 指令类型
5. 第 8~11字节为 序列号
6. 第12字节占位使用 (使用了 0xff 来占位)
7. 第13 ~ 16字节为 内容长度

```JAVA
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 73 6f 75 6c 01 01 01 00 00 00 01 ff 00 00 00 ca |soul............|
|        | …………  …………  …………  …………  …………  …………  …………  …………  |................|
|........|                                                 |................|
+--------+-------------------------------------------------+----------------+

```
## 启动

1.nacos

2.直接运行 RpcServer + RpcClient

