package me.griphion.DragonRewards;

import me.griphion.DragonRewards.ConfigFiles.DragonsConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangManager {

    private final String configPathPrefix = "mensajes.";
    private String pluginPrefix;

    LangManager() {
        pluginPrefix = getTranslationPrefix();
    }


    private String getTranslationPrefix() {
        return agregarTodosLosColores(Core.config.getString(configPathPrefix + "plugin-prefix", "&7[&5DragonRewards&7]"));
    }

    public void actualizarPrefijoPlugin(){
        pluginPrefix = getTranslationPrefix();
    }

    public String[] getConfigTranslation(String path) {
        return deListaAArray(getConfigTranslationList(path));
    }

    private String[] deListaAArray(List<String> lista){
        String[] translation = new String[lista.size()];
        for(int i = 0; i < lista.size();i++){
            translation[i] = lista.get(i);
        }
        return translation;
    }

    public String getConfigTranslationString(String path) {
        if (path == null) {
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo obtener la traducción (Path NULL).");
            return null;
        }
        String retorno;
        switch (path) {
            case "recordatorio-dragon-mensaje-respawn":
                retorno = Core.config.getString(configPathPrefix + path,"&7Un dragón en &6{mundo} &7va aparecer el: &6{fechaRespawn}");
                break;
            case "recordatorio-dragon-mensaje-vivo":
                retorno = Core.config.getString(configPathPrefix + path, "{dragon} &festá vivo o invocándose en &6{mundo}");
                break;
            default:
                retorno = "--Error: No existe traducción para: '" + path + "'--";
        }
        return agregarTodosLosColores(retorno).replace("{pluginPrefix}", pluginPrefix);
    }

    public List<String> getConfigTranslationList(String path) {
        if (path == null) {
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo obtener la traducción (Path NULL).");
            return null;
        }
        List<String> retorno;

        switch (path) {
            case "recordatorio-recompensas-mensaje":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&fTienes &6&l{cantidad} &frecompensa/s para reclamar! Use: \"/dragonr reclamar\" para reclamar una)");
                }
                break;
            case "no-permission":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("&cNo tienes permiso para ejecutar ese comando!");
                }
                break;
            case "not-console-command":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("&cNo se puede usar este comando desde la consola!");
                }
                break;
            case "reclamar-success":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&aRecompensa reclamada con éxito!");
                }
                break;
            case "reclamar-left":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&fTe quedan &6&l{cantidad} &frecompensas más para reclamar!");
                }
                break;
            case "reclamar-1-left":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&fTe queda &6&l1 &frecompensa más para reclamar!");
                }
                break;
            case "reclamar-0-left":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&cNo tienes mas recompensas para reclamar!");
                }
                break;
            case "dragon-kill-lasthit-not-found":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("&7Alguien (De una forma no convencional) ha dado el último golpe a {dragon} &7y ha ayudado: {assist}");
                }
                break;
            case "dragon-kill-not-conventional":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{ultimoGolpe} &7ha matado solo a {dragon} &7de una forma no convencional.");
                }
                break;
            case "dragon-kill-unknown-death":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("&7Ha muerto {dragon} &7en circunstancias misteriosas... ¯\\_(o_O)_/¯");
                }
                break;
            case "dragon-admin-kill":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("&7Un administrador le disparo un rayo laser a {dragon} &7y murió incinerado");
                }
                break;
            case "lootTable-no-space":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&cNo tienes espacio en el inventario!");
                }
                break;
            case "lootTable-error-1":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&cHa ocurrido un problema con la recompensa, por favor contáctese con algún administrador! (Error: 1)");
                }
                break;
            case "lootTable-error-2":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&cHa ocurrido un problema con la recompensa, por favor contáctese con algún administrador! (Error: 2)");
                }
                break;
            case "lootTable-error-3":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&cHa ocurrido un problema con la recompensa, por favor contáctese con algún administrador! (Error: 3)");
                }
                break;
            case "lootTable-reclamar":
                retorno = Core.config.getStringList(configPathPrefix + path);
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}{color:#ffff00}Has recibido una recompensa! (Reclámela con /dragonr reclamar)");
                }
                break;
            default:
                retorno = Collections.singletonList("--Error: No existe traducción para: '" + path + "'--");
        }
        List<String> auxList = new ArrayList<>();
        for (String s : retorno) {
            auxList.add(agregarTodosLosColores(s).replace("{pluginPrefix}", pluginPrefix));
        }

        return auxList;
    }
    /*
    public String[] getDragonTranslation(String path, String dragonFileName) {
        return (String[]) getDragonTranslationList(path, dragonFileName).toArray();
    }
     */
    public List<String> getDragonTranslationList(final String path, final String dragonFileName) {
        if (path == null) {
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo obtener la traducción (Path NULL).");
            return null;
        }
        if (dragonFileName == null) {
            Bukkit.getLogger().log(Level.SEVERE, "No se pudo obtener la traducción (DragonFileName NULL).");
            return null;
        }

        DragonsConfig config = new DragonsConfig(dragonFileName);
        List<String> retorno;
        switch (path) {
            case "dragon-appear":
                retorno = config.getDragonAppearMessage();
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{pluginPrefix}&d¡Ha aparecido {dragon}&d!");
                }
                break;
            case "dragon-kill-message":
                retorno = config.getDragonKillMessage();
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{ultimoGolpe} &7ha dado el último golpe a {dragon} &7y ha ayudado: {assist}");
                }
                break;
            case "dragon-kill-message-solo":
                retorno = config.getDragonKillMessageSolo();
                if (retorno.isEmpty()) {
                    retorno = Collections.singletonList("{ultimoGolpe} &7ha matado solo a {dragon}&7!");
                }
                break;
            default:
                retorno = Collections.singletonList("--Error: No existe traducción para: '" + path + "'--");
        }
        List<String> auxList = new ArrayList<>();
        for (String s : retorno) {
            auxList.add(agregarTodosLosColores(s).replace("{pluginPrefix}", pluginPrefix));
        }

        return auxList;
    }

    public static String agregarColoresHex(String cadena) {
        Pattern pat = Pattern.compile("\\{color:(?<HEX>.{7})}");
        Matcher matcher = pat.matcher(cadena);
        String next;
        String replace;
        String color;
        while (matcher.find()) {
            replace = matcher.group();
            next = matcher.group("HEX");
            color = ChatColor.of(next).toString();
            cadena = cadena.replace(replace, color);
        }
        return cadena;
    }

    public static String agregarTodosLosColores(String cadena){
        return ChatColor.translateAlternateColorCodes('&', agregarColoresHex(cadena));
    }


    public String[] getTranslationWithPlayer(final String path, final Player player) {
        List<String> auxList = new ArrayList<>();
        for (String s : getConfigTranslationList(path)) {
            auxList.add(s.replace("{player}", player.getDisplayName()));
        }
        return deListaAArray(auxList);
    }

    public String[] getTranslationWithPlayer(final String path, final Player player, final int cantidad) {
        List<String> auxList = new ArrayList<>();
        for (String s : getConfigTranslationList(path)) {
            auxList.add(s.replace("{player}", player.getDisplayName()).replace("{cantidad}", String.valueOf(cantidad)));
        }
        return deListaAArray(auxList);
    }

    public String[] getTranslationWithDragon(final String path, final String dragonName, final String mundo) {
        List<String> auxList = new ArrayList<>();
        for (String s : getConfigTranslationList(path)) {
            auxList.add(s.replace("{dragon}", dragonName).replace("{mundo}", agregarTodosLosColores(Core.worldsConfig.getAlias(mundo))));
        }
        return deListaAArray(auxList);
    }

    public String getTranslationStringWithRespawnDate(final String path, final String fechaRespawn, final String mundo) {
        return getConfigTranslationString(path).replace("{mundo}", agregarTodosLosColores(Core.worldsConfig.getAlias(mundo))).replace("{fechaRespawn}", fechaRespawn);
    }

    public String getTranslationStringWithDragon(final String path, final String dragonName, final String mundo) {
        return getConfigTranslationString(path).replace("{dragon}", dragonName).replace("{mundo}", agregarTodosLosColores(Core.worldsConfig.getAlias(mundo)));
    }

    public String[] getTranslationWithDragon(final String path, final String dragonName, final String ultimoGolpe, final String asistencia, final String mundo) {
        List<String> auxList = new ArrayList<>();
        for (String s : getConfigTranslationList(path)) {
            auxList.add(s.replace("{dragon}", dragonName).replace("{ultimoGolpe}", ultimoGolpe).replace("{assist}", asistencia).replace("{mundo}", agregarTodosLosColores(Core.worldsConfig.getAlias(mundo))));
        }
        return deListaAArray(auxList);
    }

    public String[] getDragonTranslationWithDragon(final String path, final String dragonName, final String dragonFileName, final String mundo) {
        List<String> auxList = new ArrayList<>();
        for (String s : getDragonTranslationList(path,dragonFileName)) {
            auxList.add(s.replace("{dragon}", dragonName).replace("{mundo}", agregarTodosLosColores(Core.worldsConfig.getAlias(mundo))));
        }
        return deListaAArray(auxList);
    }

    public String[] getDragonTranslationWithDragon(final String path, final String dragonName, final String dragonFileName, final String ultimoGolpe, final String asistencia, final String mundo) {
        List<String> auxList = new ArrayList<>();
        for (String s : getDragonTranslationList(path,dragonFileName)) {
            auxList.add(s.replace("{dragon}", dragonName).replace("{ultimoGolpe}", ultimoGolpe).replace("{assist}", asistencia).replace("{mundo}", agregarTodosLosColores(Core.worldsConfig.getAlias(mundo))));
        }
        return deListaAArray(auxList);
    }
}