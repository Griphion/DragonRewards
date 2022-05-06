package me.griphion.DragonRewards.ConfigFiles;

import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorldsConfig {

    private final ConfigFileManager config;
    private final Core plugin;

    public List<String> mundosEndNames = new ArrayList<>();

    public static final String ACTIVO = ".activo";
    public static final String MOSTRAR_ANUNCIO_MUERTE = ".mostrar-anuncio-muerte";
    public static final String MOSTRAR_ANUNCIO_INVOCACION = ".mostrar-anuncio-invocacion";
    public static final String RESPAWN_ON_DEATH = ".respawn-al-morir";
    public static final String DEATH_RESPAWN_DELAY = ".demora-respawn";
    public static final String DRAGONS = ".dragones";
    public static final String RESPAWN_DATE = ".fecha-respawn";
    public static final String HOLOGRAMA = ".holograma";
    public static final String ALIAS = ".alias";

    public WorldsConfig(Core plugin) {
        this.plugin = plugin;
        config = new ConfigFileManager("Worlds", ConfigFileManager.directoryEnum.NONE);
    }

    public void destroy(){
        if(mundosEndNames != null){
            mundosEndNames.clear();
            mundosEndNames = null;
        }
    }

    public void scannerDeMundos(boolean isReload) {
        for (World world : plugin.getServer().getWorlds()) {
            //if(!todosLosMundos.contains(world.getName())) todosLosMundos.add(world.getName());
            if(world == null || mundosEndNames == null) continue;
            if (world.getEnvironment().equals(World.Environment.THE_END) && !mundosEndNames.contains(world.getName())) {
                if(isReload){
                    getConfig().set(world.getName() + ACTIVO, false);
                    getConfig().set(world.getName() + ALIAS, world.getName());
                    getConfig().set(world.getName() + MOSTRAR_ANUNCIO_MUERTE, -1);
                    getConfig().set(world.getName() + MOSTRAR_ANUNCIO_INVOCACION, -1);
                    getConfig().set(world.getName() + RESPAWN_ON_DEATH, true);
                    getConfig().set(world.getName() + DEATH_RESPAWN_DELAY, "5m");
                    getConfig().set(world.getName() + HOLOGRAMA, Arrays.asList("Fecha de respawn:", "{fechaRespawn}"));
                    getConfig().set(world.getName() + DRAGONS, Collections.singletonList("Smaug"));
                    saveConfig();
                }else {
                    getConfig().addDefault(world.getName() + ACTIVO, false);
                    getConfig().addDefault(world.getName() + ALIAS, world.getName());
                    getConfig().addDefault(world.getName() + MOSTRAR_ANUNCIO_MUERTE, -1);
                    getConfig().addDefault(world.getName() + MOSTRAR_ANUNCIO_INVOCACION, -1);
                    getConfig().addDefault(world.getName() + RESPAWN_ON_DEATH, true);
                    getConfig().addDefault(world.getName() + DEATH_RESPAWN_DELAY, "5m");
                    getConfig().addDefault(world.getName() + HOLOGRAMA, Arrays.asList("Fecha de respawn:", "{fechaRespawn}"));
                    getConfig().addDefault(world.getName() + DRAGONS, Collections.singletonList("Smaug"));
                }
                mundosEndNames.add(world.getName());
            }
        }
        if(!isReload) saveCopyDefaults();
    }

    /*
    public boolean noEstaActivoEnEseMundo(final String nombreMundo){
        try{
            return !worldsConfig.getConfig().getBoolean(nombreMundo + ".activo");
        }catch (Exception e){
            return true;
        }
    }
    */
    public boolean noEstaActivoEnEseMundo(final World world){
        if(world == null) return true;
        try{
            return !getActivo(world.getName());
        }catch (Exception e){
            return true;
        }
    }
    public boolean noEstaActivoEnEseMundo(final String nombreMundo, final CommandSender sender){
        if(sender == null) return true;
        if(nombreMundo == null){
            sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "Ese mundo no existe o no esta disponible.");
            return true;
        }
        boolean resultado;
        try{
            resultado = !getActivo(nombreMundo);
        }catch (Exception e){
            resultado = true;
        }
        if(resultado){
            sender.sendMessage(plugin.pluginPrefix + ChatColor.RED + "Ese mundo esta desactivado! Use 'dragonr editmundo toggle [Mundo]' para activarlo.");
        }
        return resultado;
    }

    public boolean noTieneQueRespawnearDragon(final World mundo){
        boolean resultado;
        try{
            resultado = !getRespawnOnDeath(mundo.getName());
        }catch (Exception e){
            resultado = true;
        }
        return resultado;
    }
    /*
    public boolean noEstaActivoEnEseMundo(final World world, final CommandSender sender){
        if(world == null || sender == null) return true;
        boolean resultado;
        try{
            resultado = !worldsConfig.getConfig().getBoolean(world.getName() + ".activo");
        }catch (Exception e){
            resultado = true;
        }
        if(resultado){
            sender.sendMessage(pluginPrefix + ChatColor.RED + "Ese mundo esta desactivado! Use 'dragonr editmundo toggle [Mundo]' para activarlo.");
        }
        return resultado;
    }
*/
    public boolean noEsUnMundoEndValido(World world, CommandSender sender){
        if(world == null){
            sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "Ese mundo no existe o no esta disponible.");
            return true;
        }
        if(mundosEndNames.contains(world.getName())) return false;
        sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "El mundo '" + ChatColor.GOLD + world.getName() + ChatColor.RED + "' no existe o no esta disponible.");
        return true;
    }
    public boolean noEsUnMundoEndValido(World world){
        if(world == null) return true;
        return !mundosEndNames.contains(world.getName());
    }

    public boolean noEsUnMundoEndValido(String worldName, CommandSender sender){
        if(worldName == null){
            sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "Ese mundo no existe o no esta disponible.");
            return true;
        }
        if(mundosEndNames.contains(worldName)) return false;
        sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "El mundo '" + ChatColor.GOLD + worldName + ChatColor.RED + "' no existe o no esta disponible.");
        return true;
    }

    public String getAlias(String mundo){
        return config.getConfig().getString(mundo + ALIAS, mundo);
    }
    public boolean getActivo(String mundo){
        return config.getConfig().getBoolean(mundo + ACTIVO, false);
    }
    public int getMostrarAnuncioMuerte(String mundo){
        return config.getConfig().getInt(mundo + MOSTRAR_ANUNCIO_MUERTE, 0);
    }
    public int getMostrarAnuncioInvocacion(String mundo){
        return config.getConfig().getInt(mundo + MOSTRAR_ANUNCIO_INVOCACION, 0);
    }
    public boolean getRespawnOnDeath(String mundo){
        return config.getConfig().getBoolean(mundo + RESPAWN_ON_DEATH, false);
    }
    public String getDeathRespawnDelay(String mundo){
        return config.getConfig().getString(mundo + DEATH_RESPAWN_DELAY, "1w");
    }
    public List<String> getDragons(String mundo){
        return config.getConfig().getStringList(mundo + DRAGONS);
    }
    public String getRespawnDate(String mundo){
        return config.getConfig().getString(mundo + RESPAWN_DATE, null);
    }
    public List<String> getHolograma(String mundo){
        return config.getConfig().getStringList(mundo + HOLOGRAMA);
    }

    
    public void setAlias(String mundo, String alias){
        config.getConfig().set(mundo + ALIAS, alias);
        saveConfig();
    }
    public void setActivo(String mundo, boolean activo){
        config.getConfig().set(mundo + ACTIVO, activo);
        saveConfig();
    }
    public void setMostrarAnuncioMuerte(String mundo, int valor){
        config.getConfig().set(mundo + MOSTRAR_ANUNCIO_MUERTE, valor);
        saveConfig();
    }
    public void setMostrarAnuncioInvocacion(String mundo, int valor){
        config.getConfig().set(mundo + MOSTRAR_ANUNCIO_INVOCACION, valor);
        saveConfig();
    }
    public void setRespawnOnDeath(String mundo, boolean respawnOnDeath){
        config.getConfig().set(mundo + RESPAWN_ON_DEATH, respawnOnDeath);
        saveConfig();
    }
    public void setDeathRespawnDelay(String mundo, String delay){
        config.getConfig().set(mundo + DEATH_RESPAWN_DELAY, delay);
        saveConfig();
    }
    public void setDragons(String mundo, List<String> dragones){
        config.getConfig().set(mundo + DRAGONS, dragones);
        saveConfig();
    }
    public void setRespawnDate(String mundo, String fecha){
        config.getConfig().set(mundo + RESPAWN_DATE, fecha);
        saveConfig();
    }
    public void setHolograma(String mundo, List<String> textoHolograma){
        config.getConfig().set(mundo + HOLOGRAMA, textoHolograma);
        saveConfig();
    }



    public void reloadConfig(){
        config.reloadConfig();
    }
    public void saveConfig(){
        config.saveConfig();
    }
    public void saveCopyDefaults(){
        config.saveCopyDefaults();
    }
    public FileConfiguration getConfig(){
        return config.getConfig();
    }
}
