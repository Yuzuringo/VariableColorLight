package jp.example.variablecolorlight;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;

@Mod(modid = VariableColorLight.MODID, name = "Variable Color Light", version = "0.1.0", guiFactory = "jp.example.variablecolorlight.VCLGuiFactory")
public final class VariableColorLight {
    public static final String MODID = "variablecolorlight";
    public static Block lamp;
    public static Item entityLightItem;
    public static boolean isEditableLightItem(ItemStack stack){return stack!=null&&(stack.getItem()==entityLightItem||stack.getItem()==Item.getItemFromBlock(lamp));}
    @Mod.Instance(MODID) public static VariableColorLight instance;
    @SidedProxy(clientSide="jp.example.variablecolorlight.ClientProxy", serverSide="jp.example.variablecolorlight.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        lamp = new BlockVariableColorLamp().setBlockName("bbcl")
                .setBlockTextureName("glass").setCreativeTab(CreativeTabs.tabDecorations);
        GameRegistry.registerBlock(lamp,ItemBlockBBCL.class,"bbcl");
        GameRegistry.registerTileEntity(TileVariableColorLamp.class, MODID + ":bbcl");
        EntityRegistry.registerModEntity(EntityVariableColorLight.class,"bbcl_entity",1,this,64,10,true);
        entityLightItem=new ItemEntityLight().setUnlocalizedName("bbclEntityLight").setTextureName("minecraft:blaze_powder").setCreativeTab(CreativeTabs.tabDecorations);
        GameRegistry.registerItem(entityLightItem,"bbcl_entity_light");
        NetworkHandler.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        cpw.mods.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandlerBBCL());
        proxy.init();
    }
    @Mod.EventHandler public void serverStarting(FMLServerStartingEvent event) { event.registerServerCommand(new CommandVcl()); }
}
