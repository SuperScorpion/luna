package com.jy.luna.protocol;

import com.jy.luna.xsd.LunaXsdHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = LunaXsdHandler.serialization.equals("kryo") ? KryoSerializeUtil.readObjectFromByteArray(data, genericClass) : ProtoStuffSerializeUtil.deserialize(data, genericClass);
//        Object obj = ProtoStuffSerializeUtil.deserialize(data, genericClass);

        out.add(obj);
    }
}
