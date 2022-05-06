package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class EditDragonListGUI extends GUI{

    public EditDragonListGUI(Core plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void buildAndOpenGUI() {
        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Dragones disponibles:", 54, event -> {

            if(noTienePermisoGUI("dragonrewards.recompensas",player)){
                event.setWillDestroy(true);
                return;
            }

            if(event.getName().equals("&cSalir")){
                event.setWillDestroy(true);
                return;
            }

            if (event.getPosition() < 36) {//Se considera un click en el gui/menu
                String dragon = event.getName().replace("&d" ,"");

                if(Core.noEsUnDragonFileNameValido(dragon,player)){
                    event.setWillDestroy(true);
                }else{
                    event.setWillDestroy(true);
                    delayEntreMenues(new RewardsOptionsGUI(plugin,player,dragon,this));
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
                .setOption(49, new ItemStack(Material.BARRIER, 1), "&cSalir",null);
        agregarDragones(chestMenu);
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }

    private void agregarDragones(final ChestMenu menu){
        short i = 0;
        for(Core.DragonNameAndFileName elem: Core.dragonesDisponibles){
            menu.setOption(i, new ItemStack(Material.DRAGON_HEAD,1), "&d" + elem.getDragonFileName(),null);
            i++;
            if(i>35){
                Bukkit.getLogger().log(Level.SEVERE,"La cantidad de dragones supera la cantidad maxima de 35!");
                return;
            }
        }
    }
}
