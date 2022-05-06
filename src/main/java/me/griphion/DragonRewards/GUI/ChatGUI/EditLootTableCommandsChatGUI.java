package me.griphion.DragonRewards.GUI.ChatGUI;

import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.Core;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;

public class EditLootTableCommandsChatGUI extends ChatGUI {

    String lootTable;
    public EditLootTableCommandsChatGUI(Core plugin, CommandSender sender, String lootTable) {
        super(plugin, sender);
        this.lootTable = lootTable;
    }

    @Override
    public void buildAndOpenGUI() {
        CustomLootTable customLootTable = new CustomLootTable(lootTable);
        sender.sendMessage(Core.separador);
        sender.sendMessage(plugin.helpPrefix + ChatColor.GOLD + "Agrega o elimina comandos");
        sender.sendMessage(plugin.helpPrefix + " -  El comando debe empezar con [consola] o [usuario] para hacer referencia a quien lo ejecuta.");
        sender.sendMessage(plugin.helpPrefix + " -  Se puede usar {nombre} y {mundo} para hacer referencia al usuario que reclamo la recompensa y al mundo donde se encuentra.");
        ComponentBuilder deleteBuilder = new ComponentBuilder("[").color(ChatColor.DARK_RED).append("-").color(ChatColor.RED).append("]").color(ChatColor.DARK_RED);
        TextComponent deleteButton;
        TextComponent message;
        int i = 0;
        for (String command : customLootTable.getComandos()){
            message = new TextComponent();
            deleteButton = new TextComponent(deleteBuilder.create());
            deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED +"Haz click para eliminar el comando (" + i + ")")));
            deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/dragonr editloot " + lootTable  + " remcomando " + i));
            message.addExtra(deleteButton);
            message.addExtra(ChatColor.YELLOW + " (" + i + "): " + ChatColor.WHITE + command);
            sender.spigot().sendMessage(message);
            i++;
        }
        ComponentBuilder addCommandBuilder = new ComponentBuilder("[").color(ChatColor.DARK_GREEN).append("+").color(ChatColor.GREEN).append("]").color(ChatColor.DARK_GREEN);
        TextComponent addCommandButton = new TextComponent(addCommandBuilder.create());
        addCommandButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Haz click para agregar un comando")));
        addCommandButton.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/dragonr editloot " + lootTable  + " addcomando "));
        sender.spigot().sendMessage(addCommandButton);
        sender.sendMessage(Core.separador);
    }
}
