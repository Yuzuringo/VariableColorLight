package jp.example.variablecolorlight;
import cpw.mods.fml.client.IModGuiFactory;import java.util.Set;import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiScreen;
public final class VCLGuiFactory implements IModGuiFactory{
    public void initialize(Minecraft mc){}public Class<? extends GuiScreen> mainConfigGuiClass(){return GuiVCLConfig.class;}public Set<RuntimeOptionCategoryElement> runtimeGuiCategories(){return null;}public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement e){return null;}
}
