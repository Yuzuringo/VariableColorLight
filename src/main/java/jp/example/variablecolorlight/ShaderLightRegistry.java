package jp.example.variablecolorlight;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import cpw.mods.fml.common.FMLLog;

public final class ShaderLightRegistry {
    private static final int MAX = 256;
    private static final FloatBuffer POS_RADIUS = BufferUtils.createFloatBuffer(MAX * 4);
    private static final FloatBuffer COLOR_POWER = BufferUtils.createFloatBuffer(MAX * 4);
    private static volatile Snapshot snapshot = new Snapshot(new float[0], new float[0], 0);
    private static boolean loggedSnapshot,loggedUniform,loggedMissingPosition,loggedMissingColor;
    private static volatile List<ExternalLight> externalLights = Collections.emptyList();
    private static final List<TileVariableColorLamp> observedLights=new ArrayList<TileVariableColorLamp>();
    private static final List<EntityVariableColorLight> observedEntityLights=new ArrayList<EntityVariableColorLight>();
    public static void observe(TileVariableColorLamp lamp){if(!observedLights.contains(lamp)){observedLights.add(lamp);rebuild();}}
    public static void observe(EntityVariableColorLight light){if(!observedEntityLights.contains(light)){observedEntityLights.add(light);rebuild();}}
    public static void remove(TileVariableColorLamp lamp){if(observedLights.remove(lamp))rebuild();}
    public static void remove(EntityVariableColorLight light){if(observedEntityLights.remove(light))rebuild();}
    public static void clear(){observedLights.clear();observedEntityLights.clear();snapshot=new Snapshot(new float[0],new float[0],0);}

    public static void bootstrapClient() {}
    public static void replaceExternalLights(List<ExternalLight> lights) {
        externalLights = Collections.unmodifiableList(new ArrayList<ExternalLight>(lights));
        rebuild();
    }

    public static void rebuild() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) { snapshot = new Snapshot(new float[0], new float[0], 0); return; }
        final double cx=mc.renderViewEntity==null?0:mc.renderViewEntity.posX,cy=mc.renderViewEntity==null?0:mc.renderViewEntity.posY,cz=mc.renderViewEntity==null?0:mc.renderViewEntity.posZ;
        List<LightValue> all=new ArrayList<LightValue>();
        for(TileVariableColorLamp l:observedLights)if(l.getWorldObj()==mc.theWorld&&l.getEffectiveLevel()>0)all.add(new LightValue(l.xCoord,l.yCoord,l.zCoord,l.getColor(),l.getEffectiveLevel()));
        for(EntityVariableColorLight l:observedEntityLights)if(l.worldObj==mc.theWorld&&!l.isDead&&l.getLevel()>0)all.add(new LightValue(l.posX-.5,l.posY-.5,l.posZ-.5,l.getColor(),l.getLevel()));
        int dim=mc.theWorld.provider.dimensionId;
        for(ExternalLight l:externalLights)if(l.dimension==dim)all.add(new LightValue(l.x,l.y,l.z,l.color(),l.level));
        Collections.sort(all,new Comparator<LightValue>(){public int compare(LightValue a,LightValue b){return Double.compare(a.distanceSq(cx,cy,cz),b.distanceSq(cx,cy,cz));}});
        int count = Math.min(MAX, all.size()); float[] p = new float[count * 4], c = new float[count * 4];
        for (int i=0;i<count;i++) { LightValue l=all.get(i); float[] rgb=l.rgb;
            p[i*4]=(float)(l.x+.5); p[i*4+1]=(float)(l.y+.5); p[i*4+2]=(float)(l.z+.5); p[i*4+3]=Math.max(2F,l.level*1.15F);
            // GUI/NBT colours are sRGB, while shader-pack lighting is evaluated
            // in linear RGB. Sending sRGB directly lifts the secondary channels
            // and makes saturated colours look pale or hue-shifted.
            c[i*4]=srgbToLinear(rgb[0]); c[i*4+1]=srgbToLinear(rgb[1]); c[i*4+2]=srgbToLinear(rgb[2]); c[i*4+3]=l.level/15F; }
        snapshot=new Snapshot(p,c,count);
        if(count>0&&!loggedSnapshot){loggedSnapshot=true;FMLLog.info("[BBCL] Client light registry contains %d light(s).",count);}
    }
    /** Called by the coremod immediately after a shader program is bound. */
    public static void onProgramBound(int program) {
        if (program <= 0) return; Snapshot s=snapshot;
        int n=GL20.glGetUniformLocation(program,"vclLightCount"); if(n<0)return;
        if(!loggedUniform){loggedUniform=true;FMLLog.info("[BBCL] Shader program %d accepts BBCL uniforms; uploading %d light(s).",program,s.count);}
        GL20.glUniform1i(n,s.count);
        int p=GL20.glGetUniformLocation(program,"vclLightPosRadius[0]"); if(p>=0){Minecraft mc=Minecraft.getMinecraft();double cx=mc.renderViewEntity==null?0:mc.renderViewEntity.posX,cy=mc.renderViewEntity==null?0:mc.renderViewEntity.posY,cz=mc.renderViewEntity==null?0:mc.renderViewEntity.posZ;POS_RADIUS.clear();for(int i=0;i<s.count;i++){POS_RADIUS.put(s.pos[i*4]-(float)cx);POS_RADIUS.put(s.pos[i*4+1]-(float)cy);POS_RADIUS.put(s.pos[i*4+2]-(float)cz);POS_RADIUS.put(s.pos[i*4+3]);}POS_RADIUS.flip();GL20.glUniform4(p,POS_RADIUS);}else if(!loggedMissingPosition){loggedMissingPosition=true;FMLLog.warning("[BBCL] vclLightPosRadius[0] was optimized out or unavailable.");}
        int c=GL20.glGetUniformLocation(program,"vclLightColorPower[0]"); if(c>=0){COLOR_POWER.clear();COLOR_POWER.put(s.color).flip();GL20.glUniform4(c,COLOR_POWER);}else if(!loggedMissingColor){loggedMissingColor=true;FMLLog.warning("[BBCL] vclLightColorPower[0] was optimized out or unavailable.");}
    }
    public static void onProgramBoundARB(int program){onProgramBound(program);}

    public static float[] kelvinToRgb(int kelvin) {
        return ColorUtil.kelvinToRgb(kelvin);
    }
    private static float srgbToLinear(float value){
        value=Math.max(0F,Math.min(1F,value));
        return value<=0.04045F?value/12.92F:(float)Math.pow((value+0.055F)/1.055F,2.4D);
    }
    private static final class Snapshot { final float[] pos,color; final int count; Snapshot(float[]p,float[]c,int n){pos=p;color=c;count=n;} }
    private static final class LightValue {final double x,y,z;final int level;final float[]rgb;LightValue(double x,double y,double z,float[]c,int l){this.x=x;this.y=y;this.z=z;rgb=c;level=l;}double distanceSq(double a,double b,double c){double dx=x+.5-a,dy=y+.5-b,dz=z+.5-c;return dx*dx+dy*dy+dz*dz;}}
    public static final class ExternalLight {public final int x,y,z,kelvin,level,dimension,r,g,b;public final boolean rgbMode;public ExternalLight(int x,int y,int z,int k,int l,int d,boolean mode,int r,int g,int b){this.x=x;this.y=y;this.z=z;kelvin=k;level=l;dimension=d;rgbMode=mode;this.r=r;this.g=g;this.b=b;}float[]color(){return rgbMode?new float[]{r/255F,g/255F,b/255F}:kelvinToRgb(kelvin);}}
}
