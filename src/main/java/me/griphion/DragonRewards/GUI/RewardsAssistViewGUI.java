package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.DragonsConfig;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RewardsAssistViewGUI extends GUI{

    String dragon;
    GUI guiAnterior;

    public RewardsAssistViewGUI(Core plugin, Player player, String dragon, GUI guiAnterior) {
        super(plugin, player);
        this.dragon = dragon;
        this.guiAnterior = guiAnterior;
    }


    @Override
    public void buildAndOpenGUI() {

        FileConfiguration dragonConfig = new DragonsConfig(dragon).getConfig();
        String lootTable = dragonConfig.getString(DragonsConfig.RECOMPENSA_ASISTENCIA);

        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Loot asistencias", 54, event -> {
            if(noTienePermisoGUI("dragonrewards.recompensas",player)){
                event.setWillDestroy(true);
                return;
            }
            switch (event.getName()){
                case "&eEditar items":
                    event.setWillDestroy(true);
                    delayEntreMenues(new EditLootTableItemsListGUI(plugin,player,lootTable,this));
                    return;
                case "&eElegir otra LootTable":
                    event.setWillDestroy(true);
                    delayEntreMenues(new RewardsAssistChangeGUI(plugin,player,dragon,this));
                    return;
                case "&9Volver":
                    event.setWillDestroy(true);
                    delayEntreMenues(guiAnterior);
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
                .setOption(53, new ItemStack(Material.ARROW, 1), "&9Volver",null);
        agregarLootConInfo(chestMenu, lootTable);
        chestMenu.setSpecificTo(player);
        if(player.hasPermission("dragonrewards.editloot")){
            chestMenu.setOption(45, new ItemStack(Material.CHEST, 1), "&eEditar items",null)
                    .setOption(49, new ItemStack(Material.NAME_TAG, 1), "&eElegir otra LootTable",null);
        }
        chestMenu.open(player);
    }
}
