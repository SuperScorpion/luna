package com.jy.luna.client;

import com.jy.luna.protocol.RpcRequest;
import com.jy.luna.protocol.RpcResponse;
import com.jy.luna.xsd.LunaXsdHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    private ConcurrentHashMap<String, RpcFuture> rpcFutureMap = new ConcurrentHashMap<>();

    private volatile Channel channel;

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {

        String requestId = response.getRequestId();
        RpcFuture rpcFuture = rpcFutureMap.get(requestId);

        if (rpcFuture != null) {
            rpcFutureMap.remove(requestId);
            rpcFuture.setResponse0(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Luna: client caught exception", cause);
        ctx.close();
    }


    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RpcFuture channelWrite0(RpcRequest request) throws ExecutionException, InterruptedException {
        //设置service请求超时时间由用户决定的
        Map<String, String> stMap = LunaXsdHandler.serviceTimeoutMap;
        String timeout = null;
        if(stMap != null && !stMap.isEmpty()) {
            timeout =   LunaXsdHandler.serviceTimeoutMap.get(request.getClassName());
        }

        RpcFuture rpcFuture = new RpcFuture(request, timeout);
        rpcFutureMap.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request);
        return rpcFuture;
    }

}
