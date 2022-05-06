package me.griphion.DragonRewards.ConfigFiles;

import me.griphion.DragonRewards.Core;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigFileManager {

    private final String fileName;
    private FileConfiguration fileConfiguration = null;
    private File configFile = null;

    public static final Map<directoryEnum,String> directoryPath;
    static {
        directoryPath = new HashMap<>();
        directoryPath.put(directoryEnum.NONE, "");
        directoryPath.put(directoryEnum.DRAGON,"/Dragones");
        directoryPath.put(directoryEnum.LOOTTABLE,"/LootTables");
    }
    private final directoryEnum directory;
    public enum directoryEnum{
        NONE,
        DRAGON,
        LOOTTABLE
    }

    public ConfigFileManager(String fileName, directoryEnum directory){
        this.fileName = fileName;
        this.directory = directory;
        saveDefaultConfig();
    }

    public void reloadConfig(){
        if(this.configFile == null) this.configFile = new File(Core.plugin.getDataFolder() + directoryPath.get(directory), fileName + ".yml");

        fileConfiguration = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultStream = Core.plugin.getResource(fileName + ".yml");
        if(defaultStream != null){
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.fileConfiguration.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig(){
        if(this.fileConfiguration == null) reloadConfig();
        return this.fileConfiguration;
    }

    public void saveConfig(){
        if(this.fileConfiguration == null || this.configFile == null) return;
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            Core.plugin.getLogger().log(Level.SEVERE, "No se pudo guardar la Config de " + this.configFile, e);
        }
    }

    public void saveDefaultConfig(){
        if(this.configFile == null){
            this.configFile = new File(Core.plugin.getDataFolder() + directoryPath.get(directory), fileName + ".yml");
        }
        if(!this.configFile.exists()){
            try{
                if(configFile.createNewFile()){
                    Core.plugin.getLogger().log(Level.INFO, "Se ha creado el archivo de configuración: <" + this.configFile + "> exitosamente.");
                }
            }catch (Exception e){
                Core.plugin.getLogger().log(Level.SEVERE, "No se pudo crear el archivo de configuración: " + this.configFile, e);
            }
        }
    }

    public void saveCopyDefaults(){
        fileConfiguration.options().copyDefaults(true);
        saveConfig();
    }

    public static void createConfigDirectories(){
        for(String elem : directoryPath.values()){
            if(!elem.equals("")){
                if(new File(Core.plugin.getDataFolder() + elem).mkdirs())
                    Bukkit.getLogger().log(Level.INFO, "Se han creado los directorios base exitosamente!");
            }
        }
    }

    public void deleteFile() {
        if(!configFile.delete()){
            Bukkit.getLogger().log(Level.INFO, "No se pudo eliminar el archivo: '" + fileName + "'. Este se eliminará cuando se cierre el servidor.");
            configFile.deleteOnExit();
        }

    }

    public boolean isEmpty() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Core.plugin.getDataFolder() + directoryPath.get(directory) + "/" +fileName+".yml"));
            return br.readLine() == null;
        }catch (IOException e){
            Bukkit.getLogger().log(Level.SEVERE, "Error al leer el archivo: " + this.fileName, e);
            return true;
        }

    }

    public static void loadConfigFromJar(final String config, final String path) {

        File configFile = new File(Core.plugin.getDataFolder() + path, config);

        if (!configFile.exists()) {

            try (InputStream fis = Core.plugin.getClass().getResourceAsStream(path + "/" + config) ; FileOutputStream fos = new FileOutputStream(configFile)) {
                byte[] buf = new byte[1024];
                int i;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                Core.plugin.getServer().getLogger().log(Level.SEVERE, "Error al cargar la config del JAR!", e);
            }

        }

    }

}
