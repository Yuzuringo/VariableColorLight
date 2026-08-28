package jp.example.variablecolorlight;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

public final class MessageSetBBCL implements IMessage {
    private int x,y,z,kelvin,r,g,b,level;private boolean rgbMode,redstoneControlled;
    public MessageSetBBCL(){}
    public MessageSetBBCL(TileVariableColorLamp l,boolean mode,boolean redstone,int kelvin,int r,int g,int b,int level){x=l.xCoord;y=l.yCoord;z=l.zCoord;rgbMode=mode;redstoneControlled=redstone;this.kelvin=kelvin;this.r=r;this.g=g;this.b=b;this.level=level;}
    public void toBytes(ByteBuf out){out.writeInt(x);out.writeInt(y);out.writeInt(z);out.writeBoolean(rgbMode);out.writeBoolean(redstoneControlled);out.writeShort(kelvin);out.writeByte(r);out.writeByte(g);out.writeByte(b);out.writeByte(level);}
    public void fromBytes(ByteBuf in){x=in.readInt();y=in.readInt();z=in.readInt();rgbMode=in.readBoolean();redstoneControlled=in.readBoolean();kelvin=in.readUnsignedShort();r=in.readUnsignedByte();g=in.readUnsignedByte();b=in.readUnsignedByte();level=in.readUnsignedByte();}
    public static final class Handler implements IMessageHandler<MessageSetBBCL,IMessage>{
        public IMessage onMessage(MessageSetBBCL m,MessageContext ctx){EntityPlayerMP p=ctx.getServerHandler().playerEntity;if(p.getDistanceSq(m.x+.5,m.y+.5,m.z+.5)>64)return null;TileEntity raw=p.worldObj.getTileEntity(m.x,m.y,m.z);if(!(raw instanceof TileVariableColorLamp))return null;TileVariableColorLamp l=(TileVariableColorLamp)raw;l.setKelvin(m.kelvin);l.setRgb(m.r,m.g,m.b);l.setRgbMode(m.rgbMode);l.setRedstoneControlled(m.redstoneControlled);l.setLevel(m.level);p.worldObj.markBlockForUpdate(m.x,m.y,m.z);p.worldObj.updateLightByType(net.minecraft.world.EnumSkyBlock.Block,m.x,m.y,m.z);return null;}
    }
}
