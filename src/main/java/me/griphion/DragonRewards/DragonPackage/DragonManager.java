package me.griphion.DragonRewards.DragonPackage;

import me.griphion.DragonRewards.ConfigFiles.WorldsConfig;
import me.griphion.DragonRewards.Core;
import org.bukkit.*;
import org.bukkit.boss.DragonBattle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.NumberConversions;

import java.time.LocalDateTime;

public class DragonManager implements Listener {
    private final Core plugin;

    public DragonManager(Core plugin){
        this.plugin = plugin;
    }

    public void detectDragonsByPlayersWorld(){ //Mas que nada para que funcione mejor con el PlugMan
        EnderDragon dragon;
        for (Player player : Bukkit.getOnlinePlayers()){
            if(player == null) continue;
            if(player.getWorld().getEnvironment().equals(World.Environment.THE_END)){
                DragonBattle dragonBattle = player.getWorld().getEnderDragonBattle();
                if(dragonBattle != null){
                    dragon = dragonBattle.getEnderDragon();
                    if(dragon != null) addEnderDragon(dragon,player.getWorld());
                }
            }

        }
    }

    public void detectNotSummonedDragonsOnPluginUnload(){
        for(World mundo : Core.dragonesActivos.keySet()){
            if(!Core.dragonesActivos.get(mundo).getDragonBattle().getRespawnPhase().equals(DragonBattle.RespawnPhase.NONE)){
                Core.worldsConfig.setRespawnDate(mundo.getName(),DragonScheduler.formatDateToString(LocalDateTime.now().plusSeconds(10)));
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEnderCrystalPlace(PlayerInteractEvent event){
        if(!plugin.getConfig().getBoolean("prevenir-invocacion-manual",true)) return;
        if(event.getItem() == null) return;
        if(!event.getItem().getType().equals(Material.END_CRYSTAL)) return;
        if(Core.worldsConfig.noEsUnMundoEndValido(event.getPlayer().getWorld())) return;
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if(event.getClickedBlock() == null) return;
        if(!isCloseToCenter(event.getClickedBlock().getLocation())) return;
        if(!(event.getClickedBlock().getType().equals(Material.BEDROCK) || event.getClickedBlock().getType().equals(Material.OBSIDIAN))) return;
        if(!event.getPlayer().hasPermission("dragonrewards.placeendcrystal")){
            event.getPlayer().sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "No tienes permiso para poner cristales del end aquí!");
            event.setCancelled(true);
        }
    }
    private boolean isCloseToCenter(final Location from){
        if(from == null) return false;
        if(from.getWorld() == null) return false;
        DragonBattle dragonBattle = from.getWorld().getEnderDragonBattle();
        if(dragonBattle == null) return false;
        Location centro = dragonBattle.getEndPortalLocation();
        if(centro == null) return false;
        return (Math.sqrt(NumberConversions.square(centro.getX() - from.getX()) + NumberConversions.square(centro.getZ() - from.getZ())) < 4);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        if(plugin.getConfig().getBoolean("recordatorio-recompensas",true)) {
            if (event.getPlayer().hasPermission("dragonrewards.recordatorio.reclamar"))
                if (plugin.playerDataConfig.tieneLootDisponible(event.getPlayer().getUniqueId())) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> event.getPlayer().sendMessage(Core.langManager.getTranslationWithPlayer("recordatorio-recompensas-mensaje", event.getPlayer(), plugin.playerDataConfig.obtenerListaDeLoot(event.getPlayer().getUniqueId()).size())), 40L);
                }
        }

        if(plugin.getConfig().getBoolean("recordatorio-dragon",true)){
            if (event.getPlayer().hasPermission("dragonrewards.recordatorio.dragon")){
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> enviarMensajeDragonesVivosYRespawn(event.getPlayer()), 50L);
            }
        }
    }

    public void enviarMensajeDragonesVivosYRespawn(CommandSender sender){
        String[] msg = obtenerFormatoDragonesVivosYRespawn();
        sender.sendMessage(Core.separador);
        if(msg == null){
            sender.sendMessage(ChatColor.RED + "No hay dragones programados o vivos");
        }else {
            sender.sendMessage(msg);
        }
        sender.sendMessage(Core.separador);
    }

    private String[] obtenerFormatoDragonesVivosYRespawn(){

        String[] retorno = new String[Core.worldsConfig.mundosEndNames.size()];
        int i = 0;

        for(String elem: Core.worldsConfig.mundosEndNames){
            if(Core.worldsConfig.getActivo(elem)){
                if(Core.worldsConfig.getConfig().contains(elem + WorldsConfig.RESPAWN_DATE) && Core.worldsConfig.getRespawnOnDeath(elem)){
                    retorno[i] = Core.langManager.getTranslationStringWithRespawnDate("recordatorio-dragon-mensaje-respawn", Core.worldsConfig.getRespawnDate(elem), Core.worldsConfig.getAlias(elem));
                    i++;
                }else if (Core.dragonesActivos.containsKey(Bukkit.getWorld(elem))){
                    retorno[i] = Core.langManager.getTranslationStringWithDragon("recordatorio-dragon-mensaje-vivo", Core.dragonesActivos.get(Bukkit.getWorld(elem)).getName(), Core.worldsConfig.getAlias(elem));
                    i++;
                }
            }
        }
        if(i == 0) return null;
        return retorno;

    }
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoinDetectDragon(PlayerJoinEvent event){
        World world = event.getPlayer().getWorld();
        EnderDragon dragon;

        if(world.getEnvironment().equals(World.Environment.THE_END)){
            DragonBattle dragonBattle = world.getEnderDragonBattle();
            if(dragonBattle !=null){
                dragon = dragonBattle.getEnderDragon();
                if(dragon == null) return;
            } else {
                return;
            }
        }else {
            return;
        }
        addEnderDragon(dragon,world);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleportDetectDragon(PlayerTeleportEvent event){
        if(event.getTo() == null) return;
        World world = event.getTo().getWorld();
        if(world == null) return;

        EnderDragon dragon;

        if(world.getEnvironment().equals(World.Environment.THE_END) && !event.getPlayer().getWorld().equals(world)){
            DragonBattle dragonBattle = world.getEnderDragonBattle();
            if(dragonBattle !=null){
                dragon = dragonBattle.getEnderDragon();
                if(dragon == null) return;
            } else {
                return;
            }
        }else {
            return;
        }
        addEnderDragon(dragon,world);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnyDragonDeath(EntityDeathEvent event){
        if(!event.getEntityType().equals(EntityType.ENDER_DRAGON)) return;
        World world = event.getEntity().getWorld();
        if(Core.worldsConfig.noEsUnMundoEndValido(world)) return;
        if(Core.worldsConfig.noEstaActivoEnEseMundo(world)) return;
        if(Core.worldsConfig.noTieneQueRespawnearDragon(world)) return;
        plugin.dragonScheduler.scheduleDragon(world);
    }

    private void addEnderDragon (final EnderDragon dragon, final World world){
        if(Core.worldsConfig.noEstaActivoEnEseMundo(world)) return;
        String dragonName = dragon.getCustomName();
        if (!Core.noEsUnDragonNameValido(dragonName) && !Core.dragonesActivos.containsKey(world)){
            Core.DragonNameAndFileName dragonNameAndFileName = Core.obtenerPorDragonName(dragonName);
            if(dragonNameAndFileName == null) return;
            new Dragon(dragonNameAndFileName.getDragonFileName(),world.getName(),plugin,false);
        }
    }

    public void destroy(){
        HandlerList.unregisterAll(this);
    }
}
