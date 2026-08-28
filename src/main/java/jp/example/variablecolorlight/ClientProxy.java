package jp.example.variablecolorlight;

import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy {
    @Override public void observe(TileVariableColorLamp lamp){ShaderLightRegistry.observe(lamp);}
    @Override public void observe(EntityVariableColorLight light){ShaderLightRegistry.observe(light);}
    @Override public void remove(TileVariableColorLamp lamp){ShaderLightRegistry.remove(lamp);}
    @Override public void remove(EntityVariableColorLight light){ShaderLightRegistry.remove(light);}
    @Override public void lightChanged(){ShaderLightRegistry.rebuild();}
    @Override public void init() {
        ShaderLightRegistry.bootstrapClient();
        ClientRegistry.bindTileEntitySpecialRenderer(TileVariableColorLamp.class, new RenderVariableColorLamp());
        RenderingRegistry.registerEntityRenderingHandler(EntityVariableColorLight.class,new RenderEntityLight());
        MinecraftForge.EVENT_BUS.register(new ClientLightEvents());
        FMLEventChannel channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("VCL|SYNC");
        channel.register(new SpigotSyncHandler());
    }
}
