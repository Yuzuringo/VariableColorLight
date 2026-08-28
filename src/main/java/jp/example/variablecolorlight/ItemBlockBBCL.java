package jp.example.variablecolorlight;
import net.minecraft.block.Block;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.ItemBlock;import net.minecraft.item.ItemStack;import net.minecraft.tileentity.TileEntity;import net.minecraft.world.World;
public final class ItemBlockBBCL extends ItemBlock{
    public ItemBlockBBCL(Block block){super(block);}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){if(!world.isRemote)player.openGui(VariableColorLight.instance,GuiHandlerBBCL.GUI_ITEM,world,0,0,0);return stack;}
    @Override public boolean placeBlockAt(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ,int metadata){if(!super.placeBlockAt(stack,player,world,x,y,z,side,hitX,hitY,hitZ,metadata))return false;TileEntity raw=world.getTileEntity(x,y,z);if(raw instanceof TileVariableColorLamp&&stack.hasTagCompound()){ItemStackLightData from=new ItemStackLightData(stack);TileVariableColorLamp to=(TileVariableColorLamp)raw;to.setKelvin(from.getKelvin());to.setRgb(from.getRed(),from.getGreen(),from.getBlue());to.setRgbMode(from.isRgbMode());to.setLevel(from.getLevel());to.setRedstoneControlled(from.isRedstoneControlled());world.markBlockForUpdate(x,y,z);world.updateLightByType(net.minecraft.world.EnumSkyBlock.Block,x,y,z);}return true;}
}
