package jp.example.variablecolorlight;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public final class ContainerBBCL extends Container {
    private final TileVariableColorLamp lamp;
    public ContainerBBCL(TileVariableColorLamp lamp){this.lamp=lamp;}
    @Override public boolean canInteractWith(EntityPlayer player){return lamp!=null&&!lamp.isInvalid()&&player.getDistanceSq(lamp.xCoord+.5,lamp.yCoord+.5,lamp.zCoord+.5)<=64;}
}
