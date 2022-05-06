package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.Core;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditLootTableNewLTGUI extends GUI {

    public EditLootTableNewLTGUI(Core plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void buildAndOpenGUI() {
        if (noTienePermisoGUI("dragonrewards.editloot", player)) {
            return;
        }

        new AnvilGUI.Builder()
                .onComplete((player2, text) -> {
                    if (noTienePermisoGUI("dragonrewards.editloot", player)) {
                        return AnvilGUI.Response.close();
                    }
                    if (Core.lootTablesDisponibles.contains(text)) {
                        return AnvilGUI.Response.text("Nombre no disponible");
                    }
                    delayEntreMenues(new EditLootTableItemsListGUI(plugin, player, text, this));
                    Core.lootTablesDisponibles.add(text);
                    return AnvilGUI.Response.close();
                })
                .text("Nombre")
                .title("Creando nueva LootTable")
                .itemLeft(new ItemStack(Material.CHEST))
                .onLeftInputClick(player1 -> {
                    player1.closeInventory();
                    delayEntreMenues(new EditLootTablesListGUI(plugin, player1));
                })
                .plugin(plugin)
                .open(player);
    }
}
