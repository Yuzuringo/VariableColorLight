package jp.example.variablecolorlight;
import net.minecraft.client.gui.GuiButton;import net.minecraft.client.gui.GuiScreen;import net.minecraft.util.StatCollector;
public final class GuiVCLConfig extends GuiScreen{
    private final GuiScreen parent;private GuiButton toggle;public GuiVCLConfig(GuiScreen p){parent=p;}
    public void initGui(){buttonList.clear();toggle=new GuiButton(0,width/2-100,height/2-25,200,20,text());buttonList.add(toggle);buttonList.add(new GuiButton(1,width/2-100,height/2+35,200,20,tr("gui.bbcl.done")));}
    private String text(){return tr("gui.bbcl.occlusion")+": "+tr(ClientSettings.blockOcclusion()?"gui.bbcl.on":"gui.bbcl.off");}
    protected void actionPerformed(GuiButton b){if(b.id==0){ClientSettings.setBlockOcclusion(!ClientSettings.blockOcclusion());toggle.displayString=text();}else if(b.id==1)mc.displayGuiScreen(parent);}
    public void drawScreen(int x,int y,float tick){drawDefaultBackground();drawCenteredString(fontRendererObj,tr("gui.bbcl.client_settings"),width/2,height/2-70,0xFFFFFF);drawCenteredString(fontRendererObj,tr("gui.bbcl.shader_reload"),width/2,height/2+5,0xAAAAAA);super.drawScreen(x,y,tick);}private static String tr(String k){return StatCollector.translateToLocal(k);}
}
