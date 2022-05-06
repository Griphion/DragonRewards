package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditLootTableDeleteGUI extends GUI {

    String lootTable;
    GUI guiAnterior;

    public EditLootTableDeleteGUI(Core plugin, Player player, String lootTable, GUI guiAnterior) {
        super(plugin, player);
        this.lootTable = lootTable;
        this.guiAnterior = guiAnterior;
    }

    @Override
    public void buildAndOpenGUI() {
        if (noTienePermisoGUI("dragonrewards.editloot", player)) {
            return;
        }
        if (laLootTableNoExiste(lootTable, player)) {
            return;
        }
        final String confirmarEliminadoNombre = "&aConfirmar";
        final String cancelarEliminadoNombre = "&cCancelar";
        List<String> confirmarEliminadoLore = new ArrayList<>(Collections.singletonList(ChatColor.RED + "" + ChatColor.BOLD + "Esta acción no se puede deshacer!"));

        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Confirmación eliminado de: " + lootTable, 9, event -> {
            if (noTienePermisoGUI("dragonrewards.editloot", player)) {
                event.setWillDestroy(true);
                return;
            }
            if (laLootTableNoExiste(lootTable, player)) {
                event.setWillDestroy(true);
                return;
            }
            switch (event.getName()) {
                case confirmarEliminadoNombre:
                    event.setWillDestroy(true);
                    CustomLootTable customLootTableConfig = new CustomLootTable(lootTable);
                    customLootTableConfig.delete();
                    Core.lootTablesDisponibles.remove(lootTable);
                    player.sendMessage(Core.plugin.pluginPrefix + ChatColor.GREEN + "LootTable eliminada con éxito!");
                    return;
                case cancelarEliminadoNombre:
                    event.setWillDestroy(true);
                    delayEntreMenues(guiAnterior);
                    break;
            }

        }, plugin)
                .setOption(0, new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1), confirmarEliminadoNombre, confirmarEliminadoLore)
                .setOption(1, new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1), confirmarEliminadoNombre, confirmarEliminadoLore)
                .setOption(2, new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1), confirmarEliminadoNombre, confirmarEliminadoLore)
                .setOption(3, new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1), confirmarEliminadoNombre, confirmarEliminadoLore)
                .setOption(5, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), cancelarEliminadoNombre, null)
                .setOption(6, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), cancelarEliminadoNombre, null)
                .setOption(7, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), cancelarEliminadoNombre, null)
                .setOption(8, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), cancelarEliminadoNombre, null);
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }
}
