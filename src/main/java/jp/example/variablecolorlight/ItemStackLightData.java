package jp.example.variablecolorlight;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
public final class ItemStackLightData implements ColoredLightData{
    private final ItemStack stack;public ItemStackLightData(ItemStack s){stack=s;if(!s.hasTagCompound())s.setTagCompound(new NBTTagCompound());}
    private NBTTagCompound n(){return stack.getTagCompound();} public int getKelvin(){return n().hasKey("Kelvin")?n().getInteger("Kelvin"):3000;}public int getLevel(){return n().hasKey("Level")?n().getByte("Level")&255:15;}public boolean isRgbMode(){return n().getBoolean("RgbMode");}public int getRed(){return n().hasKey("Red")?n().getByte("Red")&255:255;}public int getGreen(){return n().hasKey("Green")?n().getByte("Green")&255:180;}public int getBlue(){return n().hasKey("Blue")?n().getByte("Blue")&255:107;}
    public void setKelvin(int v){n().setInteger("Kelvin",Math.max(1000,Math.min(20000,v)));}public void setLevel(int v){n().setByte("Level",(byte)Math.max(0,Math.min(15,v)));}public void setRgbMode(boolean v){n().setBoolean("RgbMode",v);}public void setRgb(int r,int g,int b){n().setByte("Red",(byte)clamp(r));n().setByte("Green",(byte)clamp(g));n().setByte("Blue",(byte)clamp(b));}public float[]getColor(){return isRgbMode()?new float[]{getRed()/255F,getGreen()/255F,getBlue()/255F}:ColorUtil.kelvinToRgb(getKelvin());}private static int clamp(int v){return Math.max(0,Math.min(255,v));}
    public boolean isRedstoneControlled(){return n().getBoolean("RedstoneControlled");}public void setRedstoneControlled(boolean v){n().setBoolean("RedstoneControlled",v);}
}
