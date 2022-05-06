package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class EditLootTableItemEditorQuantityGUI extends GUI {

    String lootTable;
    int itemPosition;
    GUI guiAnterior;

    public EditLootTableItemEditorQuantityGUI(Core plugin, Player player, String lootTable, int itemPosition, GUI guiAnterior) {
        super(plugin, player);
        this.lootTable = lootTable;
        this.itemPosition = itemPosition;
        this.guiAnterior = guiAnterior;
    }

    @Override
    public void buildAndOpenGUI() {

        if (noTienePermisoGUI("dragonrewards.editloot", player)) return;
        if (laLootTableNoExiste(lootTable, player)) return;
        if (elItemNoExiste(itemPosition,lootTable,player)) {
            delayEntreMenues(guiAnterior);
            return;
        }

        CustomLootTable customLootTable = new CustomLootTable(lootTable);

        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Editando cantidad de items",9, event -> {
            if (noTienePermisoGUI("dragonrewards.editloot", player)) {
                event.setWillDestroy(true);
                return;
            }
            if (laLootTableNoExiste(lootTable, player)) {
                event.setWillDestroy(true);
                return;
            }
            if (elItemNoExiste(itemPosition,lootTable,player)) {
                event.setWillDestroy(true);
                delayEntreMenues(guiAnterior);
                return;
            }
            int value;
            switch(event.getName()){
                case "&eMínimo &a+1":
                    value = customLootTable.getItemMin(itemPosition);
                    value++;
                    customLootTable.setItemMin(itemPosition,value);
                    actualizarMinimo(event,customLootTable);
                    break;
                case "&eMínimo &c-1":
                    value = customLootTable.getItemMin(itemPosition);
                    value--;
                    if(value < 0) break;
                    customLootTable.setItemMin(itemPosition,value);
                    actualizarMinimo(event,customLootTable);
                    break;
                case "&eMáximo &a+1":
                    value = customLootTable.getItemMax(itemPosition);
                    value++;
                    customLootTable.setItemMax(itemPosition,value);
                    actualizarMaximo(event,customLootTable);
                    break;
                case "&eMáximo &c-1":
                    value = customLootTable.getItemMax(itemPosition);
                    value--;
                    if(value < 0) break;
                    customLootTable.setItemMax(itemPosition,value);
                    actualizarMaximo(event,customLootTable);
                    break;
                case "&9Volver":
                    event.setWillDestroy(true);
                    delayEntreMenues(guiAnterior);
                    break;
            }

        },plugin)
                .setOption(0, new ItemStack(Material.GOLD_INGOT, 1), "&eMínimo &a+1", Collections.singletonList(ChatColor.WHITE + "Mínimo actual: " + ChatColor.GREEN + customLootTable.getItemMin(itemPosition)))
                .setOption(2, new ItemStack(Material.GOLD_NUGGET, 1), "&eMínimo &c-1", Collections.singletonList(ChatColor.WHITE + "Mínimo actual: " + ChatColor.GREEN + customLootTable.getItemMin(itemPosition)))
                .setOption(4, new ItemStack(Material.IRON_INGOT, 1), "&eMáximo &a+1", Collections.singletonList(ChatColor.WHITE + "Máximo actual: " + ChatColor.GREEN + customLootTable.getItemMax(itemPosition)))
                .setOption(6, new ItemStack(Material.IRON_NUGGET, 1), "&eMáximo &c-1", Collections.singletonList(ChatColor.WHITE + "Máximo actual: " + ChatColor.GREEN + customLootTable.getItemMax(itemPosition)))
                .setOption(8, new ItemStack(Material.ARROW, 1), "&9Volver", null);
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }
    private void actualizarMinimo(ChestMenu.OptionClickEvent event,CustomLootTable customLootTable){
        event.addItemOption(0, new ItemStack(Material.GOLD_INGOT, 1), "&eMínimo &a+1", Collections.singletonList(ChatColor.WHITE + "Mínimo actual: " + ChatColor.GREEN + customLootTable.getItemMin(itemPosition)));
        event.addItemOption(2, new ItemStack(Material.GOLD_NUGGET, 1), "&eMínimo &c-1", Collections.singletonList(ChatColor.WHITE + "Mínimo actual: " + ChatColor.GREEN + customLootTable.getItemMin(itemPosition)));
    }
    private void actualizarMaximo(ChestMenu.OptionClickEvent event,CustomLootTable customLootTable){
        event.addItemOption(4, new ItemStack(Material.IRON_INGOT, 1), "&eMáximo &a+1", Collections.singletonList(ChatColor.WHITE + "Máximo actual: " + ChatColor.GREEN + customLootTable.getItemMax(itemPosition)));
        event.addItemOption(6, new ItemStack(Material.IRON_NUGGET, 1), "&eMáximo &c-1", Collections.singletonList(ChatColor.WHITE + "Máximo actual: " + ChatColor.GREEN + customLootTable.getItemMax(itemPosition)));
    }

}
