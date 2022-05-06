package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EditLootTablesListGUI extends GUI {

    public EditLootTablesListGUI(Core plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void buildAndOpenGUI() {
        if (noTienePermisoGUI("dragonrewards.editloot", player)) {
            return;
        }
        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "LootTables disponibles", 54, event -> {
            if (noTienePermisoGUI("dragonrewards.editloot", player)) {
                event.setWillDestroy(true);
                return;
            }

            if (event.getPosition() < 36) {
                event.setWillDestroy(true);
                delayEntreMenues(new EditLootTableOptionsGUI(plugin, player, event.getName().replace("&b", ""), this));
                return;
            }
            switch (event.getName()) {
                case "&cSalir":
                    event.setWillDestroy(true);
                    return;
                case "&aCrear una nueva LootTable":
                    event.setWillDestroy(true);
                    delayEntreMenues(new EditLootTableNewLTGUI(plugin, player));
                    break;
            }
        }, plugin)
                .setOption(36, separador, " ",null)
                .setOption(37, separador, " ",null)
                .setOption(38, separador, " ",null)
                .setOption(39, separador, " ",null)
                .setOption(40, separador, " ",null)
                .setOption(41, separador, " ",null)
                .setOption(42, separador, " ",null)
                .setOption(43, separador, " ",null)
                .setOption(44, separador, " ",null)
                .setOption(45, new ItemStack(Material.CRAFTING_TABLE, 1), "&aCrear una nueva LootTable", null)
                .setOption(53, new ItemStack(Material.BARRIER, 1), "&cSalir", null);
        agregarLootTables(chestMenu, Arrays.asList("", ChatColor.GRAY + "Click izquierdo para editar"));
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }
}
