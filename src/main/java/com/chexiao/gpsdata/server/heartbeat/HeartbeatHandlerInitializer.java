package com.chexiao.gpsdata.server.heartbeat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import io.netty.channel.Channel;
import java.util.concurrent.TimeUnit;

/**
 * Created by fulei on 2016/12/20.
 */
public class HeartbeatHandlerInitializer extends ChannelInitializer<Channel> {

private static final int READ_IDEL_TIME_OUT = 4; // 读超时
private static final int WRITE_IDEL_TIME_OUT = 5;// 写超时
private static final int ALL_IDEL_TIME_OUT = 7; // 所有超时

@Override
protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(READ_IDEL_TIME_OUT,
        WRITE_IDEL_TIME_OUT, ALL_IDEL_TIME_OUT, TimeUnit.SECONDS));
        pipeline.addLast(new HeartBeatServerHandler());
        }
}
