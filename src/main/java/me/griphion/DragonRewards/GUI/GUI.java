package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.ConfigFiles.LootTable.ItemWithProbability;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


public abstract class GUI{
    Core plugin;
    ChestMenu chestMenu;
    Player player;
    final ItemStack separador = new ItemStack(Material.BLACK_STAINED_GLASS_PANE,1);

    public GUI(Core plugin, Player player){
        this.plugin = plugin;
        this.player = player;
    }

    public abstract void buildAndOpenGUI();

    public void delayEntreMenues(GUI gui){
        if(gui != null)
        plugin.getServer().getScheduler().runTaskLater(plugin, gui::buildAndOpenGUI, 2L);
    }
    public boolean noTienePermisoGUI(final String permiso, final Player player){
        if(!player.hasPermission(permiso)){
            player.sendMessage(ChatColor.RED + "No tienes permiso para hacer eso!");
            return true;
        }
        return false;
    }
    public void agregarLoot(final ChestMenu menu, final String lootTable, final boolean isNewLootTable){ //Agrega loot actual de la CustomLootTable elegida
        CustomLootTable customLootTableConfig = new CustomLootTable(lootTable);
        if(lootTable == null) {
            return;
        }
        List<ItemStack> list = customLootTableConfig.getItems(isNewLootTable);
        if(list == null) return;
        if(list.isEmpty()) return;
        int pos = 0;
        for (ItemStack elem : list){
            if(elem==null) continue;
            if(pos < 36 && elem.getItemMeta() != null){
                menu.setOption(pos,elem,elem.getItemMeta().getDisplayName(),elem.getItemMeta().getLore());
                pos++;
            }
        }
    }
    public void agregarLootConInfo(final ChestMenu menu, final String lootTable){ //Agrega loot actual de la CustomLootTable elegida con información
        if(laLootTableNoExiste(lootTable,null)) return;
        List<ItemWithProbability> list = (new CustomLootTable(lootTable)).obtenerItemsConProbabilidad();
        if(list == null) return;
        if(list.isEmpty()) return;
        int pos = 0;
        ItemMeta im;
        List<String> lore;
        for (ItemWithProbability elem : list){
            im = elem.item.getItemMeta();
            if(im == null) continue;
            if(im.getLore() == null){
                lore = new ArrayList<>();
            }else{
                lore = im.getLore();
            }
            lore.addAll(Arrays.asList("",ChatColor.WHITE + "-------------------"));
            if(elem.maximo > elem.minimo && elem.probabilidad > 0){
                lore.addAll(Collections.singletonList(ChatColor.GOLD + "Probabilidad: " + ChatColor.YELLOW + elem.probabilidad + "%"));
                lore.addAll(Collections.singletonList(ChatColor.GOLD + "Chances: " + ChatColor.YELLOW + (elem.maximo-elem.minimo)));
                if(elem.minimo > 0){
                    lore.addAll(Collections.singletonList(ChatColor.GOLD + "Garantizado: " + ChatColor.YELLOW + elem.minimo));
                }
            }else if (elem.maximo > 0 && elem.probabilidad > 0){
                lore.addAll(Collections.singletonList(ChatColor.GOLD + "Garantizado: " + ChatColor.YELLOW + elem.maximo));
            }else{
                lore.addAll(Collections.singletonList(ChatColor.RED + "Item no disponible."));
            }
            lore.addAll(Arrays.asList(ChatColor.WHITE + "-------------------",""));

            if(pos < 36 && elem.item.getItemMeta() != null){
                menu.setOption(pos,elem.item,elem.item.getItemMeta().getDisplayName(),lore);
                pos++;
            }
        }
    }

    public void agregarLootTables(final ChestMenu menu, final List<String> lore){
        short i = 0;
        for(String elem: Core.lootTablesDisponibles){
            menu.setOption(i, new ItemStack(Material.CHEST,1),"&b" + elem,lore);
            i++;
            if(i>35){
                Bukkit.getLogger().log(Level.SEVERE,"La cantidad de LootTables supera la cantidad maxima de 35!");
                return;
            }
        }
    }
    public boolean laLootTableNoExiste(final String lootTable, final Player player){
        if(Core.lootTablesDisponibles.contains(lootTable)){
            return false;
        }
        if(player != null)
        player.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "Esta LootTable ya no existe!");
        return true;
    }
    public boolean elItemNoExiste(final int itemPosition, final String lootTable, final Player player){
        CustomLootTable customLootTable = new CustomLootTable(lootTable);
        if(customLootTable.itemExists(itemPosition)){
            return false;
        }
        player.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "El item ya no existe!");
        return true;
    }
}

//------------------------------------------------------------------------------------------------------

