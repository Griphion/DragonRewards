package me.griphion.DragonRewards.DragonPackage;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.griphion.DragonRewards.ConfigFiles.WorldsConfig;
import me.griphion.DragonRewards.Core;
import me.griphion.DragonRewards.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public class DragonScheduler {

    private final Core plugin;
    private final static DateTimeFormatter respawnDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public DragonScheduler(Core plugin){
        this.plugin = plugin;
    }

    public void scheduleDragon(final World world){
        if(Core.worldsConfig.noEsUnMundoEndValido(world)){
            if(world != null){
                Bukkit.getLogger().log(Level.SEVERE, "Se quiso programar una invocacion para el mundo '" + world.getName() + "', pero ese mundo no existe o no está disponible!");
            }else {
                Bukkit.getLogger().log(Level.SEVERE, "Se quiso programar una invocacion para un mundo pero el mundo recibido es NULL!");
            }
            return;
        }
        if(Core.worldsConfig.noEstaActivoEnEseMundo(world)){
            Bukkit.getLogger().log(Level.INFO, "Se quiso programar una invocacion para el mundo '" + world.getName() + "', pero el mundo está desactivado!");
            return;
        }
        if(!Core.worldsConfig.getConfig().contains(world.getName())){
            Bukkit.getLogger().log(Level.SEVERE, "Se quiso programar una invocacion para el mundo '" + world.getName() + "', pero no se encontró ese mundo en worlds.yml!");
            return;
        }

        long ticks;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fechaDeRespawn;

        if(Core.worldsConfig.getConfig().contains(world.getName() + WorldsConfig.RESPAWN_DATE)){
            try{
                fechaDeRespawn = LocalDateTime.parse(Objects.requireNonNull(Core.worldsConfig.getRespawnDate(world.getName())),respawnDateFormat);
            }catch (Exception e){
                Bukkit.getLogger().log(Level.SEVERE,"No se pudo obtener la fecha de respawn! (Avisar a Griphion) mundo: " + world.getName(),e);
                return;
            }
            if(fechaDeRespawn.isBefore(now)){
                ticks = 2;
            }else{
                ticks = ChronoUnit.SECONDS.between(now, fechaDeRespawn)*20L;
            }


        }else if(Core.worldsConfig.getConfig().contains(world.getName() + WorldsConfig.DEATH_RESPAWN_DELAY)){
            ticks = Utils.formatToTicks(Core.worldsConfig.getDeathRespawnDelay(world.getName()));
            fechaDeRespawn = now.plusSeconds(ticks/20);
            Core.worldsConfig.setRespawnDate(world.getName(),formatDateToString(fechaDeRespawn));

        }else {
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo encontrar el '"+WorldsConfig.DEATH_RESPAWN_DELAY+"' ni el '"+WorldsConfig.RESPAWN_DATE+"' (Revisar config de worlds) mundo: " + world.getName());
            return;
        }

        if(Core.useHolographicDisplays)
            if(world.getEnderDragonBattle() != null)
                if(world.getEnderDragonBattle().getEndPortalLocation() != null)
                    crearHolograma(world.getEnderDragonBattle().getEndPortalLocation(),Core.worldsConfig.getHolograma(world.getName()));

        scheduleRun(world,ticks);
    }


    public static String formatDateToString(LocalDateTime fecha){
        return respawnDateFormat.format(fecha);
    }

    private void scheduleRun(final World world, final long ticks){
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
        {
            if(world == null){
                Bukkit.getLogger().log(Level.SEVERE, "Se quiso invocar un dragón en un mundo pero este mundo ya no existe (NULL)!");
                return;
            }
            Core.worldsConfig.setRespawnDate(world.getName(),null);
            if(Core.useHolographicDisplays)
            HologramManager.eliminarHolograma(world);
            if(Core.worldsConfig.noEstaActivoEnEseMundo(world)){
                Bukkit.getLogger().log(Level.INFO, "Se quiso invocar un dragón en el mundo '" + world.getName() + "', pero está desactivado!");
                return;
            }
            if(plugin.elMundoTieneUnDragon(world) && Core.dragonesActivos.get(world).getDragon().isDead()){
                Core.dragonesActivos.get(world).destroy(true);
            }

            if(!Core.worldsConfig.getConfig().contains(world.getName() + WorldsConfig.DRAGONS)){
                Bukkit.getLogger().log(Level.SEVERE, "No se pudo invocar a un dragón en el mundo '"+ world.getName() +"' porque no hay dragones configurados!");
                return;
            }
            List<String> dragones = Core.worldsConfig.getDragons(world.getName());
            if(dragones.isEmpty()){
                Bukkit.getLogger().log(Level.SEVERE, "No se pudo invocar a un dragón en el mundo '"+ world.getName() +"' porque no hay dragones configurados!");
                return;
            }
            String chosenDragon = getChosenDragon(dragones);
            if(chosenDragon == null){
                Bukkit.getLogger().log(Level.SEVERE, "No se pudo invocar a un dragón en el mundo '"+ world.getName() +"' porque los dragones configurados no son válidos!");
                return;
            }

            Dragon dragon = new Dragon(chosenDragon,world.getName(),plugin,true);
            dragon.spawn();

        }, ticks);
    }

    private String getChosenDragon(final List<String> dragones){
        String chosenDragon;

        if(dragones.size() == 1){
            chosenDragon = dragones.get(0);
        }else {
            Random random = new Random();
            chosenDragon = dragones.get(random.nextInt(dragones.size()));
        }
        if(Core.noEsUnDragonFileNameValido(chosenDragon)){
            dragones.remove(chosenDragon);
            if(dragones.isEmpty()){
                return null;
            }
            getChosenDragon(dragones);
        }
        return chosenDragon;
    }

    public void searchScheduledDragons(){
        for(String worldName: Core.worldsConfig.mundosEndNames){
            if(Core.worldsConfig.getConfig().contains(worldName + WorldsConfig.RESPAWN_DATE)){
                scheduleDragon(Bukkit.getWorld(worldName));
            }
        }
    }

    public void crearHolograma(final Location donde, final List<String> contenido){
        donde.add(0.5,5,0.5);
        Hologram holograma = HologramsAPI.createHologram(plugin,donde);
        for(String linea : contenido){
            if(Core.worldsConfig.getConfig().contains(Objects.requireNonNull(donde.getWorld()).getName() + WorldsConfig.RESPAWN_DATE) && linea.contains("{fechaRespawn}"))
                linea = linea.replace("{fechaRespawn}", Objects.requireNonNull(Core.worldsConfig.getRespawnDate(donde.getWorld().getName())));
            linea = ChatColor.translateAlternateColorCodes('&',linea);
            holograma.appendTextLine(linea);
        }
        HologramManager.agregarHolograma(donde.getWorld(), holograma);
    }
}
