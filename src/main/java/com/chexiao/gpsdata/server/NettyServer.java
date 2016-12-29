package com.chexiao.gpsdata.server;

import com.bj58.daojia.app.engine.context.AppContext;
import com.bj58.daojia.app.engine.task.AppTask;
import com.chexiao.base.util.PropertyUtil;
import com.chexiao.gpsdata.codec.MessageDecoder;
import com.chexiao.gpsdata.handler.PositionHandler;
import com.chexiao.gpsdata.server.heartbeat.HeartbeatHandlerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fulei on 2016-12-17.
 */
public class NettyServer implements AppTask {

//    private static final Logger logger = Logger.getLogger(NettyServer.class);
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyServer.class);

    private static final int BUFSIZE = 256; // Buffer size (bytes)
    private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;
    private static final int LENGTH_FIELD_LENGTH = 0;
    private static final int LENGTH_FIELD_OFFSET = 45;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int INITIAL_BYTES_TO_STRIP = 0;


    List<Integer> portList = new ArrayList<Integer>();
    public void destory() {

    }

    public void init(AppContext arg0) {
        //读取监听port配置
        String portConfig = PropertyUtil.getProperty("port", "32102");
//        String portConfig = "32102,32103";

        if(portConfig.indexOf(",") > 0) {
            String[] ports = portConfig.split(",");
            for (String onePort : ports) {
                portList.add(Integer.parseInt(onePort));
            }
        } else {
            portList.add(Integer.parseInt(portConfig));
        }

        logger.info("tcp监听端口配置文件==" + portList.toString());
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class).childHandler(new HeartbeatHandlerInitializer()) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder",
                                    new MessageDecoder(MAX_FRAME_LENGTH,
                                            LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,
                                            LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
//                            ch.pipeline().addLast("encoder", new ProtocolEncoder());
                            ch.pipeline().addLast(new PositionHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)


            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(portList.get(0)).sync(); // (7)

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();
            logger.info("tpc启动成功，监听端口==" + portList.toString());
        }catch(Exception e){
            logger.error("tcp启动成功失败.........",e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
