package jp.example.variablecolorlight;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Places a persistent BBCL entity light on the selected block face. */
public final class ItemEntityLight extends Item {
    public ItemEntityLight(){setMaxStackSize(64);}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){if(!world.isRemote)player.openGui(VariableColorLight.instance,GuiHandlerBBCL.GUI_ITEM,world,0,0,0);return stack;}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ){
        ForgeDirection face=ForgeDirection.getOrientation(side);
        double px=x+0.5+face.offsetX*0.501,py=y+0.5+face.offsetY*0.501,pz=z+0.5+face.offsetZ*0.501;
        if(!world.isRemote){
            EntityVariableColorLight light=new EntityVariableColorLight(world);
            light.setPosition(px,py,pz);
            if(stack.hasTagCompound()){
                if(stack.getTagCompound().hasKey("Kelvin"))light.setKelvin(stack.getTagCompound().getInteger("Kelvin"));
                if(stack.getTagCompound().hasKey("Level"))light.setLevel(stack.getTagCompound().getByte("Level"));
                if(stack.getTagCompound().hasKey("Red"))light.setRgb(stack.getTagCompound().getByte("Red")&255,stack.getTagCompound().getByte("Green")&255,stack.getTagCompound().getByte("Blue")&255);
                light.setRgbMode(stack.getTagCompound().getBoolean("RgbMode"));
            }
            world.spawnEntityInWorld(light);
            if(!player.capabilities.isCreativeMode)--stack.stackSize;
        }
        return true;
    }
}
