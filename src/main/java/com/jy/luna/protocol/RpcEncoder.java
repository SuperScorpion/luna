package com.jy.luna.protocol;

import com.jy.luna.xsd.LunaXsdHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = LunaXsdHandler.serialization.equals("kryo") ? KryoSerializeUtil.writeObjectToByteArray(in) : ProtoStuffSerializeUtil.serialize(in);
//            byte[] data = ProtoStuffSerializeUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
