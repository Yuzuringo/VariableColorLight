package jp.example.variablecolorlight.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

@IFMLLoadingPlugin.Name("VariableColorLightCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"jp.example.variablecolorlight.core"})
public final class VCLLoadingPlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass(){return new String[]{"jp.example.variablecolorlight.core.ShaderBindTransformer"};}
    public String getModContainerClass(){return null;} public String getSetupClass(){return null;}
    public void injectData(Map<String,Object> data){} public String getAccessTransformerClass(){return null;}
}
