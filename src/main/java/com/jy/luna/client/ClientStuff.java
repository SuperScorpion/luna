package com.jy.luna.client;

import com.jy.luna.client.proxy.ProxyFactory;
import com.jy.luna.protocol.RpcDecoder;
import com.jy.luna.protocol.RpcEncoder;
import com.jy.luna.protocol.RpcRequest;
import com.jy.luna.protocol.RpcResponse;
import com.jy.luna.stuff.LunaConfigure;
import com.jy.luna.stuff.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.LunaXsdHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by neo on 2017/6/20.
 */
public class ClientStuff {


    public ClientStuff initProxy2Spring(ApplicationContext applicationContext) {
        List<String> serviceList = LunaXsdHandler.servicePathList;
        if(serviceList != null && !serviceList.isEmpty()) {
            for(String se : serviceList) {

                Class<?> clz = null;
                Object pox = null;

                try {
                    clz = Class.forName(se);
                    pox =  ProxyFactory.createProxy(clz);
                }catch (Exception e) {
                    e.printStackTrace();
                }

                if(pox == null) throw new LunaException("Luna: there was an error in the init proxy : " + se);

                DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
                beanFactory.registerSingleton(LunaUtils.lowcaseFirst(clz.getSimpleName()), pox);
            }
        }
        return this;
    }


    EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    public void connectServerProcessor(InetSocketAddress remotePeer) throws Exception {
        LunaConfigure.execuService.submit(() -> {
            Bootstrap b = new Bootstrap();
            try {
                b.group(eventLoopGroup).channel(NioSocketChannel.class).handler(
                        new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel socketChannel) throws Exception {
                                ChannelPipeline cp = socketChannel.pipeline();
                                cp.addLast(new LengthFieldBasedFrameDecoder(65537, 0, 4, 0, 0));
                                cp.addLast(new RpcEncoder(RpcRequest.class));
                                cp.addLast(new RpcDecoder(RpcResponse.class));
                                cp.addLast(new ClientHandler());
                            }
                        }
                );



                // Start the client.
                ChannelFuture f = b.connect(remotePeer).addListener(
                        new ChannelFutureListener() {
                            @Override
                            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                                if (channelFuture.isSuccess()) {

                                    ClientHandler handler = channelFuture.channel().pipeline().get(ClientHandler.class);
                                    ClientHandlerManager.getInstance().addHandler(handler);

                                    /*RpcRequest rrqt = new RpcRequest();
                                    rrqt.setRequestId("123456789");
                                    RpcFuture rpcFuture = handler.channelWrite0(rrqt);*/

//                                    Configure.execuService.submit(new Runnana(rpcFuture));//for test
                                }
                            }
                        }
                );

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
//                System.out.println("shutdown client gracef");
                eventLoopGroup.shutdownGracefully();
            }
        });
    }

    /*public class Runnana implements Runnable {

        private RpcFuture rpcFuture;
        public Runnana(RpcFuture p) {
            this.rpcFuture = p;
        }
        @Override
        public void run() {
            try {

                System.out.println("333=========>>" + rpcFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }*/
    /*public static void main(String[] args) {

        ClientStuff cstf = new ClientStuff();
        try {
            cstf.connectServerProcessor(new InetSocketAddress("localhost", 3333));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
