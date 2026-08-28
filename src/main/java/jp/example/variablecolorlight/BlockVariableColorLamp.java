package jp.example.variablecolorlight;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;

public final class BlockVariableColorLamp extends BlockContainer {
    public BlockVariableColorLamp() { super(Material.glass); setHardness(0.4F); setStepSound(soundTypeGlass); }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileVariableColorLamp(); }
    @Override public int getRenderType(){return -1;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean canConnectRedstone(IBlockAccess world,int x,int y,int z,int side){return true;}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return null;}
    @Override public ItemStack getPickBlock(MovingObjectPosition target,World world,int x,int y,int z,EntityPlayer player){TileEntity te=world.getTileEntity(x,y,z);ItemStack stack=new ItemStack(Item.getItemFromBlock(this));return te instanceof TileVariableColorLamp?LightItemUtil.copySettings(stack,(TileVariableColorLamp)te):stack;}
    @Override public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        return te instanceof TileVariableColorLamp ? ((TileVariableColorLamp) te).getEffectiveLevel() : 15;
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor){
        TileEntity te=world.getTileEntity(x,y,z);
        if(te instanceof TileVariableColorLamp&&((TileVariableColorLamp)te).isRedstoneControlled()){
            world.updateLightByType(EnumSkyBlock.Block,x,y,z);
            world.markBlockForUpdate(x,y,z);
            if(world.isRemote)VariableColorLight.proxy.lightChanged();
        }
    }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                               int side, float hitX, float hitY, float hitZ) {
        if(!world.isRemote)player.openGui(VariableColorLight.instance,GuiHandlerBBCL.GUI_ID,world,x,y,z);
        return true;
    }
}
