package jp.example.variablecolorlight;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;
import java.lang.reflect.Method;

/** Vanilla-renderer fallback: colored emissive core. Vanilla block light itself remains monochrome. */
public final class RenderVariableColorLamp extends TileEntitySpecialRenderer {
    private static final boolean ANGELICA=classExists("net.coderbot.iris.Iris");
    private static final Method ANGELICA_SHADOW_METHOD=findShadowMethod();
    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partialTick) {
        if(!(raw instanceof TileVariableColorLamp))return;
        if(isAngelicaShadowPass())return;
        TileVariableColorLamp lamp=(TileVariableColorLamp)raw;
        Minecraft mc=Minecraft.getMinecraft();ItemStack held=mc.thePlayer==null?null:mc.thePlayer.getHeldItem();
        boolean holding=held!=null&&held.getItem()==Item.getItemFromBlock(VariableColorLight.lamp);
        if(!holding&&!(mc.currentScreen instanceof GuiBBCL))return;
        float power=lamp.getLevel()/15F;
        if(power<=0)return;
        float[] rgb=lamp.getColor();
        float oldX=OpenGlHelper.lastBrightnessX,oldY=OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_LIGHTING_BIT);
        GL11.glPushMatrix();GL11.glTranslated(x,y,z);
        // Some shader packs (notably SEUS Renewed) discard vertex colour in
        // gbuffers_basic.  A white atlas texture routes this through the
        // textured pass while keeping the configured RGB as the visible tint.
        bindTexture(TextureMap.locationBlocksTexture);
        GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,ANGELICA?GL11.GL_ONE_MINUS_SRC_ALPHA:GL11.GL_ONE);GL11.glDepthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240F,240F);
        IIcon white=Blocks.quartz_block.getIcon(1,0);
        drawCube(0.02,0.98,rgb[0],rgb[1],rgb[2],ANGELICA?0.16F+power*0.12F:0.22F+power*0.28F,white);
        drawCube(0.12,0.88,rgb[0],rgb[1],rgb[2],ANGELICA?0.25F+power*0.18F:0.38F+power*0.42F,white);
        GL11.glDepthMask(true);GL11.glPopMatrix();GL11.glPopAttrib();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,oldX,oldY);
    }
    private static boolean classExists(String name){try{Class.forName(name,false,RenderVariableColorLamp.class.getClassLoader());return true;}catch(Throwable ignored){return false;}}
    private static Method findShadowMethod(){try{return Class.forName("net.coderbot.iris.shadows.ShadowRenderingState",false,RenderVariableColorLamp.class.getClassLoader()).getMethod("areShadowsCurrentlyBeingRendered");}catch(Throwable ignored){return null;}}
    private static boolean isAngelicaShadowPass(){if(ANGELICA_SHADOW_METHOD==null)return false;try{return Boolean.TRUE.equals(ANGELICA_SHADOW_METHOD.invoke(null));}catch(Throwable ignored){return false;}}
    private static void drawCube(double a,double b,float r,float g,float bl,float alpha,IIcon icon){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setColorRGBA_F(r,g,bl,alpha);t.setBrightness(0xF000F0);
        double u0=icon.getMinU(),u1=icon.getMaxU(),v0=icon.getMinV(),v1=icon.getMaxV();
        face(t,a,a,a,b,a,b,b,a,b,a,a,b,u0,u1,v0,v1); face(t,a,b,a,a,b,b,b,b,b,b,b,a,u0,u1,v0,v1);
        face(t,a,a,a,a,b,a,b,b,a,b,a,a,u0,u1,v0,v1); face(t,b,a,a,b,a,b,b,b,b,b,b,a,u0,u1,v0,v1);
        face(t,a,a,a,b,a,a,b,b,a,a,b,a,u0,u1,v0,v1); face(t,a,a,b,a,b,b,b,b,b,b,a,b,u0,u1,v0,v1);t.draw();
    }
    private static void face(Tessellator t,double x1,double y1,double z1,double x2,double y2,double z2,double x3,double y3,double z3,double x4,double y4,double z4,double u0,double u1,double v0,double v1){t.addVertexWithUV(x1,y1,z1,u0,v1);t.addVertexWithUV(x2,y2,z2,u1,v1);t.addVertexWithUV(x3,y3,z3,u1,v0);t.addVertexWithUV(x4,y4,z4,u0,v0);}
}
