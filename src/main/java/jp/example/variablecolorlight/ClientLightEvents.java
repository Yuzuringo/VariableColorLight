package jp.example.variablecolorlight;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.entity.EntityJoinWorldEvent;import net.minecraftforge.event.world.WorldEvent;
public final class ClientLightEvents{
    @SubscribeEvent public void joined(EntityJoinWorldEvent e){if(e.world.isRemote&&e.entity instanceof EntityVariableColorLight)ShaderLightRegistry.observe((EntityVariableColorLight)e.entity);}
    @SubscribeEvent public void unloaded(WorldEvent.Unload e){if(e.world.isRemote)ShaderLightRegistry.clear();}
}
