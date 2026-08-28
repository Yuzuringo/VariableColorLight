package jp.example.variablecolorlight;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.item.Item;

public final class GuiHandlerBBCL implements IGuiHandler {
    public static final int GUI_ID=1,GUI_ITEM=2;
    public Object getServerGuiElement(int id,EntityPlayer p,World w,int x,int y,int z){if(id==GUI_ITEM)return new ContainerItemBBCL();TileEntity te=w.getTileEntity(x,y,z);return id==GUI_ID&&te instanceof TileVariableColorLamp?new ContainerBBCL((TileVariableColorLamp)te):null;}
    public Object getClientGuiElement(int id,EntityPlayer p,World w,int x,int y,int z){if(id==GUI_ITEM&&p.getHeldItem()!=null)return new GuiBBCL(new ItemStackLightData(p.getHeldItem()),true,p.getHeldItem().getItem()==Item.getItemFromBlock(VariableColorLight.lamp));TileEntity te=w.getTileEntity(x,y,z);return id==GUI_ID&&te instanceof TileVariableColorLamp?new GuiBBCL((TileVariableColorLamp)te):null;}
}
