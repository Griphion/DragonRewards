package me.griphion.DragonRewards.Utils;


import me.griphion.DragonRewards.Core;
import org.bukkit.Bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class CustomLogger {

    public static void log(String message) {
        if(!Core.plugin.getConfig().getBoolean("log",true)) return;
        File file = new File(Core.plugin.getDataFolder(), "log.txt");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "] " + message);
            bw.newLine();
            bw.close();
        }catch(Exception e){
            Bukkit.getLogger().log(Level.WARNING,"Error al querer escribir el archivo 'log.txt'!", e);
        }

    }

}
