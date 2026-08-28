package jp.example.variablecolorlight;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.util.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

/** Persistent, movable, invisible coloured-light anchor. */
public final class EntityVariableColorLight extends Entity implements ColoredLightData {
    private static final int KELVIN=20,LEVEL=21,MODE=22,RED=23,GREEN=24,BLUE=25;
    public EntityVariableColorLight(World world){super(world);setSize(0.3F,0.3F);noClip=true;}
    @Override protected void entityInit(){dataWatcher.addObject(KELVIN,Integer.valueOf(3000));dataWatcher.addObject(LEVEL,Byte.valueOf((byte)15));dataWatcher.addObject(MODE,Byte.valueOf((byte)0));dataWatcher.addObject(RED,Byte.valueOf((byte)255));dataWatcher.addObject(GREEN,Byte.valueOf((byte)180));dataWatcher.addObject(BLUE,Byte.valueOf((byte)107));}
    @Override public void onUpdate(){super.onUpdate();motionX=motionY=motionZ=0;noClip=true;}
    @Override public void setDead(){if(worldObj!=null&&worldObj.isRemote)VariableColorLight.proxy.remove(this);super.setDead();}
    @Override public void func_145781_i(int id){if(worldObj!=null&&worldObj.isRemote)VariableColorLight.proxy.lightChanged();}
    @Override public boolean canBeCollidedWith(){return true;}
    @Override public boolean canBePushed(){return false;}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){if(!worldObj.isRemote)setDead();return true;}
    @Override public ItemStack getPickedResult(MovingObjectPosition target){return LightItemUtil.copySettings(new ItemStack(VariableColorLight.entityLightItem),this);}
    @Override protected void readEntityFromNBT(NBTTagCompound n){setKelvin(n.hasKey("Kelvin")?n.getInteger("Kelvin"):3000);setLevel(n.hasKey("Level")?n.getByte("Level"):15);setRgb(n.hasKey("Red")?n.getByte("Red")&255:255,n.hasKey("Green")?n.getByte("Green")&255:180,n.hasKey("Blue")?n.getByte("Blue")&255:107);setRgbMode(n.getBoolean("RgbMode"));}
    @Override protected void writeEntityToNBT(NBTTagCompound n){n.setInteger("Kelvin",getKelvin());n.setByte("Level",(byte)getLevel());n.setBoolean("RgbMode",isRgbMode());n.setByte("Red",(byte)getRed());n.setByte("Green",(byte)getGreen());n.setByte("Blue",(byte)getBlue());}
    public int getKelvin(){return dataWatcher.getWatchableObjectInt(KELVIN);} public int getLevel(){return dataWatcher.getWatchableObjectByte(LEVEL)&255;} public boolean isRgbMode(){return dataWatcher.getWatchableObjectByte(MODE)!=0;}
    public int getRed(){return dataWatcher.getWatchableObjectByte(RED)&255;} public int getGreen(){return dataWatcher.getWatchableObjectByte(GREEN)&255;} public int getBlue(){return dataWatcher.getWatchableObjectByte(BLUE)&255;}
    public void setKelvin(int v){dataWatcher.updateObject(KELVIN,Integer.valueOf(Math.max(1000,Math.min(20000,v))));}
    public void setLevel(int v){dataWatcher.updateObject(LEVEL,Byte.valueOf((byte)Math.max(0,Math.min(15,v))));}
    public void setRgbMode(boolean v){dataWatcher.updateObject(MODE,Byte.valueOf((byte)(v?1:0)));}
    public void setRgb(int r,int g,int b){dataWatcher.updateObject(RED,Byte.valueOf((byte)clamp(r)));dataWatcher.updateObject(GREEN,Byte.valueOf((byte)clamp(g)));dataWatcher.updateObject(BLUE,Byte.valueOf((byte)clamp(b)));setRgbMode(true);}
    public float[] getColor(){return isRgbMode()?new float[]{getRed()/255F,getGreen()/255F,getBlue()/255F}:ColorUtil.kelvinToRgb(getKelvin());}
    public boolean isRedstoneControlled(){return false;}public void setRedstoneControlled(boolean value){}
    private static int clamp(int v){return Math.max(0,Math.min(255,v));}
}
