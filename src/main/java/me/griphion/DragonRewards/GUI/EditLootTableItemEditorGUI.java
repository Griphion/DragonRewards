package me.griphion.DragonRewards.GUI;

import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.Core;
import me.griphion.DragonRewards.Utils.SpecificPlayerChatListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class EditLootTableItemEditorGUI extends GUI {

    String lootTable;
    int itemPosition;
    GUI guiAnterior;

    public EditLootTableItemEditorGUI(Core plugin, Player player, String lootTable, int itemPosition, GUI guiAnterior) {
        super(plugin, player);
        this.lootTable = lootTable;
        this.itemPosition = itemPosition;
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
        ItemStack item = customLootTable.getItem(itemPosition);
        chestMenu = new ChestMenu(ChatColor.DARK_GRAY + "Editando item de: " + lootTable, 45, event -> {
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
            switch (event.getName()) {
                case "&cEliminar item":
                    customLootTable.removerItem(itemPosition);
                    event.setWillDestroy(true);
                    delayEntreMenues(guiAnterior);
                    return;
                case "&eCambiar cantidad":
                    event.setWillDestroy(true);
                    delayEntreMenues(new EditLootTableItemEditorQuantityGUI(plugin,player,lootTable,itemPosition,this));
                    return;
                case "&eCambiar probabilidad":
                    event.setWillDestroy(true);
                    player.sendMessage(plugin.helpPrefix + ChatColor.WHITE + "Ingrese el nuevo valor de probabilidad (Puede llevar coma):");
                    player.sendMessage(plugin.helpPrefix + ChatColor.GRAY + "Para salir/cancelar escriba \"salir\"");
                    new SpecificPlayerChatListener(plugin, player, listener -> {
                        String mensaje = listener.getMessage();
                        if(mensaje.matches("[0-9]*[.,]?[0-9]*")){
                            mensaje = mensaje.replace(',','.');
                            customLootTable.setItemProbability(itemPosition,Double.parseDouble(mensaje));
                            Bukkit.getScheduler().runTask(plugin, () -> (new EditLootTableItemEditorGUI(plugin, player,lootTable,itemPosition,guiAnterior)).buildAndOpenGUI());
                            listener.setWillStop(true);
                        }else if (listener.getMessage().equalsIgnoreCase("salir")){
                            player.sendMessage(ChatColor.RED + "Saliendo del editor.");
                            listener.setWillStop(true);
                            (new EditLootTableItemEditorGUI(plugin, player,lootTable,itemPosition,guiAnterior)).buildAndOpenGUI();
                        }else{
                            player.sendMessage(ChatColor.RED + "Ingrese un número válido!");
                        }
                    });
                    return;
                case "&9Volver":
                    event.setWillDestroy(true);
                    delayEntreMenues(guiAnterior);
                    break;
            }

        }, plugin)
                .setOption(4, new ItemStack(Material.TNT, 1), "&cEliminar item", Collections.singletonList(ChatColor.DARK_RED + "Esta acción no se puede deshacer!"))
                .setOption(20, new ItemStack(Material.COMPARATOR, 1), "&eCambiar cantidad", Arrays.asList("",ChatColor.WHITE + "Mínimo actual: " + ChatColor.GREEN+customLootTable.getItemMin(itemPosition),ChatColor.WHITE + "Máximo actual: " + ChatColor.GREEN + customLootTable.getItemMax(itemPosition), "", ChatColor.GOLD + "Mínimo:" + ChatColor.GRAY + " Cantidad de este item que va a dar si o si.", ChatColor.GOLD + "Máximo:" + ChatColor.GRAY + " Cantidad máxima que puede llegar a darte",ChatColor.GRAY + "de este item (Limita al mínimo)."))
                .setOption(24, new ItemStack(Material.GLOWSTONE_DUST, 1), "&eCambiar probabilidad", Arrays.asList("",ChatColor.WHITE + "Probabilidad actual: "+ ChatColor.GREEN + customLootTable.getItemProbability(itemPosition) + "%","", ChatColor.GRAY + "Probabilidad de que salga 1 de este item."))
                .setOption(40, new ItemStack(Material.ARROW, 1), "&9Volver", null);
        if(item.getItemMeta() != null){
            chestMenu.setOption(22, item, item.getItemMeta().getDisplayName(), item.getItemMeta().getLore());
        }else{
            chestMenu.setOption(22, item, item.getType().name(), null);
        }
        chestMenu.setSpecificTo(player);
        chestMenu.open(player);
    }
}
