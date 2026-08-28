package jp.example.variablecolorlight;
import java.io.*;import java.util.Properties;import net.minecraft.client.Minecraft;
public final class ClientSettings{
    private static boolean loaded,occlusion=true;private ClientSettings(){}
    public static boolean blockOcclusion(){if(!loaded)load();return occlusion;}
    public static void setBlockOcclusion(boolean value){occlusion=value;loaded=true;save();}
    private static File file(){return new File(new File(Minecraft.getMinecraft().mcDataDir,"config"),"variablecolorlight-client.properties");}
    private static void load(){loaded=true;Properties p=new Properties();File f=file();if(f.isFile())try{FileInputStream in=new FileInputStream(f);p.load(in);in.close();occlusion=Boolean.parseBoolean(p.getProperty("blockOcclusion","true"));}catch(IOException ignored){}}
    private static void save(){Properties p=new Properties();p.setProperty("blockOcclusion",Boolean.toString(occlusion));File f=file();File parent=f.getParentFile();if(!parent.isDirectory())parent.mkdirs();try{FileOutputStream out=new FileOutputStream(f);p.store(out,"BBCL client-only settings");out.close();}catch(IOException ignored){}}
}
