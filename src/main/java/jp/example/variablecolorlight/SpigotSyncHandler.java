package jp.example.variablecolorlight;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;

public final class SpigotSyncHandler {
    @SubscribeEvent public void receive(FMLNetworkEvent.ClientCustomPacketEvent event) {
        ByteBuf b=event.packet.payload();
        if(b.readableBytes()<3)return;int version=b.readUnsignedByte();if(version!=1&&version!=2)return;
        int count=Math.min(4096,b.readUnsignedShort());
        List<ShaderLightRegistry.ExternalLight> out=new ArrayList<ShaderLightRegistry.ExternalLight>(count);
        for(int i=0;i<count && b.readableBytes()>=(version==1?19:23);i++){
            int x=b.readInt(),y=b.readInt(),z=b.readInt();int kelvin=b.readUnsignedShort();boolean mode=false;int r=255,g=255,blue=255;
            if(version==2){mode=b.readUnsignedByte()!=0;r=b.readUnsignedByte();g=b.readUnsignedByte();blue=b.readUnsignedByte();}
            int level=b.readUnsignedByte();int dimension=b.readInt();out.add(new ShaderLightRegistry.ExternalLight(x,y,z,kelvin,level,dimension,mode,r,g,blue));
        }
        ShaderLightRegistry.replaceExternalLights(out);
    }
}
