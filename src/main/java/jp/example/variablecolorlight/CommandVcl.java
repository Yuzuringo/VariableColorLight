package jp.example.variablecolorlight;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class CommandVcl extends CommandBase {
    public String getCommandName(){return "vcl";}
    public String getCommandUsage(ICommandSender s){return "/vcl entity <create|remove> | mode <kelvin|rgb> | kelvin <1000..20000> | rgb <0..255> <0..255> <0..255> | level <0..15>";}
    public int getRequiredPermissionLevel(){return 2;}
    public void processCommand(ICommandSender sender,String[] a){
        if(!(sender instanceof EntityPlayer))throw new net.minecraft.command.WrongUsageException(getCommandUsage(sender));
        EntityPlayer p=(EntityPlayer)sender;
        if(a.length==2&&"entity".equalsIgnoreCase(a[0])&&"create".equalsIgnoreCase(a[1])){
            Vec3 look=p.getLookVec();EntityVariableColorLight e=new EntityVariableColorLight(p.worldObj);
            e.setPosition(p.posX+look.xCoord*3,p.posY+p.getEyeHeight()+look.yCoord*3,p.posZ+look.zCoord*3);
            p.worldObj.spawnEntityInWorld(e);sender.addChatMessage(new ChatComponentText("Created BBCL entity light #"+e.getEntityId()+"."));return;
        }
        Target target=target(p);
        if(a.length==2&&"entity".equalsIgnoreCase(a[0])&&"remove".equalsIgnoreCase(a[1])){
            if(target==null||!(target.light instanceof EntityVariableColorLight)){sender.addChatMessage(new ChatComponentText("Look at a BBCL entity light within 8 blocks."));return;}
            ((EntityVariableColorLight)target.light).setDead();sender.addChatMessage(new ChatComponentText("Removed BBCL entity light."));return;
        }
        if(target==null){sender.addChatMessage(new ChatComponentText("Look at a BBCL block or entity light within 8 blocks."));return;}
        ColoredLightData l=target.light;
        try{
            if(a.length==2&&"mode".equalsIgnoreCase(a[0]))l.setRgbMode("rgb".equalsIgnoreCase(a[1]));
            else if(a.length==2&&"kelvin".equalsIgnoreCase(a[0])){l.setKelvin(parseIntBounded(sender,a[1],1000,20000));l.setRgbMode(false);}
            else if(a.length==4&&"rgb".equalsIgnoreCase(a[0]))l.setRgb(parseIntBounded(sender,a[1],0,255),parseIntBounded(sender,a[2],0,255),parseIntBounded(sender,a[3],0,255));
            else if(a.length==2&&"level".equalsIgnoreCase(a[0]))l.setLevel(parseIntBounded(sender,a[1],0,15));
            else throw new net.minecraft.command.WrongUsageException(getCommandUsage(sender));
            if(l instanceof TileVariableColorLamp){TileVariableColorLamp t=(TileVariableColorLamp)l;p.worldObj.markBlockForUpdate(t.xCoord,t.yCoord,t.zCoord);p.worldObj.updateLightByType(net.minecraft.world.EnumSkyBlock.Block,t.xCoord,t.yCoord,t.zCoord);}
            sender.addChatMessage(new ChatComponentText(status(l)));
        }catch(NumberFormatException e){throw new net.minecraft.command.WrongUsageException(getCommandUsage(sender));}
    }
    private static Target target(EntityPlayer p){
        Vec3 from=Vec3.createVectorHelper(p.posX,p.posY+p.getEyeHeight(),p.posZ),look=p.getLookVec(),to=from.addVector(look.xCoord*8,look.yCoord*8,look.zCoord*8);
        MovingObjectPosition hit=p.worldObj.rayTraceBlocks(from,to);double blockDistance=hit==null?Double.MAX_VALUE:from.distanceTo(hit.hitVec);Target best=null;
        if(hit!=null){TileEntity te=p.worldObj.getTileEntity(hit.blockX,hit.blockY,hit.blockZ);if(te instanceof TileVariableColorLamp)best=new Target((TileVariableColorLamp)te,blockDistance);}
        List entities=p.worldObj.getEntitiesWithinAABB(EntityVariableColorLight.class,AxisAlignedBB.getBoundingBox(Math.min(from.xCoord,to.xCoord)-1,Math.min(from.yCoord,to.yCoord)-1,Math.min(from.zCoord,to.zCoord)-1,Math.max(from.xCoord,to.xCoord)+1,Math.max(from.yCoord,to.yCoord)+1,Math.max(from.zCoord,to.zCoord)+1));
        for(Object o:entities){Entity e=(Entity)o;double vx=e.posX-from.xCoord,vy=e.posY-from.yCoord,vz=e.posZ-from.zCoord;double along=vx*look.xCoord+vy*look.yCoord+vz*look.zCoord;if(along<0||along>8||along>blockDistance)continue;double dx=vx-look.xCoord*along,dy=vy-look.yCoord*along,dz=vz-look.zCoord*along;if(dx*dx+dy*dy+dz*dz<=0.64&&(best==null||along<best.distance))best=new Target((EntityVariableColorLight)e,along);}
        return best;
    }
    private static String status(ColoredLightData l){return l.isRgbMode()?"Light: RGB "+l.getRed()+","+l.getGreen()+","+l.getBlue()+" / level "+l.getLevel():"Light: Kelvin "+l.getKelvin()+" K / level "+l.getLevel();}
    private static final class Target{final ColoredLightData light;final double distance;Target(ColoredLightData l,double d){light=l;distance=d;}}
}
