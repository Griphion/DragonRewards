package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.Core;
import me.griphion.DragonRewards.GUI.ChatGUI.EditLootTableCommandsChatGUI;
import me.griphion.DragonRewards.Utils.SpecificPlayerChatListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EditLootTableOptionsGUI extends GUI {

    String lootTable;
    GUI guiAnterior;

    public EditLootTableOptionsGUI(Core plugin, Player player, String lootTable, GUI guiAnterior) {
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
        CustomLootTable customLootTable = new CustomLootTable(lootTable);
        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Menu de edición de " + lootTable, 54, event -> {
            if (noTienePermisoGUI("dragonrewards.editloot", player)) {
                event.setWillDestroy(true);
                return;
            }
            if (laLootTableNoExiste(lootTable, player)) {
                event.setWillDestroy(true);
                return;
            }
            switch (event.getName()) {
                case "&4&lEliminar LootTable":
                    event.setWillDestroy(true);
                    delayEntreMenues(new EditLootTableDeleteGUI(plugin, player, lootTable, this));
                    return;
                case "&eCambiar cantidad de dinero":
                    event.setWillDestroy(true);
                    player.sendMessage(plugin.helpPrefix + ChatColor.WHITE + "Ingrese la cantidad de dinero con la que quiere recompensar (Puede llevar coma):");
                    player.sendMessage(plugin.helpPrefix + ChatColor.GRAY + "Para salir/cancelar escriba \"salir\"");
                    new SpecificPlayerChatListener(plugin, player, listener -> {
                        String mensaje = listener.getMessage();
                        if(mensaje.matches("[0-9]*[.,]?[0-9]*")){
                            mensaje = mensaje.replace(',','.');
                            customLootTable.setDinero(Double.parseDouble(mensaje));
                            Bukkit.getScheduler().runTask(plugin, () -> (new EditLootTableOptionsGUI(plugin, player,lootTable,guiAnterior)).buildAndOpenGUI());
                            listener.setWillStop(true);
                        }else if (listener.getMessage().equalsIgnoreCase("salir")){
                            player.sendMessage(ChatColor.RED + "Saliendo del editor.");
                            listener.setWillStop(true);
                            Bukkit.getScheduler().runTask(plugin, () -> (new EditLootTableOptionsGUI(plugin, player,lootTable,guiAnterior)).buildAndOpenGUI());
                        }else{
                            player.sendMessage(ChatColor.RED+"Ingrese un número válido!");
                        }
                    });
                    return;
                case "&eCambiar cantidad de experiencia":
                    event.setWillDestroy(true);
                    player.sendMessage(plugin.helpPrefix + ChatColor.WHITE + "Ingrese la cantidad de experiencia o niveles (Poniendo una L a la derecha del valor):");
                    player.sendMessage(plugin.helpPrefix + ChatColor.GRAY + "Para salir/cancelar escriba \"salir\"");
                    new SpecificPlayerChatListener(plugin, player, listener -> {
                        String mensaje = listener.getMessage();
                        if(mensaje.matches("[0-9]*L?")){
                            customLootTable.setExperiencia(mensaje);
                            Bukkit.getScheduler().runTask(plugin, () -> (new EditLootTableOptionsGUI(plugin, player,lootTable,guiAnterior)).buildAndOpenGUI());
                            listener.setWillStop(true);
                        }else if (listener.getMessage().equalsIgnoreCase("salir")){
                            player.sendMessage(ChatColor.RED + "Saliendo del editor.");
                            listener.setWillStop(true);
                            Bukkit.getScheduler().runTask(plugin, () -> (new EditLootTableOptionsGUI(plugin, player,lootTable,guiAnterior)).buildAndOpenGUI());
                        }else{
                            player.sendMessage(ChatColor.RED+"Ingrese un número válido!");
                        }
                    });
                    return;
                case "&eEditar Items":
                    event.setWillDestroy(true);
                    delayEntreMenues(new EditLootTableItemsListGUI(plugin, player, lootTable, this));
                    return;
                case "&eEditar commandos":
                    event.setWillDestroy(true);
                    (new EditLootTableCommandsChatGUI(plugin,player,lootTable)).buildAndOpenGUI();
                    return;
                case "&9Volver":
                    event.setWillDestroy(true);
                    if (guiAnterior != null)
                        delayEntreMenues(guiAnterior);
                    break;
            }
        }, plugin)
                .setOption(36, separador, " ", null)
                .setOption(37, separador, " ", null)
                .setOption(38, separador, " ", null)
                .setOption(39, separador, " ", null)
                .setOption(40, separador, " ", null)
                .setOption(41, separador, " ", null)
                .setOption(42, separador, " ", null)
                .setOption(43, separador, " ", null)
                .setOption(44, separador, " ", null)
                .setOption(45, new ItemStack(Material.TNT, 1), "&4&lEliminar LootTable", null)
                .setOption(47, new ItemStack(Material.EMERALD, 1), "&eCambiar cantidad de dinero", Arrays.asList("",ChatColor.WHITE + "Recompensa actual: " + ChatColor.GREEN + customLootTable.getDinero(),"", ChatColor.GRAY + "Cantidad de dinero que se le va a dar al", ChatColor.GRAY + "jugador cuando reclame la recompensa.", "", ChatColor.RED + "" + ChatColor.BOLD + "Esto solo funciona si se tiene un", ChatColor.RED + "" + ChatColor.BOLD + "plugin de economía que use Vault."))
                .setOption(48, new ItemStack(Material.EXPERIENCE_BOTTLE, 1), "&eCambiar cantidad de experiencia", Arrays.asList("",ChatColor.WHITE + "Recompensa actual: " + ChatColor.GREEN + customLootTable.getExperiencia(),"",ChatColor.GRAY + "Cantidad de experiencia o niveles (L)", ChatColor.GRAY + "que se le va a dar al jugador cuando",ChatColor.GRAY + "reclame la recompensa."))
                .setOption(50, new ItemStack(Material.COMMAND_BLOCK, 1), "&eEditar commandos", Arrays.asList("", ChatColor.GRAY + "Comandos que se ejecutarán cuando", ChatColor.GRAY + "se reclame la recompensa."))
                .setOption(51, new ItemStack(Material.CHEST, 1), "&eEditar Items", null)
                .setOption(53, new ItemStack(Material.ARROW, 1), "&9Volver", null);
        agregarLootConInfo(chestMenu, lootTable);
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }
}
