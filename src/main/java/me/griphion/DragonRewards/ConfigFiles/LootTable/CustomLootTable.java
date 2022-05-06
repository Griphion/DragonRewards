package me.griphion.DragonRewards.ConfigFiles.LootTable;

import me.griphion.DragonRewards.ConfigFiles.ConfigFileManager;
import me.griphion.DragonRewards.Core;
import me.griphion.DragonRewards.Utils.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class CustomLootTable {
    private final ConfigFileManager config;
    private final String fileName;
    static public final int MAX_ITEMS_PER_LOOT_TABLE = 36; //Representa la máxima cantidad de loot que se puede poner en una loottable [0-35] (En caso de querer aumentarlo cambiar la GUI!)

    public CustomLootTable(String fileName){
        this.fileName = fileName;
        config = new ConfigFileManager(fileName, ConfigFileManager.directoryEnum.LOOTTABLE);
    }
    public void saveConfig(){
        config.saveConfig();
    }
    public void delete(){ config.deleteFile();}

    public void agregarComando(String comando){
        List<String> comandos = getComandos();
        comandos.add(comando);
        config.getConfig().set("comandos",comandos);
        saveConfig();
    }
    public void remComando(int position){
        List<String> comandos = getComandos();
        if(position > comandos.size()) return;
        comandos.remove(position);
        if(comandos.isEmpty()){
            config.getConfig().set("comandos",null);
        }else{
            config.getConfig().set("comandos",comandos);
        }
        saveConfig();
    }
    public List<String> getComandos(){
        return config.getConfig().getStringList("comandos");
    }

    public void setExperiencia(String exp){
        config.getConfig().set("experiencia",exp);
        saveConfig();
    }

    public String getExperiencia(){
        return config.getConfig().getString("experiencia","0");
    }

    public void setDinero(double cantidad){
        if(cantidad < 0){
            config.getConfig().set("dinero",0);
        }else{
            config.getConfig().set("dinero",cantidad);
        }
        saveConfig();
    }

    public double getDinero(){
        return config.getConfig().getDouble("dinero",0);
    }

    public void darDinero(Player player){
        if (player == null) return;
        if(!Core.useVault) return;
        if(getDinero() <= 0) return;
        Core.economyManager.eco.depositPlayer(player,getDinero());
        player.sendMessage(ChatColor.GREEN + "Has recibido: " + ChatColor.GOLD + Core.economyManager.eco.format(getDinero()));
    }

    public void darExperiencia(Player player){
        String exp = getExperiencia();
        if(exp == null || exp.equals("0") || exp.equals("0L")) return;
        try{
            if(exp.endsWith("L")){
                player.giveExpLevels(Integer.parseInt(exp.replace("L","")));
            }else {
                player.giveExp(Integer.parseInt(exp));
            }
        }catch (NumberFormatException e){
            Bukkit.getLogger().log(Level.SEVERE, "Error en la LootTable: '" + fileName + "', la experiencia tiene mal el formato!");
        }

    }

    private String formatearComando(String comando, Player player){
        return comando.replace("{nombre}",player.getName()).replace("{mundo}",player.getWorld().getName());
    }

    public void ejecutarComandos(Player player){
        for(String cmd : getComandos()){
            if(cmd.startsWith("[consola]")){
                cmd = cmd.replace("[consola]", "");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),formatearComando(cmd, player));
            }else if (cmd.startsWith("[usuario]")){
                cmd = cmd.replace("[usuario]", "");
                Bukkit.dispatchCommand(player,formatearComando(cmd, player));
            }
        }
    }

    public void agregarItem(final ItemStack item, final int posicion){
        int pos;
        if(posicion == -1){
            pos = obtenerIndiceDelUltimoItem() + 1;
        }else {
            pos = posicion;
        }
        if(pos > MAX_ITEMS_PER_LOOT_TABLE){ //Posiciones del inventario posibles: 0-35
            Bukkit.getLogger().log(Level.SEVERE,"Error! Se quiso agregar un item a la loot table: '" + this.fileName + "' pero ya esta llena!");
            return;
        }

        config.getConfig().set("items." + pos + ".probabilidad",0D);
        config.getConfig().set("items." + pos + ".minimo",0);
        config.getConfig().set("items." + pos + ".maximo",0);
        config.getConfig().set("items." + pos + ".item",item);

        saveConfig();
    }

    public void removerItem(final int posicion){
        config.getConfig().set("items." + posicion,null);
        saveConfig();
        ordenarIndicesItems(posicion);
    }

    public void ordenarIndicesItems(int removedPosition){
        int[] indices = obtenerIndicesDeLosItems();
        if(indices == null) return;
        for(int i : indices){
            if(i>removedPosition){
                cambiarPosicionItem(i,i-1);
            }
        }
    }

    private void cambiarPosicionItem(int actualPosition, int newPosition){
        config.getConfig().set("items." + newPosition, config.getConfig().get("items." + actualPosition));
        config.getConfig().set("items." + actualPosition, null);
        saveConfig();
    }

    public List<ItemStack> getItems(final boolean isNewLootTable){
        if(isNewLootTable || config.isEmpty() || itemsIsEmptyOrDoesntExist()) return new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for(int i : obtenerIndicesDeLosItems()){
            if(itemExists(i)){
                items.add(getItem(i));
            }
        }

        return items;
    }

    public boolean itemExists(int position){
        return config.getConfig().contains("items." + position) && config.getConfig().contains("items." + position + ".item");
    }
    /*
    * Tipos de errores:
    * Error 1: Ocurre si no se pueden obtener los objetos de la LootTable correctamente (Revisar la consola)
    * Error 2: Ocurre si la lista de ítems esta vacía
    * Error 3: Ocurre si la CustomLootTable ingresada (En la config del dragón) no existe
    * */

    public boolean darLootTable(final Player player){
        ItemStack[] items;
        List<ItemWithProbability> itemsConProba;
        try{
            itemsConProba = obtenerItemsConProbabilidad();
        }catch (NullPointerException e){
            Bukkit.getLogger().log(Level.SEVERE,"[DragonRewards--ERROR-1]",e);
            player.sendMessage(Core.langManager.getConfigTranslation("lootTable-error-1"));
            return false;
        }

        if(itemsConProba.isEmpty()){
            Bukkit.getLogger().log(Level.WARNING,"[DragonRewards--ERROR-2]");
            player.sendMessage(Core.langManager.getConfigTranslation("lootTable-error-2"));
            return false;
        }

        if(Core.recompensasEnEspera.containsKey(player.getUniqueId())){
            items = Core.recompensasEnEspera.get(player.getUniqueId());
        }else{
            items = generarLoot(itemsConProba);
        }

        if(!jugadorTieneEspacio(player,items)){
            if(!Core.recompensasEnEspera.containsKey(player.getUniqueId()))
            Core.recompensasEnEspera.put(player.getUniqueId(),items);
            player.sendMessage(Core.langManager.getConfigTranslation("lootTable-no-space"));
            player.sendMessage(ChatColor.GRAY + "Necesitas " + ChatColor.WHITE + items.length + ChatColor.GRAY + " espacios libres en el inventario (Cuando tenga espacio escriba el comando nuevamente).");
            return false;
        }

        Core.recompensasEnEspera.remove(player.getUniqueId());
        player.getInventory().addItem(items);
        CustomLogger.log( "[Loot - Reclamado] -Usuario (UUID): " + player.getName() + " (" + player.getUniqueId() + ")" + " -Loot table: " + fileName + " -Cantidad de items/espacios: " + items.length);
        return true;
    }

    private ItemStack[] generarLoot(List<ItemWithProbability> itemsConProba){
        List<ItemStack> loot = new ArrayList<>();
        for(ItemWithProbability elem : itemsConProba){
            loot.addAll(elem.getRandomAmountOfItem());
        }
        return loot.toArray(new ItemStack[0]);
    }
    public boolean jugadorTieneEspacio(final Player player,final ItemStack[] items){
        if(player.getInventory().firstEmpty() == -1) return false;
        if(player.getInventory().isEmpty()) return true;
        int cant = 0;
        for(ItemStack elem : player.getInventory().getStorageContents()){
            if(elem == null || elem.getType().isAir()){
                cant++;
            }
        }
        return cant >= items.length;
    }

    public void vaciar(){
        this.config.getConfig().set("items",null);
        this.config.saveConfig();
    }

    public List<ItemWithProbability> obtenerItemsConProbabilidad(){
        List<ItemStack> items = getItems(false);
        List<ItemWithProbability> itemsConProbabilidad = new ArrayList<>();
        int[] indices = obtenerIndicesDeLosItems();
        int i = 0;
        for(ItemStack item : items){
            itemsConProbabilidad.add(new ItemWithProbability(item,
                    getItemProbability(indices[i]),
                    getItemMin(indices[i]),
                    getItemMax(indices[i])));
            i++;
        }
        return itemsConProbabilidad;
    }


    public int obtenerIndiceDelUltimoItem(){
        int[] indices = obtenerIndicesDeLosItems();
        return indices[indices.length - 1];
    }

    public int[] obtenerIndicesDeLosItems(){
        if(itemsIsEmptyOrDoesntExist()) return new int[0];
        Set<String> indicesString = Objects.requireNonNull(config.getConfig().getConfigurationSection("items")).getKeys(false);
        int[] indices = new int[indicesString.size()]; //Va de 0 a 35
        int i = 0;
        for(String elem: indicesString){
            indices[i] = Integer.parseInt(elem);
            i++;
        }
        return indices;
    }

    public boolean itemsIsEmptyOrDoesntExist(){
        return config.getConfig().getConfigurationSection("items") == null;
    }
    public int getItemMax(int position){
        return config.getConfig().getInt("items." + position + ".maximo",0);
    }
    public int getItemMin(int position){
        return config.getConfig().getInt("items." + position + ".minimo",0);
    }
    public double getItemProbability(int position){
        return config.getConfig().getDouble("items." + position + ".probabilidad",0);
    }
    public void setItemMax(int position, int value){
        config.getConfig().set("items." + position + ".maximo",value);
        saveConfig();
    }
    public void setItemMin(int position, int value){
        config.getConfig().set("items." + position + ".minimo",value);
        saveConfig();
    }
    public void setItemProbability(int position, double value){
        config.getConfig().set("items." + position + ".probabilidad",value);
        saveConfig();
    }
    public ItemStack getItem(int posicion){
        if(!itemExists(posicion)) return null;
        try {
            return (ItemStack) (config.getConfig().get("items." + posicion + ".item"));
        }catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, "Error al querer obtener un objeto de la lootTable: '" + this.fileName + "'. El item en la posición '"+ posicion +"' esta mal configurado!", e);
            return null;
        }
    }




}
