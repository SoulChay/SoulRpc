package com.soul.manager.handler;

import com.soul.config.Config;
import com.soul.message.Message;
import com.soul.utils.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

@ChannelHandler.Sharable
/**
 * 自定义想要发送的协议
 * 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 */
public class MessageCodecHandler extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        out.writeBytes("soul".getBytes());// 4字节的魔数
        out.writeByte(1);// 1字节的版本
        out.writeByte(Config.getSerializerAlgorithm().ordinal()); //1字节的序列化方式
        out.writeByte(msg.getMessageType());   // 1字节的指令类型
        out.writeInt(msg.getSequenceId()); //4字节的版本号
        out.writeByte(0xff);  //无意义 对齐填充 保证内容长度前有12字节

        // 6. 获取内容的字节数组
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        // 7. 长度
        out.writeInt(bytes.length); //4字节的长度
        // 8. 写入内容
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerAlgorithm = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes,0,length);

        //找到反序列化算法
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerAlgorithm];
        //确定具体消息类型
        Class<?> messageClass = Message.getMessageClass(messageType);
        Object deserialize = algorithm.deserialize(messageClass,bytes);
        out.add(deserialize);
    }
}
