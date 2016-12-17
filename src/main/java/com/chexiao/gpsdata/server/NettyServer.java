package com.chexiao.gpsdata.server;

import com.bj58.daojia.app.engine.context.AppContext;
import com.bj58.daojia.app.engine.task.AppTask;
import com.chexiao.base.util.PropertyUtil;
import com.chexiao.gpsdata.entity.Student;
import com.chexiao.gpsdata.handler.DiscardServerHandler;
import com.chexiao.gpsdata.service.StudentSerivce;
import com.chexiao.gpsdata.tcp.protocol.EchoProtocol;
import com.chexiao.gpsdata.tcp.protocol.TCPEchoSelectorProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fulei on 2016-12-17.
 */
public class NettyServer implements AppTask {

    private static final Logger logger = Logger.getLogger(NettyServer.class);
    StudentSerivce studentSerivce= new StudentSerivce();

    private static final int BUFSIZE = 256; // Buffer size (bytes)
    private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
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

        logger.info("启动中，监听端口==" + portList.toString());
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(portList.get(0)).sync(); // (7)

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();
        }catch(Exception e){
            logger.error("服务异常.....",e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
