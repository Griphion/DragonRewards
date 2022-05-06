package me.griphion.DragonRewards.GUI.ChatGUI;

import me.griphion.DragonRewards.Core;
import org.bukkit.command.CommandSender;

public abstract class ChatGUI {
    Core plugin;
    CommandSender sender;

    public ChatGUI(Core plugin, CommandSender sender){
        this.plugin = plugin;
        this.sender = sender;
    }
    public abstract void buildAndOpenGUI();
}
