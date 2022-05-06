package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditLootTableItemsListGUI extends GUI {

    String lootTable;
    GUI guiAnterior;

    boolean isNewLootTable = false;

    public EditLootTableItemsListGUI(Core plugin, Player player, String lootTable, GUI guiAnterior) {
        super(plugin, player);
        this.player = player;
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
        CustomLootTable customLootTableConfig = new CustomLootTable(lootTable);

        if (guiAnterior instanceof EditLootTableNewLTGUI) {
            guiAnterior = new EditLootTablesListGUI(plugin, player);
            isNewLootTable = true;
            customLootTableConfig.vaciar();
        }
        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Editando: " + lootTable, 54, event -> {
            if (noTienePermisoGUI("dragonrewards.editloot", player)) {
                event.setWillDestroy(true);
                return;
            }
            if (laLootTableNoExiste(lootTable, player)) {
                event.setWillDestroy(true);
                return;
            }

            ItemStack itemSeleccionado = event.getItem();

            if (itemSeleccionado == null) return;

            if (event.getPosition() >= 54 && itemSeleccionado.getItemMeta() != null) { //Se considera un click en el inventario del jugador
                customLootTableConfig.agregarItem(itemSeleccionado, event.addItemOptionFill(itemSeleccionado, itemSeleccionado.getItemMeta().getDisplayName(), itemSeleccionado.getItemMeta().getLore()));
                return;
            } else if (event.getPosition() < 36) {//Se considera un click en el gui/menu
                delayEntreMenues(new EditLootTableItemEditorGUI(plugin, player, lootTable, event.getPosition(), this));
                return;
            }

            if (event.getName().equals("&9Volver")) {
                event.setWillDestroy(true);
                if (guiAnterior != null)
                    delayEntreMenues(guiAnterior);
            }
        }, plugin, true)
                .setOption(36, separador, " ", null)
                .setOption(37, separador, " ", null)
                .setOption(38, separador, " ", null)
                .setOption(39, separador, " ", null)
                .setOption(40, separador, " ", null)
                .setOption(41, separador, " ", null)
                .setOption(42, separador, " ", null)
                .setOption(43, separador, " ", null)
                .setOption(44, separador, " ", null)
                .setOption(53, new ItemStack(Material.ARROW, 1), "&9Volver", null);
        agregarLoot(chestMenu, lootTable, isNewLootTable);
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }

}
