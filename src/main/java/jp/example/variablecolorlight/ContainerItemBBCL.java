package jp.example.variablecolorlight;
import net.minecraft.entity.player.EntityPlayer;import net.minecraft.inventory.Container;
public final class ContainerItemBBCL extends Container{public boolean canInteractWith(EntityPlayer p){return VariableColorLight.isEditableLightItem(p.getHeldItem());}}
