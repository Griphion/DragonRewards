package me.griphion.DragonRewards.DragonPackage;

import me.griphion.DragonRewards.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;


public abstract class DragonActivePower {

    Plugin plugin;
    Dragon dragon;
    int cooldown;
    boolean isInCooldownOrActive;
    public static final String entityMetadataName = "dragonrewards";
    public final MetadataValue entityMetadataValue;

    public DragonActivePower(Plugin plugin, Dragon dragon, int cooldown) {
        this.plugin = plugin;
        this.dragon = dragon;
        this.cooldown = cooldown;
        entityMetadataValue = new FixedMetadataValue(plugin,1);
        setPowerNotInCooldown();
    }

    public static DragonActivePower parseDragonActivePower(String name, Plugin plugin, Dragon dragon, int cooldown){
        if(cooldown < 0) return null;
        switch (name){
            case "FireballBurst":
                return new FireballBurstActive(plugin,dragon,cooldown);
            case "Pulgas":
                return new PulgasActive(plugin,dragon,cooldown);
            case "ThunderInstaKill":
                return new ThunderInstaKillActive(plugin,dragon,cooldown);
            case "IceShardRain":
                return new IceShardRainActive(plugin, dragon, cooldown);
            default:
                return null;
        }
    }

    public void setPowerNotInCooldown(){
        isInCooldownOrActive = false;
    }
    public void setPowerInCooldown(){
        isInCooldownOrActive = true;
    }
    public void destroy() {

        dragon = null;
    }
    public void startCooldown(){
        if(cooldown == 0) {
            setPowerNotInCooldown();
            return;
        }
        setPowerInCooldown();
        plugin.getServer().getScheduler().runTaskLater(plugin, this::setPowerNotInCooldown, cooldown * 20L);
    }
    public boolean isInCooldownOrActive(){
        if(dragon == null || dragon.getDragon() == null || dragon.getDragon().isDead()) return true;
        return isInCooldownOrActive;
    }

    public abstract void activate();
    public abstract boolean canBeActivated();

}

class FireballBurstActive extends DragonActivePower implements Listener {

    public FireballBurstActive(Plugin plugin, Dragon dragon, int cooldown) {
        super(plugin, dragon, cooldown);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    void onFireballExplode(ExplosionPrimeEvent event){
        if(dragon.getDragon() == null) return;
        if(event.getEntity().getLocation().getWorld() != dragon.getDragon().getWorld()) return;
        if(!(event.getEntity() instanceof Fireball)) return;

        event.getEntity().getLocation().getWorld().createExplosion(event.getEntity().getLocation(), 3,true,false,dragon.getDragon());
        event.setCancelled(true);
    }

    @Override
    public void destroy(){
        HandlerList.unregisterAll(this);
        super.destroy();
    }

    @Override
    public void activate(){
        if(isInCooldownOrActive()) return;
        setPowerInCooldown();
        new BukkitRunnable() {
            int cantidad = 0;

            @Override
            public void run() {
                if(dragon == null || dragon.getDragon() == null || dragon.getDragon().isDead()){
                    cancel();
                    return;
                }
                if (cantidad >= 5) {
                    cancel();
                    startCooldown();
                    return;
                }
                lanzarBolaDeFuego(getDragonHeadLocation());
                cantidad++;
            }
        }.runTaskTimer(plugin, 0, 20L);

    }

    @Override
    public boolean canBeActivated() {
        return !isInCooldownOrActive() && !(dragon.getDragon().getPhase().equals(EnderDragon.Phase.LAND_ON_PORTAL) || dragon.getDragon().getPhase().equals(EnderDragon.Phase.FLY_TO_PORTAL) || dragon.getDragon().getPhase().equals(EnderDragon.Phase.DYING));
    }

    private Location getDragonHeadLocation(){
        if(dragon.getDragon() == null) return null;
        for (ComplexEntityPart part: dragon.getDragon().getParts()){
            return part.getLocation();
        }
        return null;
    }

    private void lanzarBolaDeFuego(Location posicion){
        if(posicion == null) return;
        if(posicion.getWorld() == null) return;
        Player player = Utils.getRandomClosePlayer(dragon.getDragon(),128,128,128);
        if(player == null) return;
        if(dragon.getDragon() == null) return;
        if(!player.getWorld().equals(dragon.getDragon().getWorld())) return;
        try {
            Fireball fireball = (Fireball) dragon.getDragon().getWorld().spawnEntity(posicion,EntityType.FIREBALL);
            fireball.setDirection(player.getLocation().toVector().subtract(posicion.toVector()).normalize());
            fireball.setYield(0);
            fireball.setShooter(dragon.getDragon());
        }catch (Exception ignore){ }
    }

}

class PulgasActive extends DragonActivePower{

    public PulgasActive(Plugin plugin, Dragon dragon, int cooldown) {
        super(plugin, dragon, cooldown);
    }

    @Override
    public void activate() {
        if(dragon.getDragon().getWorld().getDifficulty().equals(Difficulty.PEACEFUL)) return;
        if(isInCooldownOrActive()) return;

        if (dragon.getDragon() == null) return;
        if (dragon.getDragonBattle() == null) return;

        Location portalLocation = dragon.getDragonBattle().getEndPortalLocation();
        if (portalLocation == null) return;

        double xPosAux = portalLocation.getX();
        double zPosAux = portalLocation.getZ();
        portalLocation.setY(portalLocation.getY() + 1);

        portalLocation.setX(xPosAux + 3.5);
        portalLocation.setZ(zPosAux - 0.5); //3.5X -0.5Z
        spawnEndermite(portalLocation,0);
        portalLocation.setZ(zPosAux + 1.5); //3.5X 1.5Z
        spawnEndermite(portalLocation,1);
        portalLocation.setX(xPosAux - 2.5); //-2.5X 1.5Z
        spawnEndermite(portalLocation,2);
        portalLocation.setZ(zPosAux - 0.5);//-2.5X -0.5Z
        spawnEndermite(portalLocation,3);
        portalLocation.setX(xPosAux - 0.5);
        portalLocation.setZ(zPosAux + 3.5);//-0.5X 3.5Z
        spawnEndermite(portalLocation,4);
        portalLocation.setX(xPosAux + 1.5);//1.5X 3.5Z
        spawnEndermite(portalLocation,5);
        portalLocation.setZ(zPosAux - 2.5);//1.5X -2.5Z
        spawnEndermite(portalLocation,6);
        portalLocation.setX(xPosAux - 0.5);//-0.5X -2.5Z
        spawnEndermite(portalLocation,7);

        startCooldown();
    }

    @Override
    public boolean canBeActivated() {
        return !isInCooldownOrActive() && dragon.getDragon().getPhase().equals(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);
    }

    private void spawnEndermite(Location location, int delay){

        World world = location.getWorld();
        if(world == null) return;
        new BukkitRunnable(){
            @Override
            public void run() {
                world.spawnParticle(Particle.FLAME, location,10, 1.0D, 1.0D, 1.0D);
                Endermite endermite = location.getWorld().spawn(location,Endermite.class);
                endermite.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,9999,1));
                endermite.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,9999,2));
                endermite.setCustomName(ChatColor.LIGHT_PURPLE + "Pulga");
                endermite.setMetadata(entityMetadataName,entityMetadataValue);
            }
        }.runTaskLater(plugin,delay * 20L);
    }
}

class ThunderInstaKillActive extends DragonActivePower implements Listener{


    public ThunderInstaKillActive(Plugin plugin, Dragon dragon, int cooldown) {
        super(plugin, dragon, cooldown);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy(){
        HandlerList.unregisterAll(this);
        super.destroy();
    }

    @EventHandler
    public void detectThunderDamage(EntityDamageByEntityEvent event){
        if(event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING) && event.getDamager().getWorld().equals(dragon.getDragon().getWorld()) && event.getDamager().hasMetadata("ThunderInstaKill") && (event.getEntity() instanceof Player)){
            if(event.getEntity().isDead()) return;
            ((Player) event.getEntity()).setHealth(0);
        }
    }

    @Override
    public void activate() {
        if(isInCooldownOrActive()) return;
        setPowerInCooldown();
        for (Player player : Utils.getAllClosePlayers(dragon.getDragon(),128,128,128)) {
            playEffect(player.getLocation());
        }
    }

    private void playEffect(Location location){
        Location locationCopy = location.clone();
        if(locationCopy.getWorld() == null) return;
        new BukkitRunnable(){
            int counter = 0;
            double t = 0;
            final double r = 5;
            @Override
            public void run() {
                if(dragon == null || dragon.getDragon() == null || dragon.getDragon().isDead()){
                    cancel();
                    return;
                }
                while (t < Math.PI*4){
                    t = t + Math.PI/8;
                    double x = r*Math.cos(t);
                    double z = r*Math.sin(t);
                    locationCopy.add(x,0,z);
                    locationCopy.getWorld().spawnParticle(Particle.CRIT_MAGIC,locationCopy,10);
                    locationCopy.subtract(x,0,z);
                }
                t = 0;
                counter++;
                if(counter == 4){
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin,0,5L);
        new BukkitRunnable(){
            int counter = 0;
            double y = 5;
            @Override
            public void run() {
                if(dragon == null || dragon.getDragon() == null || dragon.getDragon().isDead()){
                    cancel();
                    return;
                }
                while (y >= 0){
                    locationCopy.add(0,y,0);
                    locationCopy.getWorld().spawnParticle(Particle.CRIT_MAGIC,locationCopy,10);
                    locationCopy.subtract(0,y,0);
                    y -= 0.5;
                }
                y = 5;
                counter++;
                locationCopy.getWorld().playSound(locationCopy,Sound.BLOCK_ANVIL_LAND,10,1.85F);
                if(counter == 4){
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin,0,5L);
        new BukkitRunnable(){
            @Override
            public void run() {
                locationCopy.getWorld().playSound(locationCopy,Sound.BLOCK_ANVIL_LAND,10,1.85F);
                locationCopy.getWorld().playSound(locationCopy,Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED,10,1F);
            }
        }.runTaskLater(plugin,20);
        new BukkitRunnable(){
            double t = 0;
            double r = 5;
            final double y = 0;
            @Override
            public void run() {
                if(dragon == null || dragon.getDragon() == null || dragon.getDragon().isDead()){
                    cancel();
                    return;
                }
                while (t < Math.PI*4){
                    t = t + Math.PI/8;
                    double x = r*Math.cos(t);
                    double z = r*Math.sin(t);
                    locationCopy.add(x,y,z);
                    locationCopy.getWorld().spawnParticle(Particle.CRIT,locationCopy,10);
                    locationCopy.subtract(x,y,z);
                }
                t = 0;
                r--;
                if(r == 0){
                    this.cancel();
                    Objects.requireNonNull(locationCopy.getWorld()).strikeLightning(locationCopy).setMetadata("ThunderInstaKill",entityMetadataValue);
                    startCooldown();
                }
            }
        }.runTaskTimer(plugin,20,4L);
    }
    @Override
    public boolean canBeActivated() {
        return !isInCooldownOrActive();
    }
}

class IceShardRainActive extends DragonActivePower implements Listener {

    List<FallingBlock> iceShardsList;
    BukkitTask detector;
    int activeIceShardRainTasks;

    public IceShardRainActive(Plugin plugin, Dragon dragon, int cooldown) {
        super(plugin, dragon, cooldown);
        iceShardsList = new ArrayList<>();
        activeIceShardRainTasks = 0;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {
        super.destroy();
        stopIceShardImpactDetector();
        if(!iceShardsList.isEmpty()){
            for(FallingBlock iceShard : iceShardsList){
                explodeIceShard(iceShard);
            }
        }

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onIceHitGround(EntityChangeBlockEvent event){
        if(event.getEntity().hasMetadata("IceShardRain") && event.getEntity().getWorld().equals(dragon.getDragon().getWorld()) ){
            event.setCancelled(true);
            explodeIceShard((FallingBlock) event.getEntity());
        }
    }

    @EventHandler
    public void onIceDestroyAndDrop(EntityDropItemEvent event){
        if(event.getEntity().hasMetadata("IceShardRain") && event.getEntity().getWorld().equals(dragon.getDragon().getWorld()) ){
            event.setCancelled(true);
            explodeIceShard((FallingBlock) event.getEntity());
        }
    }


    private void explodeEffect(Location location){
        if(location.getWorld() == null) return;
        Particle.DustOptions dustColor = new Particle.DustOptions(Color.fromBGR(255,220,51),2);
        location.getWorld().spawnParticle(Particle.REDSTONE,location,250,2D,2D,2D,dustColor);
        location.getWorld().playSound(location,Sound.BLOCK_GLASS_BREAK,10,0.62F);
    }

    private void startIceShardTrail(FallingBlock iceShard){
        if(iceShard == null) return;
        new BukkitRunnable(){

            @Override
            public void run() {
                if(iceShard.isDead()){
                    cancel();
                    return;
                }
                iceShard.getWorld().spawnParticle(Particle.SNOW_SHOVEL,iceShard.getLocation(),50,1D,1D,1D);
            }
        }.runTaskTimer(plugin,0,2L);
    }

    private void summonIceShard(Location location){
        if(location.getWorld() == null) return;
        location.add(0,15,0);
        location.getWorld().spawnParticle(Particle.REDSTONE,location,100,3D,1D,3D,new Particle.DustOptions(Color.fromBGR(255,255,200),3));
        location.getWorld().playSound(location,Sound.BLOCK_BEACON_POWER_SELECT,10,2F);
        BlockData data = Bukkit.createBlockData(Material.ICE);
        FallingBlock iceShard = location.getWorld().spawnFallingBlock(location, data);
        iceShard.setMetadata("IceShardRain",entityMetadataValue);
        startIceShardTrail(iceShard);
        iceShardsList.add(iceShard);
    }

    private void explodeIceShard(FallingBlock iceShard){
        iceShard.remove();
        explodeEffect(iceShard.getLocation());
        for(Player target : Utils.getAllClosePlayers(iceShard,2,2,2)){
            target.damage(5);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,60,3,true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,60,2,true));
        }
    }

    private void startIceShardImpactDetector(){
        if(detector == null || detector.isCancelled())
        detector = new BukkitRunnable(){
            FallingBlock selectedIceShard;
            Iterator<FallingBlock> it;
            @Override
            public void run() {
                if(iceShardsList.isEmpty() && activeIceShardRainTasks <= 0){
                    stopIceShardImpactDetector();
                    return;
                }
                it = iceShardsList.iterator();
                while (it.hasNext()){
                    selectedIceShard = it.next();
                    if(selectedIceShard.isDead()) it.remove();
                    for(Player player : Utils.getAllClosePlayers(selectedIceShard,5,5,5))
                        if(selectedIceShard.getBoundingBox().overlaps(player.getBoundingBox())){
                            explodeIceShard(selectedIceShard);
                            break;
                        }
                }
            }
        }.runTaskTimer(plugin,0,1L);
    }

    private void stopIceShardImpactDetector(){
        if(detector != null && !detector.isCancelled()){
            detector.cancel();
            detector = null;
        }
    }

    @Override
    public void activate() {
        Player target = Utils.getRandomClosePlayer(dragon.getDragon(),128,128,128);
        if(target == null) return;
        activeIceShardRainTasks++;
        startIceShardImpactDetector();
        new BukkitRunnable(){
            final Random r = new Random();
            int quantity = 0;
            @Override
            public void run() {
                if(dragon == null || dragon.getDragon() == null || dragon.getDragon().isDead() || !target.getWorld().equals(dragon.getDragon().getWorld())){
                    cancel();
                    activeIceShardRainTasks--;
                    return;
                }
                quantity++;
                if(quantity == 5){
                    cancel();
                    activeIceShardRainTasks--;
                    startCooldown();
                }
                summonIceShard(target.getLocation().clone().add(r.nextInt(3) - 1,0,r.nextInt(3) - 1));
            }
        }.runTaskTimer(plugin,0,15L);
    }

    @Override
    public boolean canBeActivated() {
        return !isInCooldownOrActive();
    }
}


