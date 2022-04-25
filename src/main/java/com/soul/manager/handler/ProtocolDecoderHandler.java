package com.soul.manager.handler;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 处理半包问题的 定长处理器
 */
public class ProtocolDecoderHandler extends LengthFieldBasedFrameDecoder {

    /**
     * 解码：最大字节长度 为1024, 长度偏移量为12字节，长度大小占4字节
     */
    public ProtocolDecoderHandler() {
        this(1024, 12, 4, 0, 0);
    }

    public ProtocolDecoderHandler(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
