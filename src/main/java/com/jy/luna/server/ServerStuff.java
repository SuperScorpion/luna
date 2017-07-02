package com.jy.luna.server;

import com.jy.luna.commons.LunaUtils;
import com.jy.luna.commons.Stuff;
import com.jy.luna.protocol.RpcDecoder;
import com.jy.luna.protocol.RpcEncoder;
import com.jy.luna.protocol.RpcRequest;
import com.jy.luna.protocol.RpcResponse;
import com.jy.luna.xsd.LunaXsdHandler;
import com.jy.luna.zookeeper.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by neo on 2017/6/20.
 */
public class ServerStuff {

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> serviceBeanMap;

    private String serverAddress;//"localhost:3333"

    public ServerStuff (ServiceRegistry serviceRegistry, ApplicationContext applicationContext) {

        this.serviceRegistry = serviceRegistry;
        //获取并缓存 当前server的可用服务

        Map<String, Object> serviceImplBeanMap = applicationContext.getBeansWithAnnotation(LunaService.class);

        if (serviceImplBeanMap != null && !serviceImplBeanMap.isEmpty()) {
            serviceBeanMap = new HashMap<>();
            for (Object serviceBean : serviceImplBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(LunaService.class).value().getName();
                serviceBeanMap.put(interfaceName, serviceBean);
            }
        }

        if(LunaUtils.isBlank(LunaUtils.gainLocalHostIp()) || LunaUtils.isBlank(LunaXsdHandler.port)) throw new RuntimeException("Luna: local ip or port is a need");

        this.serverAddress = LunaUtils.gainLocalHostIp() + ":" + LunaXsdHandler.port;
    }


    public void connectServerProcessor() {
        Stuff.execuService.submit(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65537, 0, 4, 0, 0))
                                        .addLast(new RpcEncoder(RpcResponse.class))
                                        .addLast(new RpcDecoder(RpcRequest.class))
                                        .addLast(new ServerHandler(serviceBeanMap));
                            }
                        }
                ).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

                String[] array = serverAddress.split(":");
                String host = array[0];
                int port = Integer.parseInt(array[1]);

                ChannelFuture future = bootstrap.bind(host, port).sync();// Start the server.

                if (serviceRegistry != null) {//注册当前server的服务
                    serviceRegistry.register(serverAddress);
                }

                future.channel().closeFuture().sync();// Wait until the server socket is closed.
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
//        ServerStuff sstf = new ServerStuff();
        try {
//            sstf.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        InetSocketAddress isa = new InetSocketAddress("localhost", 3333);
        InetSocketAddress isa2 = new InetSocketAddress("localhost", 3333);

        Stuff sf = new Stuff();
        Stuff sf2 = new Stuff();

        List<Stuff> pc = new ArrayList<>();
        pc.add(sf);
        System.out.println(pc.contains(sf2));

        List<InetSocketAddress> p = new ArrayList<>();
        p.add(isa);

        System.out.println(isa == isa2);
        System.out.println(p.contains(isa2));

        System.out.println(isa.toString());

        System.out.println(isa.getHostName() + ":" + isa.getPort());

        System.out.println(isa.getHostString());
        System.out.println(LunaUtils.gainLocalHostIp());
//        Map fd = new HashMap();
//        fd.put("a", "1");
//
//        Set vb = new HashSet(fd.keySet());
//        System.out.println(vb.size());
//
//        fd.put("b", "2");
//        System.out.println(vb.size());

    }
}
