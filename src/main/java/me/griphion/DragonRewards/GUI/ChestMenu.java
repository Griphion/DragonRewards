package me.griphion.DragonRewards.GUI;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ChestMenu implements Listener {

    private final String name;
    private final int size;
    private OptionClickEventHandler handler;
    private Plugin plugin;
    private Player player;
    private Inventory inventory;
    private boolean considerClickInPlayerInventory = false;

    private String[] optionNames;
    private ItemStack[] optionIcons;

    public ChestMenu(String name, int size, OptionClickEventHandler handler, Plugin plugin) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ChestMenu(String name, int size, OptionClickEventHandler handler, Plugin plugin, boolean considerClickInPlayerInventory) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        this.considerClickInPlayerInventory = considerClickInPlayerInventory;
        this.optionNames = new String[size + 36];
        this.optionIcons = new ItemStack[size + 36];
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ChestMenu setOption(int position, ItemStack icon, String name, List<String> info) {
        optionNames[position] = name;
        optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }

    public void setSpecificTo(Player player) {
        this.player = player;
        Core.usuariosConGuiAbierta.add(player);
    }


    public boolean isSpecific() {
        return player != null;
    }


    public void open(Player player) {
        inventory = Bukkit.createInventory(player, size, name);
        for (int i = 0; i < optionIcons.length; i++) {
            if (optionIcons[i] != null) {
                inventory.setItem(i, optionIcons[i]);
            }
        }
        player.openInventory(inventory);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        handler = null;
        plugin = null;
        optionNames = null;
        optionIcons = null;
        player = null;
        inventory = null;
    }

    public void close(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, player::closeInventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(InventoryClickEvent event) {
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;

        if ( (event.getView().getTitle().equals(name) || (considerClickInPlayerInventory && event.getRawSlot() >= size ) ) && (player == null || event.getWhoClicked() == player)) {
            event.setCancelled(true);
            if (event.getClick() != ClickType.LEFT)
                return;
            int slot = event.getRawSlot();
            if (slot >= 0 && (slot < size || (slot >= size && considerClickInPlayerInventory)) && (optionNames[slot] != null || (considerClickInPlayerInventory))) {
                OptionClickEvent e;
                if(considerClickInPlayerInventory && slot >= size){
                    try {
                        event.getCurrentItem().getItemMeta();
                    }catch (Exception exception){
                        return;
                    }
                    e = new OptionClickEvent((Player) event.getWhoClicked(), slot, event.getCurrentItem().getItemMeta().getDisplayName(), event.getCurrentItem());
                }else {
                    e = new OptionClickEvent((Player) event.getWhoClicked(), slot, optionNames[slot], optionIcons[slot]);
                }
                handler.onOptionClick(e);
                ((Player) event.getWhoClicked()).updateInventory();
                if (e.willClose()) {
                    close((Player) event.getWhoClicked());
                }
                if (e.willDestroy()) {
                    close((Player) event.getWhoClicked());
                    destroy();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    void  onInventoryClose(InventoryCloseEvent inventoryCloseEvent){ //Para cerrar el inventario automáticamente si salen por otra razón que no sea "willDestroy"
        if (inventoryCloseEvent.getView().getTitle().equals(name) && (isSpecific() && inventoryCloseEvent.getPlayer() == player)) {
            destroy();
        }
    }

    public interface OptionClickEventHandler {
        void onOptionClick(OptionClickEvent event);
    }

    public class OptionClickEvent {
        private final Player player;
        private final int position;
        private final String name;
        private boolean close;
        private boolean destroy;
        private final ItemStack item;

        public OptionClickEvent(Player player, int position, String name, ItemStack item) {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = false;
            this.destroy = false;
            this.item = item;
        }

        public Player getPlayer() {
            return player;
        }

        public int getPosition() {
            return position;
        }

        public String getName() {
            return name;
        }

        public boolean willClose() {
            return close;
        }

        public boolean willDestroy() {
            return destroy;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }

        public ItemStack getItem() {
            return item;
        }

        public void addItemOption(int position, ItemStack icon, String name, List<String> lore){
            inventory.setItem(position,setItemNameAndLore(icon,name,lore));
            player.updateInventory();
            setOption(position, icon, name, lore);
        }

        /* Retorna la posición donde se colocó el item */
        public int addItemOptionFill(ItemStack icon, String name, List<String> info){
            int j;
            for(j = 0; j < size; j++){
                if(optionIcons[j] == null){
                    addItemOption(j, icon, name, info);
                    break;
                }
            }
            return j;
        }

        public void removeOption(final int position){
            inventory.setItem(position,null);
            player.updateInventory();
            optionNames[position] = null;
            optionIcons[position] = null;
        }
    }

    private ItemStack setItemNameAndLore(final ItemStack item, String name, final List<String> lore) {
        ItemMeta im = item.getItemMeta();
        if(im == null) return item;
        name = ChatColor.translateAlternateColorCodes('&', name);
        im.setDisplayName(name);
        im.setLore(lore);
        item.setItemMeta(im);
        return item;
    }

}
