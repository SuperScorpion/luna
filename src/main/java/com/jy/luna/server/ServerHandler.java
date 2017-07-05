package com.jy.luna.server;

import com.jy.luna.protocol.RpcRequest;
import com.jy.luna.protocol.RpcResponse;
import com.jy.luna.stuff.common.LunaConfigure;
import com.jy.luna.stuff.exception.LunaException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 2017.06.24
 * RPC Handler（RPC request processor）
 * @author neo
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    private final Map<String, Object> serviceBeanMap;

    public ServerHandler(Map<String, Object> serviceBeanMap) {
        this.serviceBeanMap = serviceBeanMap;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final RpcRequest request) throws Exception {
        LunaConfigure.execuService.submit(() -> {
//                System.out.println("Recevie=======>>" + request.getRequestId());
                LOGGER.debug("Luna: Receive request " + request.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(request.getRequestId());
                try {
                    Object result = handle(request);
                    response.setResult(result);
                } catch (Throwable t) {
                    response.setError(t.toString());
                    LOGGER.error("Luna: RPC Server handle request error", t);
                }
            /*try {//TODO 模拟处理延迟很久了
                System.out.println("SendResponse sleep ===>>>");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            ctx.writeAndFlush(response).addListener((ChannelFuture channelFuture) -> {
//                    @Override
//                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        LOGGER.debug("Luna: Send response for request " + request.getRequestId());
//                    }
                });
        });
    }

    private Object handle(RpcRequest request) throws Throwable {

        if(serviceBeanMap== null || serviceBeanMap.isEmpty()) throw new LunaException("Luna: There is no LunaService available");

        String className = request.getClassName();
        Object serviceBean = serviceBeanMap.get(className);

        if(serviceBean == null) throw new LunaException("Luna: ServerHandler can not find the serviceBean " + className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterClassTypes();
        Object[] parameters = request.getParameters();

        // JDK reflect
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);

        // Cglib reflect
        /*FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Luna: server caught exception", cause);
        ctx.close();
    }
}
