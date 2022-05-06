package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RewardsOptionsGUI extends GUI{

    String dragon;
    GUI guiAnterior;

    public RewardsOptionsGUI(Core plugin, Player player, String dragon, GUI guiAnterior) {
        super(plugin, player);
        this.dragon = dragon;
        this.guiAnterior = guiAnterior;
    }

    @Override
    public void buildAndOpenGUI() {
        if(noTienePermisoGUI("dragonrewards.recompensas",player)){
            return;
        }
        if(Core.noEsUnDragonFileNameValido(dragon,player)){
            return;
        }
        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Recompensas de " + dragon, 9, event -> {
            if(noTienePermisoGUI("dragonrewards.recompensas",player)){
                event.setWillDestroy(true);
                return;
            }
            if(Core.noEsUnDragonFileNameValido(dragon,player)){
                event.setWillDestroy(true);
                return;
            }
            switch (event.getName()){
                case "&eUltimo Golpe":
                    event.setWillDestroy(true);
                    delayEntreMenues(new RewardsLastHitViewGUI(plugin,player,dragon,this));
                    return;
                case "&eAsistencia":
                    event.setWillDestroy(true);
                    delayEntreMenues(new RewardsAssistViewGUI(plugin,player,dragon,this));
                    return;
                case "&9Volver":
                    event.setWillDestroy(true);
                    delayEntreMenues(guiAnterior);
                    break;
            }

        }, plugin)
                .setOption(0, new ItemStack(Material.NETHERITE_SWORD, 1), "&eUltimo Golpe",null)
                .setOption(4, new ItemStack(Material.SPECTRAL_ARROW, 1), "&eAsistencia",null)
                .setOption(8, new ItemStack(Material.ARROW, 1), "&9Volver",null);
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }
}
