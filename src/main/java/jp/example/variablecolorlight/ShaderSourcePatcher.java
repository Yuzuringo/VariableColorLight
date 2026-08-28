package jp.example.variablecolorlight;

import cpw.mods.fml.common.FMLLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Runtime-only shader adapter. Original shader pack files are never modified. */
public final class ShaderSourcePatcher {
    private static boolean loggedComplementary,loggedSeus;
    private ShaderSourcePatcher(){}
    public static String patchAngelica(String source,String programName){
        if(source==null||programName==null||source.indexOf("vclLightCount")>=0)return source;
        String name=programName.toLowerCase();String patched=null;boolean occlusion=ClientSettings.blockOcclusion();
        if(name.indexOf("deferred1")>=0&&source.indexOf("vec3 worldPos = ViewToWorld(viewPos.xyz);")>=0)patched=patchComplementary46Angelica(source);
        else if(occlusion&&source.indexOf("void GetLighting")>=0&&source.indexOf("albedo *= smoothLighting;")>=0)patched=patchComplementaryForward(source);
        else if(source.indexOf("finalComposite *= 0.0001;")>=0&&source.indexOf("materialMask.sky")>=0){patched=patchSeusRenewed101Angelica(source);if(patched!=null)FMLLog.info("[BBCL] Installed Angelica SEUS Renewed radiation pass: %s",programName);}
        else if(name.indexOf("composite1")>=0&&source.indexOf("GetLightmapTorch")>=0&&source.indexOf("finalComposite *= 0.001f;")>=0)patched=patchSeus102(source);
        if(patched!=null&&name.indexOf("deferred1")>=0)FMLLog.info("[BBCL] Installed Angelica deferred radiation pass: %s",programName);
        if(patched!=null&&!loggedComplementary){loggedComplementary=true;FMLLog.info("[BBCL] Automatically patched Angelica/Iris shader program: %s",programName);}
        return patched==null?source:expandCapacity(patched);
    }
    public static InputStream patch(InputStream input,String path){
        if(input==null||path==null)return input;
        String normalized=path.replace('\\','/');
        boolean occlusion=ClientSettings.blockOcclusion();
        boolean complementaryPath=occlusion?normalized.endsWith("/shaders/lib/lighting/forwardLighting.glsl"):normalized.endsWith("/shaders/program/deferred1.glsl");
        boolean seusPath=normalized.endsWith("/composite2.fsh")||normalized.endsWith("/composite1.fsh");
        if(!complementaryPath&&!seusPath)return input;
        byte[] original;
        try{original=readAll(input);}catch(Exception e){FMLLog.warning("[BBCL] Could not read shader source %s: %s",path,e.toString());return input;}
        try{String source=new String(original,"UTF-8");if(source.indexOf("vclLightCount")>=0)return new ByteArrayInputStream(original);
            String patched=null;
            if(complementaryPath&&occlusion&&source.indexOf("void GetLighting")>=0&&source.indexOf("albedo *= smoothLighting;")>=0){
                patched=patchComplementaryForward(source);
                if(patched!=null&&!loggedComplementary){loggedComplementary=true;FMLLog.info("[BBCL] Automatically patched Complementary deferred lighting in memory: %s",path);}
            }else if(complementaryPath&&!occlusion&&source.indexOf("Complementary Shaders")>=0&&source.indexOf("vec3 worldPos = ViewToWorld(viewPos.xyz);")>=0){
                patched=patchComplementary46(source);
                if(patched!=null&&!loggedComplementary){loggedComplementary=true;FMLLog.info("[BBCL] Automatically patched Complementary smooth lighting in memory: %s",path);}
            }else if(normalized.endsWith("/composite2.fsh")&&source.indexOf("Do not modify this code until you have read the LICENSE.txt")>=0&&source.indexOf("finalComposite *= 0.0001;")>=0){
                patched=patchSeusRenewed101(source);
                if(patched!=null&&!loggedSeus){loggedSeus=true;FMLLog.info("[BBCL] Automatically patched SEUS Renewed lighting in memory: %s",path);}
            }else if(normalized.endsWith("/composite1.fsh")&&source.indexOf("GetLightmapTorch")>=0&&source.indexOf("finalComposite *= 0.001f;")>=0){
                patched=patchSeus102(source);
                if(patched!=null&&!loggedSeus){loggedSeus=true;FMLLog.info("[BBCL] Automatically patched SEUS v10.2 lighting in memory: %s",path);}
            }
            return new ByteArrayInputStream((patched==null?source:expandCapacity(patched)).getBytes("UTF-8"));
        }catch(Exception e){FMLLog.warning("[BBCL] Automatic shader patch failed for %s: %s",path,e.toString());return new ByteArrayInputStream(original);}
    }
    private static String patchComplementary46(String s){
        String uniformMark="uniform sampler2D depthtex0;";String programMark="//Program//";String positionMark="\t\tvec3 worldPos = ViewToWorld(viewPos.xyz);";
        if(s.indexOf(uniformMark)<0||s.indexOf(programMark)<0||s.indexOf(positionMark)<0)return null;
        String uniforms="\n\n// BBCL automatic per-block colored lighting\nuniform int vclLightCount;\nuniform vec4 vclLightPosRadius[32];\nuniform vec4 vclLightColorPower[32];";
        String function="vec3 VCLApplyLights(vec3 baseColor, vec3 worldPos) {\n\tvec3 weightedColor = vec3(0.0);\n\tfloat totalInfluence = 0.0;\n\tfor (int i = 0; i < 32; ++i) {\n\t\tif (i >= vclLightCount) break;\n\t\tvec3 delta = vclLightPosRadius[i].xyz - worldPos;\n\t\tfloat radius = max(vclLightPosRadius[i].w, 0.001);\n\t\tfloat attenuation = max(0.0, 1.0 - length(delta) / radius);\n\t\tattenuation *= attenuation;\n\t\tfloat influence = attenuation * vclLightColorPower[i].a;\n\t\tweightedColor += vclLightColorPower[i].rgb * influence;\n\t\ttotalInfluence += influence;\n\t}\n\tif (totalInfluence <= 0.0001) return baseColor;\n\tvec3 tint = weightedColor / totalInfluence;\n\ttint /= max(max(tint.r, tint.g), max(tint.b, 0.001));\n\ttint = mix(tint, pow(tint, vec3(1.65)), 0.72);\n\tfloat blendAmount = clamp(sqrt(totalInfluence) * 1.45, 0.0, 1.0);\n\tvec3 coloredLight = baseColor * (vec3(0.035) + tint * 1.20);\n\tcoloredLight += tint * totalInfluence * 0.045;\n\treturn mix(baseColor, coloredLight, blendAmount);\n}\n\n";
        s=replaceFirstLiteral(s,uniformMark,uniformMark+uniforms);s=replaceFirstLiteral(s,programMark,programMark+"\n"+function);s=replaceFirstLiteral(s,positionMark,positionMark+"\n\t\tcolor.rgb = VCLApplyLights(color.rgb, worldPos);");return s;
    }
    private static String patchComplementaryForward(String s){
        String mark="albedo *= smoothLighting;";
        if(s.indexOf(mark)<0)return null;
        String header="// BBCL torch-lightmap-gated coloured lighting\nuniform int vclLightCount;\nuniform vec4 vclLightPosRadius[32];\nuniform vec4 vclLightColorPower[32];\n\nvec3 VCLForwardLight(vec3 baseColor, vec3 worldPos, float blockLight) {\n\tvec3 weighted = vec3(0.0);\n\tfloat total = 0.0;\n\tfloat mask = smoothstep(0.015, 0.22, blockLight);\n\tfor (int i = 0; i < 32; ++i) {\n\t\tif (i >= vclLightCount) break;\n\t\tfloat radius = max(vclLightPosRadius[i].w, 0.001);\n\t\tfloat attenuation = max(0.0, 1.0 - length(vclLightPosRadius[i].xyz - worldPos) / radius);\n\t\tattenuation *= attenuation;\n\t\tfloat influence = attenuation * vclLightColorPower[i].a * mask;\n\t\tweighted += vclLightColorPower[i].rgb * influence;\n\t\ttotal += influence;\n\t}\n\tif (total <= 0.0001) return baseColor;\n\tvec3 tint = weighted / total;\n\ttint /= max(max(tint.r, tint.g), max(tint.b, 0.001));\n\tfloat peak = max(max(baseColor.r, baseColor.g), baseColor.b);\n\treturn mix(baseColor, tint * peak, clamp(sqrt(total) * 1.45, 0.0, 1.0));\n}\n\n";
        s=header+s;
        return replaceFirstLiteral(s,mark,mark+"\n\talbedo = VCLForwardLight(albedo, worldPos, lightmap.x);");
    }
    private static String patchComplementary46Angelica(String s){
        String uniformMark="uniform sampler2D depthtex0;",programMark="//Program//",positionMark="\t\tvec3 worldPos = ViewToWorld(viewPos.xyz);";
        if(s.indexOf(uniformMark)<0||s.indexOf(programMark)<0||s.indexOf(positionMark)<0)return null;
        String uniforms="\n\n// BBCL Angelica deferred coloured lighting\nuniform int vclLightCount;\nuniform vec4 vclLightPosRadius[32];\nuniform vec4 vclLightColorPower[32];";
        String function="vec3 VCLAngelicaLights(vec3 baseColor, vec3 worldPos) {\n\tvec3 weightedColor = vec3(0.0);\n\tfloat totalWeight = 0.0;\n\tfloat strongest = 0.0;\n\tfor (int i = 0; i < 32; ++i) {\n\t\tif (i >= vclLightCount) break;\n\t\tfloat radius = max(vclLightPosRadius[i].w, 0.001);\n\t\tfloat radial = max(0.0, 1.0 - length(vclLightPosRadius[i].xyz - worldPos) / radius);\n\t\tfloat attenuation = radial * radial * radial;\n\t\tfloat influence = attenuation * vclLightColorPower[i].a;\n\t\tfloat colorWeight = influence * (0.20 + 0.80 * influence);\n\t\tweightedColor += vclLightColorPower[i].rgb * colorWeight;\n\t\ttotalWeight += colorWeight;\n\t\tstrongest = max(strongest, influence);\n\t}\n\tif (totalWeight <= 0.000001) return baseColor;\n\tvec3 tint = weightedColor / totalWeight;\n\ttint /= max(max(tint.r, tint.g), max(tint.b, 0.001));\n\ttint = pow(tint, vec3(1.45));\n\tfloat luminance = max(dot(baseColor, vec3(0.2126, 0.7152, 0.0722)), max(max(baseColor.r, baseColor.g), baseColor.b) * 0.45);\n\tvec3 coloured = tint * luminance * 1.08;\n\tfloat dither = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898,78.233))) * 43758.5453) - 0.5;\n\tfloat blendAmount = clamp(1.0 - exp2(-strongest * 10.0) + dither / 255.0, 0.0, 1.0);\n\treturn mix(baseColor, coloured, blendAmount);\n}\n\n";
        s=replaceFirstLiteral(s,uniformMark,uniformMark+uniforms);s=replaceFirstLiteral(s,programMark,programMark+"\n"+function);return replaceFirstLiteral(s,positionMark,positionMark+"\n\t\tcolor.rgb = VCLAngelicaLights(color.rgb, worldPos);");
    }
    private static String patchSeusRenewed101(String s){
        String uniformMark="uniform vec3 cameraPosition;";
        String programMark="/////////////////////////MAIN//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////";
        String applyMark="\tfinalComposite *= 0.0001;";
        if(s.indexOf(uniformMark)<0||s.indexOf(programMark)<0||s.indexOf(applyMark)<0)return null;
        String uniforms="\n\n// BBCL automatic per-block coloured lighting\nuniform int vclLightCount;\nuniform vec4 vclLightPosRadius[32];\nuniform vec4 vclLightColorPower[32];";
        String function="vec3 VCLSeusLight(vec3 baseColor, vec3 albedo, vec3 worldPos) {\n\tvec3 weightedTint = vec3(0.0);\n\tfloat colourWeight = 0.0;\n\tfloat strongestInfluence = 0.0;\n\tfloat totalInfluence = 0.0;\n\tfor (int i = 0; i < 32; ++i) {\n\t\tif (i >= vclLightCount) break;\n\t\tvec3 delta = vclLightPosRadius[i].xyz - worldPos;\n\t\tfloat radius = max(vclLightPosRadius[i].w, 0.001);\n\t\tfloat attenuation = max(0.0, 1.0 - length(delta) / radius);\n\t\tattenuation *= attenuation;\n\t\tfloat influence = attenuation * vclLightColorPower[i].a;\n\t\tfloat softWeight = influence * influence;\n\t\tsoftWeight *= softWeight;\n\t\tweightedTint += vclLightColorPower[i].rgb * softWeight;\n\t\tcolourWeight += softWeight;\n\t\tstrongestInfluence = max(strongestInfluence, influence);\n\t\ttotalInfluence += influence;\n\t}\n\tif (colourWeight <= 0.000001) return baseColor;\n\tvec3 tint = weightedTint / colourWeight;\n\ttint /= max(max(tint.r, tint.g), max(tint.b, 0.001));\n\ttint = pow(tint, vec3(1.20));\n\tfloat basePeak = max(max(baseColor.r, baseColor.g), baseColor.b);\n\tvec3 coloured = tint * basePeak;\n\tcoloured += albedo * tint * min(totalInfluence * 0.00020, 0.0010);\n\tfloat blendAmount = clamp(sqrt(strongestInfluence) * 1.75, 0.0, 1.0);\n\treturn mix(baseColor, coloured, blendAmount);\n}\n\n";
        boolean occlusion=ClientSettings.blockOcclusion();
        if(occlusion){function=function.replace("vec3 VCLSeusLight(vec3 baseColor, vec3 albedo, vec3 worldPos)","vec3 VCLSeusLight(vec3 baseColor, vec3 albedo, vec3 worldPos, float blockLight)");function=function.replace("float influence = attenuation * vclLightColorPower[i].a;","float influence = attenuation * vclLightColorPower[i].a * smoothstep(0.015, 0.22, blockLight);");}
        // Blend colours throughout the complete attenuation volume. The
        // earlier fourth-power weighting produced a visible Voronoi-like seam.
        function=function.replace("float softWeight = influence * influence;\n\t\tsoftWeight *= softWeight;","float softWeight = attenuation;");
        function=function.replace("float blendAmount = clamp(sqrt(strongestInfluence) * 1.75, 0.0, 1.0);","float vclDither = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;\n\tfloat blendAmount = clamp(sqrt(strongestInfluence) * 1.75 + vclDither * (1.0 / 255.0), 0.0, 1.0);");
        s=replaceFirstLiteral(s,uniformMark,uniformMark+uniforms);
        s=replaceFirstLiteral(s,programMark,function+programMark);
        String call=occlusion?"VCLSeusLight(finalComposite, gbuffer.albedo.rgb, worldPos.xyz, gbuffer.mcLightmap.r)":"VCLSeusLight(finalComposite, gbuffer.albedo.rgb, worldPos.xyz)";
        s=replaceFirstLiteral(s,applyMark,applyMark+"\n\tif (materialMask.sky < 0.5) finalComposite = "+call+";");
        return s;
    }
    private static String patchSeusRenewed101Angelica(String s){
        String uniformMark="uniform vec3 cameraPosition;";
        String programMark="/////////////////////////MAIN//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////";
        String applyMark="\tfinalComposite *= 0.0001;";
        if(s.indexOf(uniformMark)<0||s.indexOf(programMark)<0||s.indexOf(applyMark)<0)return null;
        String uniforms="\n\n// BBCL Angelica SEUS Renewed coloured lighting\nuniform int vclLightCount;\nuniform vec4 vclLightPosRadius[32];\nuniform vec4 vclLightColorPower[32];";
        String function="vec3 VCLAngelicaSeus(vec3 baseColor, vec3 worldPos) {\n\tvec3 weighted = vec3(0.0);\n\tfloat totalWeight = 0.0;\n\tfloat strongest = 0.0;\n\tfor (int i = 0; i < 32; ++i) {\n\t\tif (i >= vclLightCount) break;\n\t\tfloat radius = max(vclLightPosRadius[i].w, 0.001);\n\t\tfloat radial = max(0.0, 1.0 - length(vclLightPosRadius[i].xyz - worldPos) / radius);\n\t\tfloat attenuation = radial * radial * radial;\n\t\tfloat influence = attenuation * vclLightColorPower[i].a;\n\t\tfloat colorWeight = influence * (0.20 + 0.80 * influence);\n\t\tweighted += vclLightColorPower[i].rgb * colorWeight;\n\t\ttotalWeight += colorWeight;\n\t\tstrongest = max(strongest, influence);\n\t}\n\tif (totalWeight <= 0.000001) return baseColor;\n\tvec3 tint = weighted / totalWeight;\n\ttint /= max(max(tint.r,tint.g),max(tint.b,0.001));\n\ttint = pow(tint,vec3(1.35));\n\tfloat luminance = max(dot(baseColor,vec3(0.2126,0.7152,0.0722)),max(max(baseColor.r,baseColor.g),baseColor.b)*0.45);\n\tvec3 coloured = tint * (luminance * 1.08 + strongest * 0.0015);\n\tfloat blend = clamp(1.0-exp2(-strongest*10.0),0.0,1.0);\n\treturn mix(baseColor,coloured,blend);\n}\n\n";
        function=function.replace("float strongest = 0.0;","float strongest = 0.0;\n\tfloat dominant = 0.0;\n\tvec3 dominantTint = vec3(1.0);");
        function=function.replace("strongest = max(strongest, influence);","strongest = max(strongest, influence);\n\t\tif (influence > dominant) { dominant = influence; dominantTint = vclLightColorPower[i].rgb; }");
        function=function.replace("vec3 tint = weighted / totalWeight;","vec3 tint = weighted / totalWeight;\n\tfloat chroma = max(max(tint.r,tint.g),tint.b) - min(min(tint.r,tint.g),tint.b);\n\ttint = mix(tint, dominantTint, (1.0-smoothstep(0.03,0.28,chroma))*0.82);");
        function=function.replace("tint = pow(tint,vec3(1.35))","tint = pow(tint,vec3(1.65))");
        function=function.replace("vec3 tint = weighted / totalWeight;","vec3 tint = weighted / totalWeight;\n\tfloat sharedWhite = min(tint.r,min(tint.g,tint.b));\n\ttint = max(tint - vec3(sharedWhite * 0.72),vec3(0.0));");
        function=function.replace("luminance * 1.08 + strongest * 0.0015","luminance * 0.96 + strongest * 0.000045");
        s=replaceFirstLiteral(s,uniformMark,uniformMark+uniforms);s=replaceFirstLiteral(s,programMark,function+programMark);
        String call="VCLAngelicaSeus(finalComposite, worldPos.xyz)";
        return replaceFirstLiteral(s,applyMark,applyMark+"\n\tif (materialMask.sky < 0.5) finalComposite = "+call+";");
    }

    private static String expandCapacity(String source){
        return source.replace("[32]","[256]").replace("i < 32","i < 256");
    }
    private static String patchSeus102(String s){
        String uniformMark="uniform vec3 cameraPosition;";
        String programMark="/////////////////////////////////////////////////////////////MAIN//////////////////////////////////////////////////////////////////////////////";
        String applyMark="\tfinalComposite *= 0.001f;";
        if(s.indexOf(uniformMark)<0||s.indexOf(programMark)<0||s.indexOf(applyMark)<0)return null;
        String uniforms="\n\n// BBCL automatic per-block coloured lighting\nuniform int vclLightCount;\nuniform vec4 vclLightPosRadius[32];\nuniform vec4 vclLightColorPower[32];";
        boolean occlusion=ClientSettings.blockOcclusion();
        String function="vec3 VCLSeus102Light(vec3 baseColor, vec3 worldPos"+(occlusion?", float blockLight":"")+") {\n\tvec3 weighted = vec3(0.0);\n\tfloat total = 0.0;\n\tfloat strongest = 0.0;\n\tfor (int i = 0; i < 32; ++i) {\n\t\tif (i >= vclLightCount) break;\n\t\tfloat radius = max(vclLightPosRadius[i].w, 0.001);\n\t\tfloat attenuation = max(0.0, 1.0 - length(vclLightPosRadius[i].xyz - worldPos) / radius);\n\t\tattenuation *= attenuation;\n\t\tfloat influence = attenuation * vclLightColorPower[i].a"+(occlusion?" * smoothstep(0.015, 0.22, blockLight)":"")+";\n\t\tweighted += vclLightColorPower[i].rgb * influence;\n\t\ttotal += influence;\n\t\tstrongest = max(strongest, influence);\n\t}\n\tif (total <= 0.0001) return baseColor;\n\tvec3 tint = weighted / total;\n\ttint /= max(max(tint.r, tint.g), max(tint.b, 0.001));\n\ttint = pow(tint, vec3(1.35));\n\tfloat peak = max(max(baseColor.r, baseColor.g), baseColor.b);\n\tvec3 coloured = tint * peak;\n\tfloat dither = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898,78.233))) * 43758.5453) - 0.5;\n\tfloat blend = clamp(1.0 - exp2(-strongest * 14.0) + dither / 255.0, 0.0, 1.0);\n\treturn mix(baseColor, coloured, blend);\n}\n\n";
        s=replaceFirstLiteral(s,uniformMark,uniformMark+uniforms);
        s=replaceFirstLiteral(s,programMark,function+programMark);
        String call="VCLSeus102Light(finalComposite, GetWorldSpacePosition(texcoord.st, surface.depth).xyz - cameraPosition"+(occlusion?", mcLightmap.torch":"")+")";
        return replaceFirstLiteral(s,applyMark,applyMark+"\n\tif (surface.mask.sky < 0.5) finalComposite = "+call+";");
    }
    private static byte[] readAll(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[]buf=new byte[8192];int n;while((n=in.read(buf))>=0)out.write(buf,0,n);in.close();return out.toByteArray();}
    private static String replaceFirstLiteral(String source,String target,String replacement){int i=source.indexOf(target);return i<0?source:source.substring(0,i)+replacement+source.substring(i+target.length());}
}
