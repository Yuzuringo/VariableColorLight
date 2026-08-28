package jp.example.variablecolorlight;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL=NetworkRegistry.INSTANCE.newSimpleChannel("VCLNET");
    private NetworkHandler(){}
    public static void init(){CHANNEL.registerMessage(MessageSetBBCL.Handler.class,MessageSetBBCL.class,0,Side.SERVER);CHANNEL.registerMessage(MessageSetItemBBCL.Handler.class,MessageSetItemBBCL.class,1,Side.SERVER);}
}
