package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.DragonsConfig;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class RewardsLastHitChangeGUI extends GUI{

    GUI guiAnterior;
    String dragon;
    public RewardsLastHitChangeGUI(Core plugin, Player player, String dragon, GUI guiAnterior) {
        super(plugin, player);
        this.dragon = dragon;
        this.guiAnterior = guiAnterior;
    }

    @Override
    public void buildAndOpenGUI() {
        if(noTienePermisoGUI("dragonrewards.editloot", player)) return;
        if(Core.noEsUnDragonFileNameValido(dragon,player)) return;
        DragonsConfig dragonConfig = new DragonsConfig(dragon);

        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Elija otra LootTable para el UltG", 54, event -> {
            if(noTienePermisoGUI("dragonrewards.editloot",player)){
                event.setWillDestroy(true);
                return;
            }

            if(Core.noEsUnDragonFileNameValido(dragon,player)){
                event.setWillDestroy(true);
                return;
            }

            if(event.getName().equals("&9Volver")){
                event.setWillDestroy(true);
                delayEntreMenues(guiAnterior);
                return;
            }

            if (event.getPosition() < 36) {
                String lootTable = event.getName().replace("&b", "");
                if (laLootTableNoExiste(lootTable, player)) {
                    event.setWillDestroy(true);
                }else{
                    dragonConfig.getConfig().set(DragonsConfig.RECOMPENSA_ULTIMO_GOLPE,lootTable);
                    dragonConfig.saveConfig();
                    event.setWillDestroy(true);
                    delayEntreMenues(guiAnterior);
                }
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
        agregarLootTables(chestMenu, Arrays.asList("",ChatColor.GRAY + "Click izquierdo para asignar esta LootTable al Último Golpe"));
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }
}
