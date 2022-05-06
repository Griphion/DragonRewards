package me.griphion.DragonRewards.ConfigFiles;

import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerDataConfig {
    private final ConfigFileManager config;
    static public final String LOOT = ".loot";
    static public final String ULTIMOS_GOLPES = ".ultimos-golpes";
    static public final String ASSISTS = ".assist";
    static public final String NAME = ".name";

    public PlayerDataConfig(){
        config = new ConfigFileManager("PlayerData", ConfigFileManager.directoryEnum.NONE);
    }

    public void reloadConfig(){
        config.reloadConfig();
    }
    public void saveConfig(){
        config.saveConfig();
    }
    public FileConfiguration getConfig(){
        return config.getConfig();
    }

    public void addUltimoGolpe(final UUID uuid){
        if(uuid == null) return;
        int actual = config.getConfig().getInt(uuid.toString() + PlayerDataConfig.ULTIMOS_GOLPES,0);
        actual++;
        config.getConfig().set(uuid.toString() + PlayerDataConfig.ULTIMOS_GOLPES,actual);
        config.saveConfig();
    }
    public void addAssist(final UUID uuid){
        if(uuid == null) return;
        int actual = config.getConfig().getInt(uuid.toString() + PlayerDataConfig.ASSISTS,0);
        actual++;
        config.getConfig().set(uuid.toString() + PlayerDataConfig.ASSISTS,actual);
        config.saveConfig();
    }

    private void addName(final Player player){
        if(player == null) return;
        if(config.getConfig().contains(player.getUniqueId() + PlayerDataConfig.NAME)) return;
        config.getConfig().set(player.getUniqueId() + PlayerDataConfig.NAME,player.getName());
        config.saveConfig();

    }

    public String getName(final UUID uuid){
        if(uuid == null) return null;
        if(!config.getConfig().contains(uuid.toString() + PlayerDataConfig.NAME)) return null;
        return config.getConfig().getString(uuid.toString() + PlayerDataConfig.NAME);
    }

    public UUID getUUID(final String name){
        if(name == null) return null;
        for (String elem: config.getConfig().getKeys(false)){
            if(elem != null)
            if(config.getConfig().contains(elem + ".name"))
            if(Objects.requireNonNull(config.getConfig().getString(elem + ".name")).equalsIgnoreCase(name)) return UUID.fromString(elem);
        }
        return null;
    }

    public int getUltimoGolpe(final UUID uuid){
        if(uuid == null) return 0;
        if(!config.getConfig().contains(uuid.toString() + PlayerDataConfig.ULTIMOS_GOLPES)) return 0;
        return config.getConfig().getInt(uuid.toString() + PlayerDataConfig.ULTIMOS_GOLPES);
    }
    public int getAssist(final UUID uuid){
        if(uuid == null) return 0;
        if(!config.getConfig().contains(uuid.toString() + PlayerDataConfig.ASSISTS)) return 0;
        return config.getConfig().getInt(uuid.toString() + PlayerDataConfig.ASSISTS);
    }


    private LinkedHashMap<String,Integer> ordenarDeMayorAMenor(HashMap<String, Integer> map){
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        ArrayList<Integer> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            list.add(entry.getValue());
        }
        list.sort(Collections.reverseOrder());
        for (int num : list) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(num)) {
                    sortedMap.put(entry.getKey(), num);
                }
            }
        }
        return sortedMap;
    }

    public void sendTop10Kills(CommandSender sender){
        HashMap<String,Integer> map = new HashMap<>();
        String name;
        int cantidad;
        for(String uuid: getConfig().getKeys(false)){
            name = getName(UUID.fromString(uuid));
            cantidad = getUltimoGolpe(UUID.fromString(uuid));
            if(name == null || cantidad == 0) continue;
            map.put(name,cantidad);
        }
        sendTop10(sender,map);
    }

    public void sendTop10Assist(CommandSender sender){
        HashMap<String,Integer> map = new HashMap<>();
        String name;
        int cantidad;
        for(String uuid: getConfig().getKeys(false)){
            name = getName(UUID.fromString(uuid));
            cantidad = getAssist(UUID.fromString(uuid));
            if(name == null || cantidad == 0) continue;
            map.put(name,cantidad);
        }
        sendTop10(sender,map);
    }

    private void sendTop10(CommandSender sender,HashMap<String,Integer> map){
        map = ordenarDeMayorAMenor(map);
        int i = 0;
        for (String nombre: map.keySet()){
            if(i == 10) return;
            sender.sendMessage( ChatColor.DARK_PURPLE + "<-" + ChatColor.LIGHT_PURPLE + (i+1) + ChatColor.DARK_PURPLE + "-> " + ChatColor.WHITE + nombre + ChatColor.DARK_PURPLE + " >> " + ChatColor.GOLD + map.get(nombre));
            i++;
        }
    }


    public void agregarLootAJugador(final String lootTable, final Player player, final boolean sendMessage){
        if(lootTable == null) return;
        addName(player);
        List<String> list = obtenerListaDeLoot(player.getUniqueId());
        list.add(lootTable);
        config.getConfig().set(player.getUniqueId() + PlayerDataConfig.LOOT,list);
        saveConfig();
        if(sendMessage)
        player.sendMessage(Core.langManager.getConfigTranslation("lootTable-reclamar"));
    }

    public String obtenerUnaLootTableYSacarla(final UUID uuid){
        List<String> list;
        if(tieneLootDisponible(uuid)){
            list = obtenerListaDeLoot(uuid);
            String elegida = list.get(0);
            list.remove(elegida);
            if(list.isEmpty()){
                config.getConfig().set(uuid.toString() + PlayerDataConfig.LOOT,null);
                saveConfig();
                return elegida;
            }
            config.getConfig().set(uuid.toString() + PlayerDataConfig.LOOT,list);
            saveConfig();
            return elegida;
        }
        return null;
    }
    public void sacarTodoElLoot(final UUID uuid){
        config.getConfig().set(uuid.toString() + PlayerDataConfig.LOOT, null);
        saveConfig();
    }
    public List<String> obtenerListaDeLoot(final UUID uuid){
        if(tieneLootDisponible(uuid))
        return new ArrayList<>(config.getConfig().getStringList(uuid.toString() + PlayerDataConfig.LOOT));
        return new ArrayList<>();
    }
    public boolean tieneLootDisponible(final UUID uuid){
        if(uuid == null) return false;
        return config.getConfig().contains(uuid.toString() + PlayerDataConfig.LOOT);
    }

    /*
    public void sacarLootEspecificoAJugador(String lootTable, Player player){
        List<String> list;
        if(tieneLootDisponible(player)){
            list = obtenerListaDeLoot(player);
            list.remove(lootTable);
            if(list.isEmpty()){
                config.getConfig().set(player.getUniqueId() + PlayerDataConfig.LOOT,null);
                saveConfig();
            }
            config.getConfig().set(player.getUniqueId() + PlayerDataConfig.LOOT,list);
            saveConfig();
        }
    }
    */
}
