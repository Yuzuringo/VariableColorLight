package jp.example.variablecolorlight.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import cpw.mods.fml.common.FMLLog;

public final class ShaderBindTransformer implements IClassTransformer {
    public byte[] transform(String name,String transformedName,byte[] bytes){
        if("net.coderbot.iris.shaderpack.ProgramSource".equals(name))return transformAngelicaSource(name,bytes);
        if("net.coderbot.iris.gl.program.Program".equals(name))return transformAngelicaProgram(name,bytes);
        if("shadersmod.client.ShaderPackZip".equals(name)||"shadersmod.client.ShaderPackFolder".equals(name))return transformShaderPack(name,bytes);
        if(!"shadersmod.client.Shaders".equals(name)&&!"net.optifine.shaders.Shaders".equals(name))return bytes;
        ClassNode cn=new ClassNode();new ClassReader(bytes).accept(cn,0);boolean changed=false;
        for(Object mo:cn.methods){MethodNode m=(MethodNode)mo;
            for(AbstractInsnNode in=m.instructions.getFirst();in!=null;in=in.getNext())if(in instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)in;
                boolean gl20=call.getOpcode()==Opcodes.INVOKESTATIC&&call.owner.equals("org/lwjgl/opengl/GL20")&&call.name.equals("glUseProgram")&&call.desc.equals("(I)V");
                boolean arb=call.getOpcode()==Opcodes.INVOKESTATIC&&call.owner.equals("org/lwjgl/opengl/ARBShaderObjects")&&call.name.equals("glUseProgramObjectARB")&&call.desc.equals("(I)V");
                if(gl20||arb){InsnList hook=new InsnList();hook.add(new InsnNode(Opcodes.DUP));m.instructions.insertBefore(call,hook);m.instructions.insert(call,new MethodInsnNode(Opcodes.INVOKESTATIC,"jp/example/variablecolorlight/ShaderLightRegistry",arb?"onProgramBoundARB":"onProgramBound","(I)V",false));changed=true;}
            }}
        if(!changed){FMLLog.warning("[BBCL] No OpenGL program bind was found in %s.",name);return bytes;}FMLLog.info("[BBCL] Installed shader uniform hook into %s.",name);ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);cn.accept(cw);return cw.toByteArray();
    }
    private byte[] transformShaderPack(String name,byte[] bytes){ClassNode cn=new ClassNode();new ClassReader(bytes).accept(cn,0);boolean changed=false;for(Object mo:cn.methods){MethodNode m=(MethodNode)mo;if(!m.name.equals("getResourceAsStream")||!m.desc.equals("(Ljava/lang/String;)Ljava/io/InputStream;"))continue;for(AbstractInsnNode in=m.instructions.getFirst();in!=null;in=in.getNext())if(in.getOpcode()==Opcodes.ARETURN){InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,1));hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"jp/example/variablecolorlight/ShaderSourcePatcher","patch","(Ljava/io/InputStream;Ljava/lang/String;)Ljava/io/InputStream;",false));m.instructions.insertBefore(in,hook);changed=true;}}if(!changed){FMLLog.warning("[BBCL] Could not hook shader source loader %s.",name);return bytes;}FMLLog.info("[BBCL] Installed automatic shader source patcher into %s.",name);ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);cn.accept(cw);return cw.toByteArray();}
    private byte[] transformAngelicaSource(String name,byte[] bytes){ClassNode cn=new ClassNode();new ClassReader(bytes).accept(cn,0);boolean changed=false;for(Object mo:cn.methods){MethodNode m=(MethodNode)mo;if(!m.name.equals("<init>"))continue;for(AbstractInsnNode in=m.instructions.getFirst();in!=null;in=in.getNext())if(in instanceof FieldInsnNode&&in.getOpcode()==Opcodes.PUTFIELD&&((FieldInsnNode)in).name.equals("fragmentSource")){InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,1));hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"jp/example/variablecolorlight/ShaderSourcePatcher","patchAngelica","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",false));m.instructions.insertBefore(in,hook);changed=true;}}if(!changed){FMLLog.warning("[BBCL] Could not hook Angelica ProgramSource.");return bytes;}FMLLog.info("[BBCL] Installed Angelica shader source adapter.");ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);cn.accept(cw);return cw.toByteArray();}
    private byte[] transformAngelicaProgram(String name,byte[] bytes){ClassNode cn=new ClassNode();new ClassReader(bytes).accept(cn,0);boolean changed=false;for(Object mo:cn.methods){MethodNode m=(MethodNode)mo;if(!m.name.equals("use")||!m.desc.equals("()V"))continue;for(AbstractInsnNode in=m.instructions.getFirst();in!=null;in=in.getNext())if(in instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)in;if(call.getOpcode()==Opcodes.INVOKESTATIC&&call.owner.equals("com/gtnewhorizons/angelica/glsm/GLStateManager")&&call.name.equals("glUseProgram")&&call.desc.equals("(I)V")){InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,0));hook.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/coderbot/iris/gl/program/Program","getProgramId","()I",false));hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"jp/example/variablecolorlight/ShaderLightRegistry","onProgramBound","(I)V",false));m.instructions.insert(in,hook);changed=true;}}}if(!changed){FMLLog.warning("[BBCL] Could not hook Angelica program binding.");return bytes;}FMLLog.info("[BBCL] Installed Angelica uniform upload hook.");ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);cn.accept(cw);return cw.toByteArray();}
}
