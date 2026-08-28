package jp.example.variablecolorlight.spigot;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class VariableColorLightPlugin extends JavaPlugin implements Listener {
    private static final String CHANNEL="VCL|SYNC";
    private final Map<String,CopiedLights> copiedLights=new HashMap<String,CopiedLights>();
    public void onEnable(){getServer().getMessenger().registerOutgoingPluginChannel(this,CHANNEL);getServer().getPluginManager().registerEvents(this,this);}
    @EventHandler public void join(PlayerJoinEvent e){sendLater(e.getPlayer());}
    @EventHandler public void world(PlayerChangedWorldEvent e){sendLater(e.getPlayer());}
    @EventHandler public void worldEditCommand(PlayerCommandPreprocessEvent e){
        String command=e.getMessage().trim().toLowerCase();
        if(command.equals("//copy")||command.startsWith("//copy "))captureWorldEditCopy(e.getPlayer());
        else if(command.equals("//paste")||command.startsWith("//paste "))pasteWorldEditCopy(e.getPlayer(),command.indexOf("-o")>=0);
    }
    @EventHandler public void broken(BlockBreakEvent e){
        if(remove(e.getBlock().getLocation()))broadcastLater(e.getBlock().getWorld());
    }
    @EventHandler public void exploded(EntityExplodeEvent e){
        boolean changed=false;for(Block b:e.blockList())changed|=remove(b.getLocation());
        if(changed)broadcastLater(e.getLocation().getWorld());
    }
    @EventHandler public void pistonExtend(BlockPistonExtendEvent e){
        List<MovedLight> moves=new ArrayList<MovedLight>();
        for(Block b:e.getBlocks()){Light light=read(b.getLocation());if(light!=null)moves.add(new MovedLight(b.getLocation(),b.getRelative(e.getDirection()).getLocation(),light));}
        if(!moves.isEmpty()){moveLater(moves,e.getBlock().getWorld());}
    }
    @EventHandler public void pistonRetract(BlockPistonRetractEvent e){
        if(!e.isSticky())return;Location from=e.getRetractLocation();Light light=read(from);if(light!=null){List<MovedLight> moves=new ArrayList<MovedLight>();moves.add(new MovedLight(from,from.clone().subtract(e.getDirection().getModX(),e.getDirection().getModY(),e.getDirection().getModZ()),light));moveLater(moves,e.getBlock().getWorld());}
    }
    @EventHandler public void interact(PlayerInteractEvent e){
        if(e.getAction()!=Action.RIGHT_CLICK_BLOCK||e.getClickedBlock()==null||e.getPlayer().getItemInHand()==null)return;
        Material m=e.getClickedBlock().getType(); if(m!=Material.REDSTONE_LAMP_ON&&m!=Material.REDSTONE_LAMP_OFF)return;
        if(e.getPlayer().getItemInHand().getType()!=Material.BLAZE_ROD||!e.getPlayer().hasPermission("variablecolorlight.edit"))return;
        e.setCancelled(true);Location l=e.getClickedBlock().getLocation();String key=key(l);int k=getConfig().getInt(key+".kelvin",3000),level=getConfig().getInt(key+".level",15);
        if(e.getPlayer().isSneaking())level=(level+1)&15;else k=k>=10000?2000:k+500;
        save(l,false,k,255,255,255,level);
        e.getClickedBlock().setType(level==0?Material.REDSTONE_LAMP_OFF:Material.REDSTONE_LAMP_ON);e.getPlayer().sendMessage(ChatColor.GOLD+"Lamp: "+k+" K / light "+level);broadcast(l.getWorld());
    }
    private void sendLater(final Player p){getServer().getScheduler().runTaskLater(this,new Runnable(){public void run(){send(p);}},20L);}
    private void broadcastLater(final World w){getServer().getScheduler().runTask(this,new Runnable(){public void run(){broadcast(w);}});}
    private void moveLater(final List<MovedLight> moves,final World w){getServer().getScheduler().runTask(this,new Runnable(){public void run(){for(MovedLight m:moves)remove(m.from);for(MovedLight m:moves)save(m.to,m.light.mode,m.light.k,m.light.r,m.light.g,m.light.b,m.light.level);broadcast(w);}});}
    private void captureWorldEditCopy(Player p){
        try{Plugin we=getServer().getPluginManager().getPlugin("WorldEdit");if(we==null)return;Object selection=we.getClass().getMethod("getSelection",Player.class).invoke(we,p);if(selection==null)return;
            Location min=(Location)selection.getClass().getMethod("getMinimumPoint").invoke(selection),max=(Location)selection.getClass().getMethod("getMaximumPoint").invoke(selection);Location origin=p.getLocation().getBlock().getLocation();List<RelativeLight> entries=new ArrayList<RelativeLight>();
            ConfigurationSection root=getConfig().getConfigurationSection("lights");if(root!=null)for(String id:root.getKeys(false)){ConfigurationSection s=root.getConfigurationSection(id);if(s==null||!p.getWorld().getName().equals(s.getString("world")))continue;int x=s.getInt("x"),y=s.getInt("y"),z=s.getInt("z");if(x<min.getBlockX()||x>max.getBlockX()||y<min.getBlockY()||y>max.getBlockY()||z<min.getBlockZ()||z>max.getBlockZ())continue;Light l=new Light(x,y,z,s.getInt("kelvin",3000),s.getInt("level",15),s.getBoolean("rgbMode",false),s.getInt("red",255),s.getInt("green",180),s.getInt("blue",107));entries.add(new RelativeLight(x-origin.getBlockX(),y-origin.getBlockY(),z-origin.getBlockZ(),l));}
            copiedLights.put(p.getUniqueId().toString(),new CopiedLights(origin,entries));
        }catch(Exception ex){getLogger().warning("Unable to capture WorldEdit selection: "+ex.toString());}
    }
    private void pasteWorldEditCopy(final Player p,boolean originalPosition){
        final CopiedLights copy=copiedLights.get(p.getUniqueId().toString());if(copy==null||copy.entries.isEmpty())return;final Location origin=originalPosition?copy.origin.clone():p.getLocation().getBlock().getLocation();
        getServer().getScheduler().runTaskLater(this,new Runnable(){public void run(){int created=0;for(RelativeLight r:copy.entries){Location at=new Location(origin.getWorld(),origin.getBlockX()+r.dx,origin.getBlockY()+r.dy,origin.getBlockZ()+r.dz);Material type=at.getBlock().getType();if(type!=Material.REDSTONE_LAMP_ON&&type!=Material.REDSTONE_LAMP_OFF)continue;save(at,r.light.mode,r.light.k,r.light.r,r.light.g,r.light.b,r.light.level);created++;}if(created>0){broadcast(origin.getWorld());p.sendMessage(ChatColor.GOLD+"Copied "+created+" variable-light setting(s).");}}},2L);
    }
    private void broadcast(World w){for(Player p:w.getPlayers())send(p);}
    public boolean onCommand(CommandSender sender,Command command,String label,String[] a){
        if(!(sender instanceof Player))return false;Player p=(Player)sender;Block block=p.getTargetBlock((java.util.HashSet<Byte>)null,8);if(block==null||(block.getType()!=Material.REDSTONE_LAMP_ON&&block.getType()!=Material.REDSTONE_LAMP_OFF)){p.sendMessage(ChatColor.RED+"Look at a redstone lamp within 8 blocks.");return true;}
        Location l=block.getLocation();String key=key(l);boolean mode=getConfig().getBoolean(key+".rgbMode",false);int k=getConfig().getInt(key+".kelvin",3000),r=getConfig().getInt(key+".red",255),g=getConfig().getInt(key+".green",180),b=getConfig().getInt(key+".blue",107),level=getConfig().getInt(key+".level",15);
        try{if(a.length==2&&a[0].equalsIgnoreCase("mode")){if(!a[1].equalsIgnoreCase("rgb")&&!a[1].equalsIgnoreCase("kelvin"))return false;mode=a[1].equalsIgnoreCase("rgb");}
            else if(a.length==2&&a[0].equalsIgnoreCase("kelvin")){k=range(a[1],1000,20000);mode=false;}
            else if(a.length==4&&a[0].equalsIgnoreCase("rgb")){r=range(a[1],0,255);g=range(a[2],0,255);b=range(a[3],0,255);mode=true;}
            else if(a.length==2&&a[0].equalsIgnoreCase("level"))level=range(a[1],0,15);else return false;
        }catch(NumberFormatException ex){p.sendMessage(ChatColor.RED+"Value is outside the allowed range.");return true;}
        save(l,mode,k,r,g,b,level);block.setType(level==0?Material.REDSTONE_LAMP_OFF:Material.REDSTONE_LAMP_ON);broadcast(l.getWorld());p.sendMessage(ChatColor.GOLD+(mode?"Lamp: RGB "+r+","+g+","+b:"Lamp: Kelvin "+k+" K")+" / light "+level);return true;
    }
    private static int range(String s,int min,int max){int v=Integer.parseInt(s);if(v<min||v>max)throw new NumberFormatException();return v;}
    private void save(Location l,boolean mode,int k,int r,int g,int b,int level){String key=key(l);getConfig().set(key+".world",l.getWorld().getName());getConfig().set(key+".x",l.getBlockX());getConfig().set(key+".y",l.getBlockY());getConfig().set(key+".z",l.getBlockZ());getConfig().set(key+".rgbMode",mode);getConfig().set(key+".kelvin",k);getConfig().set(key+".red",r);getConfig().set(key+".green",g);getConfig().set(key+".blue",b);getConfig().set(key+".level",level);saveConfig();}
    private void send(Player p){try{List<Light> ls=load(p.getWorld());int limit=Math.min(1400,ls.size());ByteArrayOutputStream raw=new ByteArrayOutputStream();DataOutputStream d=new DataOutputStream(raw);d.writeByte(2);d.writeShort(limit);for(int i=0;i<limit;i++){Light l=ls.get(i);d.writeInt(l.x);d.writeInt(l.y);d.writeInt(l.z);d.writeShort(l.k);d.writeByte(l.mode?1:0);d.writeByte(l.r);d.writeByte(l.g);d.writeByte(l.b);d.writeByte(l.level);d.writeInt(p.getWorld().getEnvironment()==World.Environment.NETHER?-1:p.getWorld().getEnvironment()==World.Environment.THE_END?1:0);}p.sendPluginMessage(this,CHANNEL,raw.toByteArray());}catch(Exception ex){getLogger().warning(ex.toString());}}
    private List<Light> load(World w){List<Light> out=new ArrayList<Light>();ConfigurationSection root=getConfig().getConfigurationSection("lights");if(root==null)return out;boolean dirty=false;for(String id:new ArrayList<String>(root.getKeys(false))){ConfigurationSection s=root.getConfigurationSection(id);if(s!=null&&w.getName().equals(s.getString("world"))){Block block=w.getBlockAt(s.getInt("x"),s.getInt("y"),s.getInt("z"));if(block.getType()!=Material.REDSTONE_LAMP_ON&&block.getType()!=Material.REDSTONE_LAMP_OFF){getConfig().set("lights."+id,null);dirty=true;continue;}out.add(new Light(s.getInt("x"),s.getInt("y"),s.getInt("z"),s.getInt("kelvin",3000),s.getInt("level",15),s.getBoolean("rgbMode",false),s.getInt("red",255),s.getInt("green",180),s.getInt("blue",107)));}}if(dirty)saveConfig();return out;}
    private Light read(Location l){String k=key(l);if(!getConfig().isConfigurationSection(k))return null;return new Light(l.getBlockX(),l.getBlockY(),l.getBlockZ(),getConfig().getInt(k+".kelvin",3000),getConfig().getInt(k+".level",15),getConfig().getBoolean(k+".rgbMode",false),getConfig().getInt(k+".red",255),getConfig().getInt(k+".green",180),getConfig().getInt(k+".blue",107));}
    private boolean remove(Location l){String k=key(l);if(!getConfig().isConfigurationSection(k))return false;getConfig().set(k,null);saveConfig();return true;}
    private String key(Location l){return "lights."+l.getWorld().getName().replace('.','_')+"_"+l.getBlockX()+"_"+l.getBlockY()+"_"+l.getBlockZ();}
    private static final class Light{final int x,y,z,k,level,r,g,b;final boolean mode;Light(int x,int y,int z,int k,int l,boolean m,int r,int g,int b){this.x=x;this.y=y;this.z=z;this.k=k;level=l;mode=m;this.r=r;this.g=g;this.b=b;}}
    private static final class MovedLight{final Location from,to;final Light light;MovedLight(Location f,Location t,Light l){from=f;to=t;light=l;}}
    private static final class RelativeLight{final int dx,dy,dz;final Light light;RelativeLight(int x,int y,int z,Light l){dx=x;dy=y;dz=z;light=l;}}
    private static final class CopiedLights{final Location origin;final List<RelativeLight> entries;CopiedLights(Location o,List<RelativeLight> e){origin=o;entries=e;}}
}
