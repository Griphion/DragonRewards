package me.griphion.DragonRewards.ConfigFiles;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class DragonsConfig{

    public String getNombreDragon() {
        return getConfig().getString(DragonsConfig.NOMBRE_DRAGON, null);
    }

    public String getRecompensaUltimoGolpe() {
        return getConfig().getString(DragonsConfig.RECOMPENSA_ULTIMO_GOLPE);
    }

    public String getRecompensaAsistencia() {
        return getConfig().getString(DragonsConfig.RECOMPENSA_ASISTENCIA);
    }

    public double getVidaMaxima() {
        return getConfig().getDouble(DragonsConfig.VIDA_MAXIMA,200);
    }

    public double getARMADURA() {
        return getConfig().getDouble(DragonsConfig.ARMADURA,0);
    }

    public double getDANIO() {
        return getConfig().getDouble(DragonsConfig.DANIO,15);
    }

    public String getBossBarStyle() {
        return getConfig().getString(DragonsConfig.BOSS_BAR_STYLE,"SOLID");
    }

    public String getBossBarColor() {
        return getConfig().getString(DragonsConfig.BOSS_BAR_COLOR,"PINK");
    }

    public List<String> getDragonAppearMessage() {
        return getConfig().getStringList(DragonsConfig.DRAGON_APPEAR);
    }

    public List<String> getDragonKillMessage() {
        return getConfig().getStringList(DragonsConfig.DRAGON_KILL_MESSAGE);
    }

    public List<String> getDragonKillMessageSolo() {
        return getConfig().getStringList(DragonsConfig.DRAGON_KILL_MESSAGE_SOLO);
    }

    public List<String> getOnDeathCommand() {
        return getConfig().getStringList(DragonsConfig.ON_DEATH_COMMAND);
    }

    public List<String> getOnAppearCommand() {
        return getConfig().getStringList(DragonsConfig.ON_APPEAR_COMMAND);
    }

    public List<String> getDragonActivePowers() {
        return getConfig().getStringList(DragonsConfig.DRAGON_ACTIVE_POWERS);
    }

    public List<String> getDragonPassivePowers() {
        return getConfig().getStringList(DragonsConfig.DRAGON_PASSIVE_POWERS);
    }

    public int getDragonActivePowerCD(){
        return getConfig().getInt(DragonsConfig.DRAGON_ACTIVE_POWER_CD,2);
    }

    public int getDragonActivePowerAutoCastTime(){
        return getConfig().getInt(DragonsConfig.DRAGON_ACTIVE_POWER_AUTO_CAST_TIME,15);
    }

    public static final String NOMBRE_DRAGON = "nombre-dragon";
    public static final String RECOMPENSA_ULTIMO_GOLPE = "recompensa-al-ultimo-golpe";
    public static final String RECOMPENSA_ASISTENCIA = "recompensa-asistencia";
    public static final String ATRIBUTOS = "atributos";
    public static final String VIDA_MAXIMA = ATRIBUTOS + ".vida-maxima";
    public static final String ARMADURA = ATRIBUTOS + ".armadura";
    public static final String DANIO = ATRIBUTOS + ".daño";
    public static final String BOSS_BAR_STYLE = "boss-bar-style";
    public static final String BOSS_BAR_COLOR = "boss-bar-color";
    public static final String DRAGON_APPEAR = "dragon-appear";
    public static final String DRAGON_KILL_MESSAGE = "dragon-kill-message";
    public static final String DRAGON_KILL_MESSAGE_SOLO = "dragon-kill-message-solo";
    public static final String ON_DEATH_COMMAND = "on-death-command";
    public static final String ON_APPEAR_COMMAND = "on-appear-command";
    public static final String DRAGON_ACTIVE_POWERS = "dragon-active-powers";
    public static final String DRAGON_PASSIVE_POWERS = "dragon-passive-powers";
    public static final String DRAGON_ACTIVE_POWER_CD = "dragon-active-power-cd";
    public static final String DRAGON_ACTIVE_POWER_AUTO_CAST_TIME = "dragon-active-power-auto-cast-time";

    private final ConfigFileManager config;

    public DragonsConfig(String fileName) {
        this.config = new ConfigFileManager(fileName, ConfigFileManager.directoryEnum.DRAGON);
        config.getConfig().addDefault(NOMBRE_DRAGON, fileName);
        fillDefault();
    }

    private void fillDefault(){
        config.getConfig().addDefault(VIDA_MAXIMA, 200.0);
        config.getConfig().addDefault(ARMADURA,0.0);
        config.getConfig().addDefault(DANIO,15.0);
        config.getConfig().addDefault(RECOMPENSA_ULTIMO_GOLPE, "default_lasthit_loot");
        config.getConfig().addDefault(RECOMPENSA_ASISTENCIA, "default_assist_loot");
        config.getConfig().addDefault(DRAGON_APPEAR, Collections.singletonList("{pluginPrefix}&d¡Ha aparecido {dragon}&d!"));
        config.getConfig().addDefault(DRAGON_KILL_MESSAGE,Collections.singletonList("{ultimoGolpe} &7ha dado el último golpe a {dragon} &7y ha ayudado: {assist}"));
        config.getConfig().addDefault(DRAGON_KILL_MESSAGE_SOLO,Collections.singletonList("{ultimoGolpe} &7ha matado solo a {dragon}&7!"));
        config.getConfig().addDefault(BOSS_BAR_STYLE,"SOLID");
        config.getConfig().addDefault(BOSS_BAR_COLOR,"PURPLE");
        config.getConfig().addDefault(ON_APPEAR_COMMAND,Collections.singletonList(""));
        config.getConfig().addDefault(ON_DEATH_COMMAND,Collections.singletonList(""));
        config.getConfig().addDefault(DRAGON_ACTIVE_POWER_CD,2);
        config.getConfig().addDefault(DRAGON_ACTIVE_POWER_AUTO_CAST_TIME,10);
        config.getConfig().addDefault(DRAGON_ACTIVE_POWERS,Collections.singletonList(""));
        config.getConfig().addDefault(DRAGON_PASSIVE_POWERS,Collections.singletonList(""));
        saveCopyDefaults();
    }



    public void saveConfig(){
        config.saveConfig();
    }
    public void saveCopyDefaults(){
        config.saveCopyDefaults();
    }
    public void delete(){ config.deleteFile();}
    public FileConfiguration getConfig(){
        return config.getConfig();
    }
    public void reloadConfig(){
        config.reloadConfig();
    }
}
