package jp.example.variablecolorlight;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public final class TileVariableColorLamp extends TileEntity implements ColoredLightData {
    private int kelvin = 3000, level = 15, red = 255, green = 180, blue = 107;
    private boolean rgbMode,redstoneControlled;
    @Override public void validate(){super.validate();if(worldObj!=null&&worldObj.isRemote)VariableColorLight.proxy.observe(this);}
    @Override public void invalidate(){if(worldObj!=null&&worldObj.isRemote)VariableColorLight.proxy.remove(this);super.invalidate();}
    @Override public void onChunkUnload(){if(worldObj!=null&&worldObj.isRemote)VariableColorLight.proxy.remove(this);super.onChunkUnload();}
    public int getKelvin() { return kelvin; }
    public int getLevel() { return level; }
    public int getEffectiveLevel(){return redstoneControlled&&worldObj!=null&&!worldObj.isBlockIndirectlyGettingPowered(xCoord,yCoord,zCoord)?0:level;}
    public boolean isRgbMode() { return rgbMode; }
    public int getRed(){return red;} public int getGreen(){return green;} public int getBlue(){return blue;}
    public void setKelvin(int value) { kelvin = Math.max(1000, Math.min(20000, value)); markDirty(); }
    public void setLevel(int value) { level = Math.max(0, Math.min(15, value)); markDirty(); }
    public void setRgbMode(boolean value){rgbMode=value;markDirty();}
    public boolean isRedstoneControlled(){return redstoneControlled;}public void setRedstoneControlled(boolean value){redstoneControlled=value;markDirty();}
    public void setRgb(int r,int g,int b){red=clampByte(r);green=clampByte(g);blue=clampByte(b);rgbMode=true;markDirty();}
    public float[] getColor(){return rgbMode?new float[]{red/255F,green/255F,blue/255F}:ColorUtil.kelvinToRgb(kelvin);}
    private static int clampByte(int v){return Math.max(0,Math.min(255,v));}
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setInteger("Kelvin", kelvin); tag.setByte("Level", (byte) level);tag.setBoolean("RgbMode",rgbMode);tag.setBoolean("RedstoneControlled",redstoneControlled);tag.setByte("Red",(byte)red);tag.setByte("Green",(byte)green);tag.setByte("Blue",(byte)blue); }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); setKelvin(tag.hasKey("Kelvin")?tag.getInteger("Kelvin"):3000); setLevel(tag.hasKey("Level")?tag.getByte("Level"):15);rgbMode=tag.getBoolean("RgbMode");redstoneControlled=tag.getBoolean("RedstoneControlled");if(tag.hasKey("Red")){red=tag.getByte("Red")&255;green=tag.getByte("Green")&255;blue=tag.getByte("Blue")&255;} }
    @Override public Packet getDescriptionPacket() { NBTTagCompound tag = new NBTTagCompound(); writeToNBT(tag); return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag); }
    @Override public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g());VariableColorLight.proxy.observe(this);VariableColorLight.proxy.lightChanged(); }
}
