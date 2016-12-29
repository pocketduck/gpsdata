package com.chexiao.gpsdata.handler;

import com.chexiao.gpsdata.entity.Student;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Created by fulei on 2016-12-17.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(DiscardServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)

        ByteBuf in = (ByteBuf) msg;
        logger.info("收到数据~~~~~~");

        try {
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
// 当出现异常就关闭连接
        logger.info("出现异常s~~");
        cause.printStackTrace();
        ctx.close();
    }
}
