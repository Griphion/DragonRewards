package me.griphion.DragonRewards.DragonPackage;

import me.griphion.DragonRewards.ConfigFiles.DragonsConfig;
import me.griphion.DragonRewards.Core;
import me.griphion.DragonRewards.LangManager;
import me.griphion.DragonRewards.Utils.CustomLogger;
import me.griphion.DragonRewards.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.DragonBattle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public class Dragon implements Listener {

    private EnderDragon dragon;
    private final String dragonFileName;
    private String name;
    private String recompensa_al_ultimo_golpe;
    private String recompensa_asistencia;
    private List<String > on_death_commands;
    private List<String> on_appear_commands;
    private Double vida,danio,armadura;
    private DragonsConfig dragonConfig;
    private final Core plugin;
    private World world;
    private DragonBattle dragonBattle;
    private final boolean newDragon;
    private UUID uuid;
    private List<Player> asistencias;
    private Player ultimoGolpe;
    private int anuncioMuerte, anuncioInvocacion;
    private Location centro;
    private String bossBarColor,bossBarStyle;
    private boolean ignoreUltimoGolpe = false;
    private boolean protectEnderCrystals;
    private DragonHealTimer timer = null;
    private List<DragonActivePower> dragonActivePowers;
    private List<DragonPassivePower> dragonPassivePowers;
    private HashMap<Player,Double> damageCounter;
    private boolean activePowersAreInCooldown;
    private int cooldown;
    private BukkitTask activePowerAutoCaster;

    public void destroy(final boolean andRemove){
        destroyDragonPowers();
        HandlerList.unregisterAll(this);
        stopHealTimer();
        if(andRemove)
            Core.dragonesActivos.remove(world);
        dragon = null;
        dragonConfig = null;
        damageCounter.clear();
        damageCounter = null;
        world = null;
        dragonBattle = null;
        if(asistencias != null)
        asistencias.clear();
        asistencias = null;
        ultimoGolpe = null;
        uuid = null;
        centro = null;
    }

    public Dragon(String dragonFileName,String worldName,Core core, boolean isNewDragon){
        plugin = core;
        newDragon = isNewDragon;
        this.dragonFileName = dragonFileName;
        this.world = Bukkit.getWorld(worldName);
        if(world == null){
            Bukkit.getLogger().log(Level.SEVERE, "Se quiso cargar al dragon '" + dragonFileName + "' pero el mundo '" + worldName + "' no existe!");
            return;
        }
        if(Core.noEsUnDragonFileNameValido(dragonFileName)){
            Bukkit.getLogger().log(Level.SEVERE, "Se quiso cargar al dragon '" + dragonFileName + "' pero este no existe o no esta disponible!");
            return;
        }

        loadDragonProperties();

        asistencias = new ArrayList<>();
        dragonActivePowers = new ArrayList<>();
        dragonPassivePowers = new ArrayList<>();
        damageCounter = new HashMap<>();
        activePowersAreInCooldown = false;

        Core.dragonesActivos.put(world,this);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        if(!newDragon){
            loadExistingDragon();
        }
    }

    public void reloadDragonProperties(){ //Solo para cuando el dragón yá esta vivo y el plugin cargado (La instancia del dragón ya existe)
        loadDragonProperties();
        loadDragonAttributesAndData();
        loadDragonPowers();
        protectEnderCrystals = false;
    }

    private void loadDragonAttributesAndData(){
        dragon.setCustomName(name);
        try{
            Objects.requireNonNull(dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(vida);
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo cambiarle la vida al dragon: '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "'");
        }
        try{
            Objects.requireNonNull(dragon.getAttribute(Attribute.GENERIC_ARMOR)).setBaseValue(armadura);
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo cambiarle la armadura al dragon: '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "'");
        }

        uuid = dragon.getUniqueId();
        dragon.setHealth(vida);

        if(dragon.getBossBar() != null){
            try {
                dragon.getBossBar().setColor(BarColor.valueOf(bossBarColor));
            }catch (IllegalArgumentException e){
                Bukkit.getLogger().log(Level.SEVERE, "No se ha podido establecer el COLOR de la BossBar! (Probablemente este mal escrito en la config del dragón) dragon: '" + ChatColor.stripColor(name) + "'");
            }

            try {
                dragon.getBossBar().setStyle(BarStyle.valueOf(bossBarStyle));
            }catch (IllegalArgumentException e){
                Bukkit.getLogger().log(Level.SEVERE, "No se ha podido establecer el STYLE de la BossBar! (Probablemente este mal escrito en la config del dragón) dragon: '" + ChatColor.stripColor(name) + "'");
            }
        }else {
            Bukkit.getLogger().log(Level.SEVERE, "No se ha podido establecer el estilo y color de la BossBar! dragon: '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "'");
        }

    }

    private void loadDragonProperties(){
        if(dragonConfig == null){
            dragonConfig = new DragonsConfig(dragonFileName);
        }else{
            dragonConfig.reloadConfig();
        }

        protectEnderCrystals = plugin.getConfig().getBoolean("proteger-end-crystals",true);
        name = dragonConfig.getNombreDragon();
        if(name != null){
            name = LangManager.agregarTodosLosColores(name);
        }else {
            name = dragonFileName;
        }
        recompensa_al_ultimo_golpe = dragonConfig.getRecompensaUltimoGolpe();
        recompensa_asistencia = dragonConfig.getRecompensaAsistencia();
        vida = dragonConfig.getVidaMaxima();
        if(vida > 2048D){
            vida = 2048D;
            Bukkit.getLogger().log(Level.WARNING, "La vida máxima posible del dragon es 2048! dragon: '" + ChatColor.stripColor(name) + "'");
        }
        danio = dragonConfig.getDANIO();
        armadura = dragonConfig.getARMADURA();
        anuncioMuerte = Core.worldsConfig.getMostrarAnuncioMuerte(world.getName());
        anuncioInvocacion = Core.worldsConfig.getMostrarAnuncioInvocacion(world.getName());
        bossBarColor = dragonConfig.getBossBarColor();
        bossBarStyle = dragonConfig.getBossBarStyle();
        on_appear_commands = dragonConfig.getOnAppearCommand();
        on_death_commands = dragonConfig.getOnDeathCommand();
        cooldown = dragonConfig.getDragonActivePowerCD();
    }

    private void loadExistingDragon(){ //Para cuando se carga el plugin y se quiere cargar el dragón como un "nuevo" dragón
        dragonBattle = world.getEnderDragonBattle();
        if (dragonBattle == null){
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo cargar el dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque no se pudo obtener DragonBattle (Avisar a Griphion).");
            destroy(true);
            return;
        }
        dragon = dragonBattle.getEnderDragon();
        if(dragon == null){
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo cargar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque no se pudo obtener al EnderDragon (Avisar a Griphion).");
            destroy(true);
            return;
        }
        centro = dragonBattle.getEndPortalLocation();
        loadDragonAttributesAndData();
        loadDragonPowers();
        protectEnderCrystals = false;
    }



    @EventHandler
    void onDragonSummon(CreatureSpawnEvent event){
        if(!newDragon) return;
        if(event.getEntityType().equals(EntityType.ENDER_DRAGON) && Objects.equals(event.getLocation().getWorld(), world)){

            dragonBattle = world.getEnderDragonBattle();
            dragon = (EnderDragon) event.getEntity();
            protectEnderCrystals = false;
            loadDragonAttributesAndData();
            loadDragonPowers();

            if(!on_appear_commands.isEmpty())
                for(String cmd: on_appear_commands){
                    executeDragonCommand(cmd);
                }

            enviarMensaje(Core.langManager.getDragonTranslationWithDragon(DragonsConfig.DRAGON_APPEAR,name,dragonFileName,world.getName()),anuncioInvocacion);
            CustomLogger.log( "[Dragón - Spawn] -Dragón: " + dragonFileName + " -Mundo: " + world.getName());
        }
    }

    @EventHandler
    void onDragonAttack(EntityDamageByEntityEvent event){

        if(event.getDamager().getType().equals(EntityType.ENDER_DRAGON) && event.getDamager().getUniqueId().equals(uuid) && Objects.equals(event.getEntity().getLocation().getWorld(), world)){
            if(event.getEntity().hasMetadata(DragonActivePower.entityMetadataName)){
                event.setCancelled(true);
                return;
            }
            event.setDamage(danio);
        }
    }

    @EventHandler (ignoreCancelled = true)
    void onDragonGetHit(EntityDamageByEntityEvent event){
        if(!Objects.equals(event.getEntity().getLocation().getWorld(), world)) return;
        if(event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL) && protectEnderCrystals){
            event.setCancelled(true);
        }
        if(event.getEntity().getType().equals(EntityType.ENDER_DRAGON) && event.getEntity().getUniqueId().equals(uuid)){
            restartHealTimer();
            Player damager = null;
            if(event.getDamager().getType().equals(EntityType.PLAYER)){
                damager = (Player) event.getDamager();
            }

            if(event.getDamager().getType().equals(EntityType.ARROW)){
                Arrow arrow = (Arrow) event.getDamager();
                damager = (Player) arrow.getShooter();
            }

            activateRandomDragonActivePower();

            if(damager == null) return;
            if(damageCounter.containsKey(damager)){
                damageCounter.put(damager,damageCounter.get(damager) + event.getDamage());
            }else{
                damageCounter.put(damager,event.getDamage());
            }
            if(!asistencias.contains(damager)){
                asistencias.add(damager);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    void enderCrystalDamageByBlock(ExplosionPrimeEvent event){
        if(!Objects.equals(event.getEntity().getLocation().getWorld(), world)) return;
        if(event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL) && protectEnderCrystals){
            event.setCancelled(true);
            return;
        }
        if(!protectEnderCrystals && dragonBattle.getEnderDragon() == null){
            destroy(true);
        }

    }
    @EventHandler
    void onDragonDeath(EntityDeathEvent event){
        if(!Objects.equals(event.getEntity().getLocation().getWorld(), world)) return;
        if(event.getEntityType().equals(EntityType.ENDER_DRAGON) && event.getEntity().getUniqueId().equals(uuid)){
            if(ignoreUltimoGolpe){
                enviarMensaje(Core.langManager.getTranslationWithDragon("dragon-admin-kill",name, Core.worldsConfig.getAlias(world.getName())),anuncioMuerte);
                destroy(true);
                return;
            }
            ultimoGolpe = event.getEntity().getKiller();

            if(ultimoGolpe == null && asistencias.size() > 1){
                Bukkit.getLogger().log(Level.INFO, "No se ha podido encontrar al que hizo el último golpe! dragon: '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "'");
                enviarMensaje(Core.langManager.getTranslationWithDragon("dragon-kill-lasthit-not-found",name,"",Utils.devolverSeparadoConComas(asistencias), world.getName()),anuncioMuerte);
                giveAssistLoot();
                destroy(true);
                return;
            } else if (ultimoGolpe == null && asistencias.size() == 1){ //
                enviarMensaje(Core.langManager.getTranslationWithDragon("dragon-kill-not-conventional",name,asistencias.get(0).getDisplayName(),Utils.devolverSeparadoConComas(asistencias), world.getName()),anuncioMuerte);
                giveAssistLoot();
                destroy(true);
                return;
            } else if (ultimoGolpe == null){
                Bukkit.getLogger().log(Level.INFO, "No se ha podido encontrar al que hizo el último golpe! dragon: '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "'");
                enviarMensaje(Core.langManager.getTranslationWithDragon("dragon-kill-unknown-death",name, world.getName()),anuncioMuerte);
                destroy(true);
                return;
            }

            asistencias.remove(ultimoGolpe);
            if(!asistencias.isEmpty()){
                enviarMensaje(Core.langManager.getDragonTranslationWithDragon(DragonsConfig.DRAGON_KILL_MESSAGE,name,dragonFileName,ultimoGolpe.getDisplayName(),Utils.devolverSeparadoConComas(asistencias), world.getName()),anuncioMuerte);
                giveAssistLoot();
                CustomLogger.log( "[Dragón - Muerte] -Dragón: " + dragonFileName + " -Mundo: " + world.getName() + " -Ultimo golpe: " + ultimoGolpe.getName() + " (" + ultimoGolpe.getUniqueId() + ")" + " -Asist: " + Utils.devolverSeparadoConComasConUUID(asistencias));

            }else {
                enviarMensaje(Core.langManager.getDragonTranslationWithDragon(DragonsConfig.DRAGON_KILL_MESSAGE_SOLO,name,dragonFileName,ultimoGolpe.getDisplayName(),"", world.getName()),anuncioMuerte);
                CustomLogger.log( "[Dragón - Muerte] -Dragón: " + dragonFileName + " -Mundo: " + world.getName() + " -Ultimo golpe [Solo]: " + ultimoGolpe.getName() + " (" + ultimoGolpe.getUniqueId() +")");
            }

            giveLastHitLoot();
            if(!on_death_commands.isEmpty()) {
                for (String cmd : on_death_commands) {
                    executeDragonCommand(cmd);
                }
            }

            destroy(true);

        }
    }

    @EventHandler
    void onPlayerGetClose(PlayerMoveEvent event){
        if(dragon == null) return;
        if(Utils.isCloseTo(event.getPlayer().getLocation(),dragon.getLocation(),3)){
            empujar(event.getPlayer(),dragon.getLocation());
        }
    }



    public void empujar(Player player, Location posicion){
        Location posicionJugador = player.getLocation();
        Vector direccion = posicionJugador.subtract(posicion).toVector();
        player.setVelocity(direccion.multiply(0.2));
    }

    public void curarYReiniciarAsistencias(){
        if(dragon != null)
        dragon.setHealth(vida);
        if(asistencias != null)
        asistencias.clear();
        damageCounter.clear();
    }

    private void executeDragonCommand(String command){
        if(command == null) return;
        if(command.isEmpty()) return;
        if(command.startsWith("[consola]")){
            command = command.replace("[consola]", "");
            formatDragonCommandAndExecute(Bukkit.getConsoleSender(),command);

        }else if (command.startsWith("[ug]")){
            command = command.replace("[ug]", "");
            if(ultimoGolpe == null) return;
            formatDragonCommandAndExecute(ultimoGolpe,command);

        }else if (command.startsWith("[assist]")){
            command = command.replace("[assist]", "");
            if(asistencias == null) return;
            if(asistencias.isEmpty()) return;
            for(Player player: asistencias){
                if(!player.isOnline()) continue;
                formatDragonCommandAndExecute(player,command);
            }

        }else if (command.startsWith("[todos]")){
            command = command.replace("[todos]", "");
            List<Player> todos = asistencias;
            asistencias.add(ultimoGolpe);
            if(todos.isEmpty()) return;
            for(Player player: todos){
                if(!player.isOnline()) continue;
                formatDragonCommandAndExecute(player,command);
            }

        }else {
            Bukkit.getLogger().log(Level.SEVERE, "Comando para el dragón '" + ChatColor.stripColor(name) + "' mal escrito, comando: " + command);
        }
    }

    private void formatDragonCommandAndExecute(final CommandSender sender, String command){
        command = command.replace("{mundo}", world.getName());
        if(command.contains("{ug}")) {
            if (ultimoGolpe == null) return;
            command = command.replace("{ug}", ultimoGolpe.getName());
        }

        if(command.contains("{assist}") && !command.contains("{todos}")){
            if(asistencias == null) return;
            if(asistencias.isEmpty()) return;

            for(Player player : asistencias){
                if(player == sender) continue;
                if(!player.isOnline()) continue;
                Bukkit.dispatchCommand(sender,command.replace("{assist}",player.getName()));
            }
            return;
        }

        if(command.contains("{todos}") && !command.contains("{assist}")){
            if(sender != ultimoGolpe)
                Bukkit.dispatchCommand(sender,command.replace("{todos}",ultimoGolpe.getName()));
            if(asistencias == null) return;
            if(asistencias.isEmpty()) return;
            for(Player player : asistencias){
                if(player == sender) continue;
                if(!player.isOnline()) continue;
                Bukkit.dispatchCommand(sender,command.replace("{todos}",player.getName()));
            }
            return;
        }

        if(command.contains("{assist}") && command.contains("{todos}")){
            List<Player> todos = asistencias;
            todos.add(ultimoGolpe);
            for (Player player: asistencias){
                if(player == sender) continue;
                if(!player.isOnline()) continue;
                for (Player player1: todos){
                    if(player1 == sender) continue;
                    if(!player1.isOnline()) continue;
                    Bukkit.dispatchCommand(sender,command.replace("{todos}",player1.getName()).replace("{assist}",player.getName()));
                }
            }
            return;
        }

        Bukkit.dispatchCommand(sender,command);
    }


    private void giveAssistLoot(){
        if(asistencias == null) return;
        if(asistencias.isEmpty()) return;
        for (Player elem : asistencias){
            if(elem == null) continue;
            if(plugin.getConfig().getBoolean("restringir-recompensa-online",true) && !elem.isOnline() && (damageCounter.get(elem) < 0.15*vida )) continue;
            if(plugin.getConfig().getBoolean("restringir-recompensa-mundo",true) && !elem.getWorld().equals(world) && (damageCounter.get(elem) < 0.15*vida )) continue;
            plugin.playerDataConfig.addAssist(elem.getUniqueId());
            plugin.playerDataConfig.agregarLootAJugador(recompensa_asistencia,elem,true);
        }
    }

    private void giveLastHitLoot(){
        if(ultimoGolpe == null) return;
        plugin.playerDataConfig.addUltimoGolpe(ultimoGolpe.getUniqueId());
        plugin.playerDataConfig.agregarLootAJugador(recompensa_al_ultimo_golpe,ultimoGolpe,true);
    }

    public void enviarMensaje(final String[] mensaje, final int prioridad){
        if(mensaje == null) return;
        if(mensaje.length == 0) return;
        switch (prioridad){
            case 0:
                return;
            case -2:
                for(String msg : mensaje){
                    Bukkit.getServer().broadcastMessage(msg);
                }
                break;
            case -1:
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(p.getWorld().equals(world)){
                        p.sendMessage(mensaje);
                    }
                }
                break;
            default:
                if(prioridad <= 0) return;
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(p != null)
                    if(p.getWorld().equals(world)){
                        if(isCloseToCenter(p.getLocation(),prioridad))
                        p.sendMessage(mensaje);
                    }
                }
                break;
        }
    }

    private boolean isCloseToCenter(final Location from, final int distanciaMax){
        if(world == null || centro == null || from == null) return false;
        return (Math.sqrt(NumberConversions.square(this.centro.getX() - from.getX()) + NumberConversions.square(this.centro.getZ() - from.getZ())) < distanciaMax);
    }


    public boolean spawn(){

        if(Core.worldsConfig.noEstaActivoEnEseMundo(world)){
            Bukkit.getLogger().log(Level.SEVERE, "No se logró invocar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque el mundo está desactivado.");
            destroy(true);
            return false;
        }

        dragonBattle = world.getEnderDragonBattle();

        if (dragonBattle == null){
            Bukkit.getLogger().log(Level.SEVERE, "No se logró invocar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque no se pudo obtener DragonBattle (Avisar a Griphion).");
            destroy(true);
            return false;
        }

        if(dragonBattle.getEnderDragon() != null){
            if(!dragonBattle.getEnderDragon().isDead()){
                Bukkit.getLogger().log(Level.SEVERE, "No se logró invocar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque ya hay uno vivo en este mundo.");
                destroy(true);
                return false;
            }

            if(dragonBattle.getEnderDragon().getPhase().equals(EnderDragon.Phase.DYING)){
                Bukkit.getLogger().log(Level.SEVERE, "No se logró invocar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque ya hay uno (Muriendo) en este mundo.");
                destroy(true);
                return false;
            }
        }

        if(!dragonBattle.getRespawnPhase().equals(DragonBattle.RespawnPhase.NONE)){
            Bukkit.getLogger().log(Level.SEVERE, "No se logró invocar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque ya hay uno invocándose en este mundo.");
            destroy(true);
            return false;
        }

        if(dragonBattle.getEndPortalLocation() == null){
            Bukkit.getLogger().log(Level.SEVERE, "No se logró invocar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque no se pudo obtener EndPortalLocation (Avisar a Griphion).");
            destroy(true);
            return false;
        }

        centro = dragonBattle.getEndPortalLocation();
        summonEndCrystals(world, centro, dragonBattle);
        return true;
    }

    private void summonEndCrystals(final World world, final Location portalLocation, final DragonBattle dragonBattle){
        double xPosAux = portalLocation.getX();
        double zPosAux = portalLocation.getZ();
        portalLocation.setY(portalLocation.getY() + 1);

        portalLocation.setX(xPosAux + 3.5);
        portalLocation.setZ(zPosAux + 0.5);
        if(!world.isChunkLoaded(world.getChunkAt(portalLocation)))
            world.loadChunk(world.getChunkAt(portalLocation));
        enderCrystalParticleEffect(portalLocation.clone(),20L);
        spawnEnderCrystalEffect(world,portalLocation.clone(),20L);

        portalLocation.setX(xPosAux - 2.5);
        if(!world.isChunkLoaded(world.getChunkAt(portalLocation)))
            world.loadChunk(world.getChunkAt(portalLocation));
        enderCrystalParticleEffect(portalLocation.clone(),40L);
        spawnEnderCrystalEffect(world,portalLocation.clone(),40L);

        portalLocation.setX(xPosAux + 0.5);
        portalLocation.setZ(zPosAux + 3.5);
        if(!world.isChunkLoaded(world.getChunkAt(portalLocation)))
            world.loadChunk(world.getChunkAt(portalLocation));
        enderCrystalParticleEffect(portalLocation.clone(),60L);
        spawnEnderCrystalEffect(world,portalLocation.clone(),60L);

        portalLocation.setZ(zPosAux - 2.5);
        if(!world.isChunkLoaded(world.getChunkAt(portalLocation)))
            world.loadChunk(world.getChunkAt(portalLocation));
        enderCrystalParticleEffect(portalLocation.clone(),80L);
        spawnEnderCrystalEffect(world,portalLocation,80L);

        plugin.getServer().getScheduler().runTaskLater(plugin, dragonBattle::initiateRespawn, 90L);
        plugin.getServer().getScheduler().runTaskLater(plugin,() -> {
            if(dragonBattle.getRespawnPhase().equals(DragonBattle.RespawnPhase.NONE)){
                Bukkit.getLogger().log(Level.SEVERE, "No se logró invocar al dragon '" + ChatColor.stripColor(name) + "' en el mundo: '" + world.getName() + "' porque no se pudo spawnear los end crystal (Avisar a Griphion).");
                destroy(true);
            }
        } ,130L);
    }

    private void spawnEnderCrystalEffect(final World world, final Location enderCrystalLocation, long ticks){
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if(plugin.getConfig().getBoolean("rayo-al-invocar",true))
                    world.strikeLightningEffect(enderCrystalLocation);
                    ((EnderCrystal) world.spawnEntity(enderCrystalLocation, EntityType.ENDER_CRYSTAL)).setShowingBottom(false);
                }, ticks);
    }

    private void enderCrystalParticleEffect(final Location location, final long espera){
        if(!plugin.getConfig().getBoolean("particulas-al-invocar",true)) return;
        if(location.getWorld() == null) return;
        location.add(0,1,0);
        new BukkitRunnable(){
            double t = 0;
            final double r = 2;
            double y = -1;
            @Override
            public void run() {
                while (t < Math.PI*4){
                    t = t + Math.PI/8;
                    double x = r*Math.cos(t);
                    double z = r*Math.sin(t);
                    location.add(x,y,z);
                    location.getWorld().spawnParticle(Particle.SPELL_WITCH,location,3);
                    location.subtract(x,y,z);
                }
                t = 0;
                y+=0.5;
                if(y == 2.5){
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin,espera,1);
    }

    private void startActivePowersAutoCastTimer(){
        if(activePowerAutoCaster == null)
        activePowerAutoCaster = new BukkitRunnable(){

            @Override
            public void run() {
                if(dragon == null || dragon.isDead()){
                    cancel();
                    return;
                }
                if(Utils.getRandomClosePlayer(dragon,128,128,128) == null) return;
                activateRandomDragonActivePower();
            }
        }.runTaskTimer(plugin,0,dragonConfig.getDragonActivePowerAutoCastTime() * 20L);
    }

    public void startActivePowersCooldown(){
        if(cooldown <= 0) return;
        activePowersAreInCooldown = true;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> activePowersAreInCooldown = false, cooldown * 20L);
    }

    private void activateRandomDragonActivePower(){
        if(dragonActivePowers == null) return;
        if(activePowersAreInCooldown) return;

        List<DragonActivePower> notInCooldownPowers = new ArrayList<>();
        for(DragonActivePower activePower : dragonActivePowers){
            if(!activePower.canBeActivated()) continue;
            notInCooldownPowers.add(activePower);
        }
        if(!notInCooldownPowers.isEmpty()){
            notInCooldownPowers.get((new SplittableRandom()).nextInt(notInCooldownPowers.size())).activate();
            startActivePowersCooldown();
        }
    }

    private void loadDragonPowers(){
        dragonActivePowers = new ArrayList<>();
        dragonPassivePowers = new ArrayList<>();
        activePowersAreInCooldown = false;
        if(dragon == null){
            Bukkit.getLogger().severe("Error al cargar los poderes de '" + name + "', no se pudo encontrar al dragón!");
            return;
        }
        int cooldown;
        DragonActivePower dragonActivePower;
        for(String active: dragonConfig.getDragonActivePowers()){
            if(active.length() == 0) continue;
            try{
                cooldown = Integer.parseInt(active.substring(active.indexOf(':') + 1));
            }catch (Exception e){
                cooldown = -1;
            }

            dragonActivePower = DragonActivePower.parseDragonActivePower(active.substring(0,active.indexOf(':')),plugin,this, cooldown);
            if(dragonActivePower != null && dragonActivePowers != null){
                dragonActivePowers.add(dragonActivePower);
            }
        }
        DragonPassivePower dragonPassivePower;
        for(String passive: dragonConfig.getDragonPassivePowers()){
            if(passive.length() == 0) continue;
            dragonPassivePower = DragonPassivePower.parseDragonPassivePower(passive,plugin,this);
            if(dragonPassivePower != null && dragonPassivePowers != null){
                dragonPassivePowers.add(dragonPassivePower);
            }
        }
        startActivePowersAutoCastTimer();
    }

    private void destroyDragonPowers(){
        if(activePowerAutoCaster != null){
            activePowerAutoCaster.cancel();
            activePowerAutoCaster = null;
        }
        if(dragonPassivePowers != null){
            for(DragonPassivePower dragonPassivePower : dragonPassivePowers){
                dragonPassivePower.destroy();
            }
            dragonPassivePowers.clear();
            dragonPassivePowers = null;
        }

        if(dragonActivePowers != null){
            for(DragonActivePower dragonActivePower : dragonActivePowers){
                dragonActivePower.destroy();
            }
            dragonActivePowers.clear();
            dragonActivePowers = null;
        }

    }


    public void kill(final CommandSender sender){
        if(dragon == null){
            sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "No se pudo matar a " + name + ChatColor.RED + " porque todavía no se invocó.");
            return;
        }
        if(!dragon.isDead()){
            asistencias.clear();
            ignoreUltimoGolpe = true;
            dragon.setHealth(0);
        }else {
            destroy(true);
            sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "El dragon "+ name + ChatColor.RED + " ya estaba muerto!");
        }
    }

    public String getDragonFileName(){
        return dragonFileName;
    }

    public String getName(){
        return name;
    }

    public UUID getUuid(){
        return uuid;
    }

    public EnderDragon getDragon(){
        return dragon;
    }

    public DragonBattle getDragonBattle() {
        return dragonBattle;
    }

    private void restartHealTimer(){
        if(!plugin.getConfig().getBoolean("regenerar-y-reiniciar",true)) return;
        if(timer == null){
            timer = new DragonHealTimer(plugin,this, (Utils.formatToTicks(plugin.getConfig().getString("regenerar-y-reiniciar-tiempo","30m"))/20) );
        }else {
            timer.reiniciar();
        }
    }

    public void stopHealTimer(){
        if(timer != null){
            timer.stop();
            timer = null;
        }
    }
}
