package me.griphion.DragonRewards.DragonPackage;

import me.griphion.DragonRewards.Utils.HomingDragonFireballTask;
import me.griphion.DragonRewards.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class DragonPassivePower{

    Plugin plugin;
    Dragon dragon;

    public DragonPassivePower(Plugin plugin, Dragon dragon) {
        this.plugin = plugin;
        this.dragon = dragon;
    }
    public static DragonPassivePower parseDragonPassivePower(String name, Plugin plugin, Dragon dragon){
        switch (name){
            case "HomingFireball":
                return new HomingFireballPassive(plugin,dragon);
            case "InmuneToArrows":
                return new InmuneToArrowsPassive(plugin, dragon);
            case "AirSuperiority":
                return new AirSuperiorityPassive(plugin, dragon);
            default:
                return null;
        }
    }

    public void destroy() {
        dragon = null;
    }
}

class HomingFireballPassive extends DragonPassivePower implements Listener{

    public HomingFireballPassive(Plugin plugin, Dragon dragon) {
        super(plugin, dragon);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {
        HandlerList.unregisterAll(this);
        super.destroy();
    }

    @EventHandler(ignoreCancelled = true)
    private void dragonFireballShootEvent(EntitySpawnEvent e) {
        if(!(e.getEntity() instanceof DragonFireball)) return;
        DragonFireball dragonFireball = (DragonFireball) e.getEntity();
        if(dragonFireball.getShooter() != dragon.getDragon()) return;
        Player primerJugador = Utils.getRandomClosePlayer(dragon.getDragon(),128,128,128);
        if (primerJugador != null) {
            new HomingDragonFireballTask(dragonFireball, primerJugador, plugin);
        }

    }
}

class InmuneToArrowsPassive extends DragonPassivePower implements Listener{

    public InmuneToArrowsPassive(Plugin plugin, Dragon dragon) {
        super(plugin, dragon);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {
        HandlerList.unregisterAll(this);
        super.destroy();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void dragonGetShotEvent(EntityDamageByEntityEvent event){
        if(!event.getEntity().equals(dragon.getDragon())) return;
        if(!event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) return;
        event.setCancelled(true);
    }
}

class AirSuperiorityPassive extends DragonPassivePower{

    private BukkitTask detector;

    public AirSuperiorityPassive(Plugin plugin, Dragon dragon) {
        super(plugin, dragon);
        startDetector();
    }
    @Override
    public void destroy() {
        super.destroy();
        if(detector != null){
            detector.cancel();
            detector = null;
        }
    }

    public void startDetector(){
        detector = new BukkitRunnable(){

            @Override
            public void run() {
                for(Player player : Utils.getAllClosePlayers(dragon.getDragon(),32,32,32)){
                    if(player.isFlying()){
                        player.setFlying(false);
                        player.sendMessage(ChatColor.RED + "La influencia de " + dragon.getName() + ChatColor.RED + " no te permite volar cerca de este!");
                    }
                    if(player.isGliding()){
                        player.setGliding(false);
                        player.sendMessage(ChatColor.RED + "La influencia de " + dragon.getName() + ChatColor.RED + " no te permite volar cerca de este!");
                    }
                }

            }
        }.runTaskTimer(plugin,0,1L);
    }
}
