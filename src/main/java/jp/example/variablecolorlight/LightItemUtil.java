package jp.example.variablecolorlight;
import net.minecraft.item.ItemStack;
public final class LightItemUtil{
    private LightItemUtil(){}
    public static ItemStack copySettings(ItemStack stack,ColoredLightData source){ItemStackLightData out=new ItemStackLightData(stack);out.setKelvin(source.getKelvin());out.setRgb(source.getRed(),source.getGreen(),source.getBlue());out.setRgbMode(source.isRgbMode());out.setLevel(source.getLevel());out.setRedstoneControlled(source.isRedstoneControlled());return stack;}
}
