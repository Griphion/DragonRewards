package me.griphion.DragonRewards;

import me.griphion.DragonRewards.ConfigFiles.ConfigFileManager;
import me.griphion.DragonRewards.ConfigFiles.DragonsConfig;
import me.griphion.DragonRewards.ConfigFiles.LootTable.CustomLootTable;
import me.griphion.DragonRewards.ConfigFiles.PlayerDataConfig;
import me.griphion.DragonRewards.ConfigFiles.WorldsConfig;
import me.griphion.DragonRewards.DragonPackage.Dragon;
import me.griphion.DragonRewards.DragonPackage.DragonManager;
import me.griphion.DragonRewards.DragonPackage.DragonScheduler;
import me.griphion.DragonRewards.DragonPackage.HologramManager;
import me.griphion.DragonRewards.GUI.ChatGUI.EditLootTableCommandsChatGUI;
import me.griphion.DragonRewards.GUI.EditDragonListGUI;
import me.griphion.DragonRewards.GUI.EditLootTableOptionsGUI;
import me.griphion.DragonRewards.GUI.EditLootTablesListGUI;
import me.griphion.DragonRewards.GUI.RewardsOptionsGUI;
import me.griphion.DragonRewards.Utils.EconomyManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

//TODO Hacer que te permita mas de una CustomLootTable por dragon?
//TODO Recompensas basadas en daño?

//TODO Crear una GUI para agregar/quitar poderes a los dragones y modificar los stats del mismo

public class Core extends JavaPlugin {

    public final String pluginPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_PURPLE +"DragonRewards" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
    static final String barraColor = ChatColor.of(new Color(255, 3, 255))  + "="
                    + ChatColor.of(new Color(204, 2, 204))  + "="
                    + ChatColor.of(new Color(153, 1, 153))  + "="
                    + ChatColor.of(new Color(102, 1, 102))  + "="
                    + ChatColor.of(new Color(51, 0, 51))    + "="
                    + ChatColor.of(new Color(102, 1, 102))  + "="
                    + ChatColor.of(new Color(153, 1, 153))  + "="
                    + ChatColor.of(new Color(204, 2, 204))  + "="
                    + ChatColor.of(new Color(255, 3, 255))  + "=";

    static public final String pluginTitle = ChatColor.of(new Color(255, 3, 255)) + "|" + barraColor + "|" + ChatColor.of(new Color(204, 2, 204)) +" - DragonRewards -  " + ChatColor.of(new Color(255, 3, 255)) +"|" + barraColor + "|";
    static public final String separador = ChatColor.of(new Color(255, 3, 255)) + "|" + barraColor + "=" + barraColor + barraColor + barraColor + "|";
    static public final ChatColor grayColor = ChatColor.of(new Color(160, 160, 160));
    public final String helpPrefix = ChatColor.of(new Color(102, 1, 102)) + ">" + ChatColor.of(new Color(204, 2, 204)) + "> " + grayColor;
    public final String needWorldName = pluginPrefix + ChatColor.RED + "Necesitas escribir el nombre de un mundo end!";
    public final String needDragonName = pluginPrefix + ChatColor.RED + "Necesitas escribir el nombre de un dragón!";
    public final String needLootTable = pluginPrefix + ChatColor.RED + "Necesitas escribir el nombre de una LootTable!";
    public final String needUserName = pluginPrefix + ChatColor.RED + "Necesitas escribir el nombre de un jugador!";


    static public List<DragonNameAndFileName> dragonesDisponibles = new ArrayList<>();
    static public List<String> lootTablesDisponibles = new ArrayList<>();
    static public List<Player> usuariosConGuiAbierta = new ArrayList<>();
    static public HashMap<World,Dragon> dragonesActivos = new HashMap<>(); //World,Dragon instance
    static public HashMap<UUID,ItemStack[]> recompensasEnEspera = new HashMap<>();

    public PlayerDataConfig playerDataConfig;
    public DragonManager dragonManager;
    public DragonScheduler dragonScheduler;
    public HologramManager hologramManager;
    static public EconomyManager economyManager;
    static public WorldsConfig worldsConfig;
    static public LangManager langManager;
    static public FileConfiguration config;
    static public Core plugin;

    static public boolean useHolographicDisplays;
    static public boolean useVault;

    public static class DragonNameAndFileName{
        private String dragonName,dragonFileName;
        public String getDragonFileName() {
            return dragonFileName;
        }
        public String getDragonName() {
            return dragonName;
        }
        public void setDragonFileName(String dragonFileName) {
            this.dragonFileName = dragonFileName;
        }
        public void setDragonName(String dragonName) {
            this.dragonName = ChatColor.stripColor(dragonName);
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();
        config = this.getConfig();

        ConfigFileManager.createConfigDirectories();
        generateDefaultConfigFiles();

        /* Creación configs, cargado de mundos, LootTables, dragones y PlayerData*/
        worldsConfig = new WorldsConfig(this);
        worldsConfig.scannerDeMundos(false);
        playerDataConfig = new PlayerDataConfig();
        this.scannerDeLootTables();
        this.scannerDeDragones();
        /* --------------------------------------------------------------------- */

        this.dragonManager = new DragonManager(this);
        langManager = new LangManager();
        this.dragonScheduler = new DragonScheduler(this);
        this.dragonScheduler.searchScheduledDragons();
        economyManager = new EconomyManager(this);

        dragonManager.detectDragonsByPlayersWorld();

        Bukkit.getServer().getPluginManager().registerEvents(this.dragonManager, this);

        Objects.requireNonNull(getCommand("dragonrewards")).setExecutor(this);
        Objects.requireNonNull(getCommand("dragonrewards")).setTabCompleter(new CommandTab());

        if(getConfig().getBoolean("use-holographicdisplays")){
            /* Start HolographicDisplays */
            useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
            if (!useHolographicDisplays) {
                getLogger().severe("*** HolographicDisplays no esta activado o no esta instalado! ***");
                getLogger().severe("*** No se va a poder usar hologramas. ***");
            } else {
                hologramManager = new HologramManager(this);
            }
            /* ------------------------ */
        }else {
            useHolographicDisplays = false;
        }

        if(getConfig().getBoolean("use-vault")){
            useVault = economyManager.setupEconomy();
            if(!useVault){
                getLogger().severe("*-* Necesitas tener instalado Vault y un plugin de Economía! *-*");
                getLogger().severe("*-* No se va a poder dar recompensas económicas. *-*");
            }
        }else {
            useVault = false;
        }

        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GREEN + "Plugin activado!");
    }

    @Override
    public void onDisable() {
        dragonManager.detectNotSummonedDragonsOnPluginUnload();
        Bukkit.getServer().getScheduler().cancelTasks(this);
        /* ---- Nulling ---- */
        worldsConfig.destroy();
        if(dragonesActivos != null){
            for (Dragon dragon:dragonesActivos.values()){
                if(dragon != null)
                    dragon.destroy(false);
            }
            dragonesActivos.clear();
            dragonesActivos = null;
        }
        if(usuariosConGuiAbierta != null){
            for(Player player : usuariosConGuiAbierta) player.closeInventory();
            usuariosConGuiAbierta.clear();
            usuariosConGuiAbierta = null;
        }
        if(dragonesDisponibles != null){
            dragonesDisponibles.clear();
            dragonesDisponibles = null;
        }
        if(lootTablesDisponibles != null){
            lootTablesDisponibles.clear();
            lootTablesDisponibles = null;
        }
        if(dragonManager != null){
            dragonManager.destroy();
            dragonManager = null;
        }
        if(hologramManager != null){
            hologramManager.clearHolograms();
            hologramManager = null;
        }
        if(recompensasEnEspera != null){
            recompensasEnEspera.clear();
            recompensasEnEspera = null;
        }
        langManager = null;
        worldsConfig = null;
        dragonScheduler = null;
        economyManager = null;
        config = null;
        plugin = null;
        /* ----------------- */

        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.RED + "Plugin desactivado!");
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, @NotNull String[] args) {
        if(label.equalsIgnoreCase("dragonrewards") || label.equalsIgnoreCase("dragonr") || label.equalsIgnoreCase("dragonrewards:dragonrewards") || label.equalsIgnoreCase("dragonrewards:dragonr")) {
            if (args.length == 0) {
                sender.sendMessage(pluginPrefix + ChatColor.WHITE + "Desarrollado por " + ChatColor.RED + "Griphion");
                return true;
            }
            Player player;
            UUID uuid;
            switch (args[0]){
                case "top":
                    if(noTieneElPermiso("dragonrewards.top",sender)) return true;
                    if(args.length < 2){
                        sender.sendMessage(grayColor + "Uso: '/dragonr top kill' o '/dragonr top assist'");
                        return true;
                    }
                    switch (args[1]){
                        case "kill":
                            if(noTieneElPermiso("dragonrewards.top.kill",sender)) return true;
                            sender.sendMessage(barraColor + " -<>- Top 10 Ultimos Golpes -<>- " + barraColor);
                            playerDataConfig.sendTop10Kills(sender);
                            break;
                        case "assist":
                            if(noTieneElPermiso("dragonrewards.top.assist",sender)) return true;
                            sender.sendMessage(barraColor + " -<>- Top 10 Asistencias -<>- " + barraColor);
                            playerDataConfig.sendTop10Assist(sender);
                            break;
                        default:
                            sender.sendMessage(grayColor + "Uso: '/dragonr top kill' o '/dragonr top assist'");
                            break;
                    }


                    return true;
                //dragonr reclamar
                case "reclamar":
                    if(noTieneElPermiso("dragonrewards.reclamar",sender)) return true;
                    if(!(sender instanceof Player)){
                        sender.sendMessage(langManager.getConfigTranslation("not-console-command"));
                        return true;
                    }
                    player = (Player) sender;
                    if(playerDataConfig.tieneLootDisponible(player.getUniqueId())){
                        String lootTableName = playerDataConfig.obtenerUnaLootTableYSacarla(player.getUniqueId());
                        if(!lootTablesDisponibles.contains(lootTableName)){ //Si no existe la loot table.
                            playerDataConfig.agregarLootAJugador(lootTableName,player,false);
                            player.sendMessage(langManager.getConfigTranslation("lootTable-error-3"));
                            Bukkit.getLogger().log(Level.SEVERE, "Error: Se quiso recompensar con la LootTable: '" + lootTableName + "' pero esta no existe o no esta disponible!");
                            return true;
                        }
                        CustomLootTable customLootTable = new CustomLootTable(lootTableName);
                        if(!customLootTable.darLootTable(player)){ //Si no se le puede dar la loot table por alguna razón.
                            playerDataConfig.agregarLootAJugador(lootTableName,player,false);
                            return true;
                        }else { //Si se pudo dar la loot table.
                            customLootTable.ejecutarComandos(player);
                            customLootTable.darExperiencia(player);
                            customLootTable.darDinero(player);
                        }
                        player.sendMessage(langManager.getConfigTranslation("reclamar-success"));
                        if(playerDataConfig.tieneLootDisponible(player.getUniqueId())){
                            int cantidad = playerDataConfig.obtenerListaDeLoot(player.getUniqueId()).size();
                            if(cantidad > 1){
                                player.sendMessage(langManager.getTranslationWithPlayer("reclamar-left",player,cantidad));
                            }else {
                                player.sendMessage(langManager.getTranslationWithPlayer("reclamar-1-left",player,cantidad));
                            }
                        }
                    }else {
                        player.sendMessage(langManager.getTranslationWithPlayer("reclamar-0-left",player));
                    }
                    return true;

                case "dragones":
                    if(noTieneElPermiso("dragonrewards.dragones",sender)) return true;
                    dragonManager.enviarMensajeDragonesVivosYRespawn(sender);
                    return true;

                case "stats":
                    if(noTieneElPermiso("dragonrewards.stats",sender)) return true;
                    if(args.length > 1 && !sender.hasPermission("dragonrewards.stats.otros")){
                        sender.sendMessage(langManager.getConfigTranslation("no-permission"));
                        return true;
                    }

                    if(!(sender instanceof Player)){ // Si es la consola
                        if(args.length == 1){
                            sender.sendMessage(needUserName);
                            return true;
                        }
                    }

                    if(args.length > 1){
                        player = Bukkit.getPlayer(args[1]);
                        if(player == null){
                            uuid = playerDataConfig.getUUID(args[1]);
                            if(uuid == null){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "No se ha podido encontrar al jugador!");
                                return true;
                            }
                        }else {
                            uuid = player.getUniqueId();
                        }
                    }else {
                        uuid = ((Player) sender).getUniqueId();
                    }

                    sender.sendMessage(separador);
                    if(args.length > 1)
                        sender.sendMessage(  ChatColor.BOLD + " " +ChatColor.GRAY + "----" + ChatColor.GRAY + " Stats de " + ChatColor.GOLD + args[1] + " " + ChatColor.BOLD + " " +ChatColor.GRAY + "----");
                    sender.sendMessage(grayColor + "Ultimos golpes: " + ChatColor.WHITE + playerDataConfig.getUltimoGolpe(uuid));
                    sender.sendMessage(grayColor + "Asistencias: " + ChatColor.WHITE + playerDataConfig.getAssist(uuid));
                    sender.sendMessage(grayColor + "Recompensas sin reclamar: " + ChatColor.WHITE + playerDataConfig.obtenerListaDeLoot(uuid).size());
                    sender.sendMessage(separador);
                    return true;

                case "help":
                    if(noTieneElPermiso("dragonrewards.help",sender)) return true;
                    sender.sendMessage(pluginTitle);
                    sender.sendMessage(ChatColor.WHITE + "/dragonr recompensas <Dragon>");
                    sender.sendMessage(helpPrefix + "Te muestra las recompensas del dragón. (O abre el menú de recompensas de todos los dragones si no se escribe nada)");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr stats <Jugador>");
                    sender.sendMessage(helpPrefix + "Te muestra los stats de un jugador. (O los stats propios si no se escribe nada)");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr top [Kill/Assist]");
                    sender.sendMessage(helpPrefix + "Te muestra el top 10 de los Ultimos Golpes o de las Asistencias.");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr reload");
                    sender.sendMessage(helpPrefix + "Recarga la configuración. (También detecta nuevos mundos/dragones/lootTables que no estén cargados)");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr mundo <MundoEnd>");
                    sender.sendMessage(helpPrefix + "Te muestra la información de la configuración de el mundo. (Si no se escribe nada te muestra la del mundo actual)");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr admin");
                    sender.sendMessage(helpPrefix + "Te muestra los subcomandos de admin.");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr editdragon");
                    sender.sendMessage(helpPrefix + "Te muestra los subcomandos para editar los dragones.");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo");
                    sender.sendMessage(helpPrefix + "Te muestra los subcomandos para editar los mundos");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr editloot <LootTable>");
                    sender.sendMessage(helpPrefix + "Te lleva a la GUI de modificación de LootTable. (Si no se escribe nada te muestra todas las LootTable)");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr editloot [LootTable] addcomando [Comando]");
                    sender.sendMessage(helpPrefix + "Agrega un comando a la LootTable. (Para mas información leer la config)");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr editloot [LootTable] setexp [CantExp]");
                    sender.sendMessage(helpPrefix + "Establece la experiencia que te va a dar la LootTable. (Para mas información leer la config)");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr editloot [LootTable] setdinero [CantDinero]");
                    sender.sendMessage(helpPrefix + "Establece el dinero que te va a dar la LootTable.");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr loottable [LootTable]");
                    sender.sendMessage(helpPrefix + "Te muestra información de la LootTable.");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr reclamar");
                    sender.sendMessage(helpPrefix + "Reclama una recompensa en caso de tener alguna disponible.");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr dragones");
                    sender.sendMessage(helpPrefix + "Te muestra qué dragones están vivos y/o cuando re-spawnean.");
                    sender.sendMessage(ChatColor.WHITE + "/dragonr dragon [Dragon]");
                    sender.sendMessage(helpPrefix + "Te muestra información del dragón.");
                    sender.sendMessage(separador);
                    return true;

                case "dragon":
                    if(noTieneElPermiso("dragonrewards.dragon",sender)) return true;
                    if(args.length < 2){
                        sender.sendMessage(needDragonName);
                        return true;
                    }
                    if(noEsUnDragonFileNameValido(args[1],sender)) return true;

                    DragonsConfig dragonConfig = new DragonsConfig(args[1]);
                    sender.sendMessage(separador);
                    sender.sendMessage(ChatColor.GRAY + "}=={ }=={ "+ ChatColor.WHITE + args[1] + ChatColor.GRAY + " }=={ }=={");
                    sender.sendMessage(helpPrefix + "Nombre display del dragón: " + LangManager.agregarTodosLosColores(dragonConfig.getNombreDragon()));
                    sender.sendMessage(helpPrefix + "Recompensa UG: " + ChatColor.WHITE + dragonConfig.getRecompensaUltimoGolpe());
                    sender.sendMessage(helpPrefix + "Recompensa Asistencia: " + ChatColor.WHITE + dragonConfig.getRecompensaAsistencia());
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " -+- Atributos -+- ");
                    sender.sendMessage(helpPrefix + "Vida máxima: " + ChatColor.WHITE + dragonConfig.getVidaMaxima() + ChatColor.GRAY + " [" + ChatColor.WHITE + dragonConfig.getVidaMaxima()/2 + "x" +  ChatColor.RED + "❤" + ChatColor.GRAY + "]");
                    sender.sendMessage(helpPrefix + "Armadura: " + ChatColor.WHITE + dragonConfig.getARMADURA());
                    sender.sendMessage(helpPrefix + "Daño: " + ChatColor.WHITE + dragonConfig.getDANIO() + ChatColor.GRAY + " [" + ChatColor.WHITE + dragonConfig.getDANIO()/2 + "x" +  ChatColor.RED + "❤" + ChatColor.GRAY + "]");
                    sender.sendMessage(separador);

                    return true;
                case "reload":
                    if(noTieneElPermiso("dragonrewards.reload",sender)) return true;
                    scannerDeLootTables();
                    scannerDeDragones();
                    dragonManager.detectDragonsByPlayersWorld();
                    worldsConfig.scannerDeMundos(true);
                    reloadConfig();
                    config = getConfig();
                    worldsConfig.reloadConfig();
                    playerDataConfig.reloadConfig();
                    langManager.actualizarPrefijoPlugin();
                    sender.sendMessage(pluginPrefix + org.bukkit.ChatColor.GREEN + "Configuración recargada!");
                    return true;

                case "debug":
                    if(noTieneElPermiso("dragonrewards.admin.debug",sender)) return true;
                    sender.sendMessage("Mundos End: " + devolverSeparadoConComas(worldsConfig.mundosEndNames));
                    sender.sendMessage("LootTables Disponibles: " + devolverSeparadoConComas(lootTablesDisponibles));
                    sender.sendMessage("Dragones: ");
                    for (World key : dragonesActivos.keySet()){
                        sender.sendMessage("++ Mundo - '"+ key.getName() + "' Dragon - '" + ChatColor.stripColor(dragonesActivos.get(key).getName()) + "' UUID - '" + dragonesActivos.get(key).getUuid().toString() + "'");
                    }
                    sender.sendMessage("DragonesDisponibles: ");
                    for (DragonNameAndFileName dragon : dragonesDisponibles){
                        sender.sendMessage("-- DragonFileName - '"+ dragon.getDragonFileName() + "' DragonName - '" + dragon.getDragonName() + "'");
                    }
                    return true;

                // dragonr mundo <MundoEnd>
                case "mundo":
                    if(noTieneElPermiso("dragonrewards.mundo",sender)) return true;
                    World mundo;
                    if(args.length >=2){
                        if(worldsConfig.noEsUnMundoEndValido(args[1],sender)) return true;
                        mundo = Bukkit.getWorld(args[1]);

                    }else if(sender instanceof Player){
                        mundo = ((Player) sender).getWorld();
                        if(worldsConfig.noEsUnMundoEndValido(mundo)){
                            sender.sendMessage(pluginPrefix + ChatColor.RED + "El mundo actual no es un mundo válido!");
                            return true;
                        }
                    }else {
                        sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el nombre del mundo!");
                        return true;
                    }
                    if(mundo == null) return true;
                    sender.sendMessage(separador);
                    sender.sendMessage(ChatColor.GRAY + "--[ "+ ChatColor.WHITE + mundo.getName() + ChatColor.GRAY + " ]--");
                    if (worldsConfig.getActivo(mundo.getName())){
                        sender.sendMessage(helpPrefix + "Alias: " + LangManager.agregarTodosLosColores(worldsConfig.getAlias(mundo.getName())));
                        sender.sendMessage(helpPrefix + "El mundo esta: " + ChatColor.GREEN + "Activado");
                        if (worldsConfig.getRespawnOnDeath(mundo.getName())){
                            sender.sendMessage(helpPrefix + "Respawn de dragones: " + ChatColor.GREEN + "Activado");
                            if(worldsConfig.getConfig().contains(mundo.getName() + WorldsConfig.RESPAWN_DATE)){
                                sender.sendMessage(helpPrefix + "Fecha de respawn: " + ChatColor.WHITE + worldsConfig.getRespawnDate(mundo.getName()));
                            }else {
                                sender.sendMessage(helpPrefix + "Delay de respawn: " + ChatColor.WHITE + worldsConfig.getDeathRespawnDelay(mundo.getName()));
                            }
                        }else {
                            sender.sendMessage(helpPrefix + "Respawn de dragones: " + ChatColor.RED + "Desactivado");
                        }
                        Dragon dragon = dragonesActivos.get(mundo);
                        if(dragon != null){
                            sender.sendMessage(helpPrefix + "Dragon actual: " + dragon.getName());
                        }else {
                            sender.sendMessage(helpPrefix + "Dragon actual: " + ChatColor.RED +  "No hay ningún dragón vivo en este mundo!");
                        }
                        if(worldsConfig.getConfig().contains(mundo.getName() + WorldsConfig.DRAGONS)){
                            sender.sendMessage(helpPrefix + "Dragones configurados: " + devolverSeparadoConComas(worldsConfig.getDragons(mundo.getName())));
                        }else {
                            sender.sendMessage(helpPrefix + ChatColor.RED + "No hay dragones configurados!");
                        }
                        switch (worldsConfig.getMostrarAnuncioInvocacion(mundo.getName())){
                            case 0:
                                sender.sendMessage(helpPrefix + "Anuncio por invocación: " + ChatColor.RED + "Desactivado");
                                break;
                            case -1:
                                sender.sendMessage(helpPrefix + "Anuncio por invocación: " + ChatColor.WHITE + "Solo para el mundo actual");
                                break;
                            case -2:
                                sender.sendMessage(helpPrefix + "Anuncio por invocación: " + ChatColor.WHITE + "Para todo el servidor");
                                break;
                            default:
                                sender.sendMessage(helpPrefix + "Anuncio por invocación: " + ChatColor.WHITE + "En un radio de " + worldsConfig.getMostrarAnuncioInvocacion(mundo.getName()) + " alrededor del centro (Donde aparece el portal)" );
                                break;
                        }
                        switch (worldsConfig.getMostrarAnuncioMuerte(mundo.getName())){
                            case 0:
                                sender.sendMessage(helpPrefix + "Anuncio por muerte: " + ChatColor.RED + "Desactivado");
                                break;
                            case -1:
                                sender.sendMessage(helpPrefix + "Anuncio por muerte: " + ChatColor.WHITE + "Solo para el mundo actual");
                                break;
                            case -2:
                                sender.sendMessage(helpPrefix + "Anuncio por muerte: " + ChatColor.WHITE + "Para todo el servidor");
                                break;
                            default:
                                sender.sendMessage(helpPrefix + "Anuncio por muerte: " + ChatColor.WHITE + "En un radio de " + worldsConfig.getMostrarAnuncioMuerte(mundo.getName()) + " alrededor del centro (Donde aparece el portal)" );
                                break;
                        }
                    }else {
                        sender.sendMessage(helpPrefix + "El mundo esta: " + ChatColor.RED + "Desactivado");
                    }
                    sender.sendMessage(separador);
                    return true;
                case "loottable":
                    if(noTieneElPermiso("dragonrewards.loottable",sender)) return true;
                    if(args.length<2){
                        sender.sendMessage(needLootTable);
                        return true;
                    }
                    if(noEsUnaLootTableValida(args[1],sender)) return true;
                    CustomLootTable customLootTable = new CustomLootTable(args[1]);
                    sender.sendMessage(separador);
                    sender.sendMessage(ChatColor.GRAY + "[ - ] LootTable: " + ChatColor.WHITE + args[1] + ChatColor.GRAY + " [ - ]");
                    sender.sendMessage(helpPrefix + "Experiencia/Niveles: " + ChatColor.WHITE + customLootTable.getExperiencia());
                    sender.sendMessage(helpPrefix + "Dinero: " + ChatColor.WHITE + customLootTable.getDinero());
                    sender.sendMessage(separador);
                    return true;

                    // dragonr recompensas <Dragon>
                case "recompensas":
                    if(noTieneElPermiso("dragonrewards.recompensas",sender)) return true;
                    if(!(sender instanceof Player)){
                        sender.sendMessage(langManager.getConfigTranslation("not-console-command"));
                        return true;
                    }
                    player = (Player) sender;
                    if(args.length >= 2){
                        if(noEsUnDragonFileNameValido(args[1],sender)) return true;
                        (new RewardsOptionsGUI(this,player,args[1],null)).buildAndOpenGUI();
                    }else {
                        (new EditDragonListGUI(this,player)).buildAndOpenGUI();
                    }


                    return true;

                    /*
                    dragonr editmundo
                    dragonr editmundo addDragon [MundoEnd] [Dragon]
                    dragonr editmundo remDragon [MundoEnd] [Dragon]
                    dragonr editmundo toggle <MundoEnd>
                    dragonr editmundo setAlias [MundoEnd] [Alias]
                    dragonr editmundo setAnuncioMuerte [MundoEnd] [Valor]
                    dragonr editmundo setAnuncioInvocacion [MundoEnd] [Valor]
                    dragonr editmundo toggleRespawnOnDeath [MundoEnd]
                     */
                case "editmundo":
                    if(noTieneElPermiso("dragonrewards.editmundo",sender)) return true;
                    if(args.length == 1){
                        sender.sendMessage(pluginTitle);
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo addDragon [MundoEnd] [Dragon] ");
                        sender.sendMessage(helpPrefix + "Agrega al dragón a la lista de dragones del mundo.");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo remDragon [MundoEnd] [Dragon]");
                        sender.sendMessage(helpPrefix + "Elimina al dragón de la lista de dragones del mundo.");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo toggle <MundoEnd>");
                        sender.sendMessage(helpPrefix + "Activa/Desactiva el mundo. (Si no se pone nada toma el mundo actual)");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo setAlias [MundoEnd] [Alias]");
                        sender.sendMessage(helpPrefix + "Cambia el Alias del mundo.");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo setAnuncioMuerte [MundoEnd] [Valor]");
                        sender.sendMessage(helpPrefix + "Cambia el modo de anuncio de la muerte de un dragón en ese mundo. (Ver config para mas info)");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo setAnuncioInvocacion [MundoEnd] [Valor]");
                        sender.sendMessage(helpPrefix + "Cambia el modo de anuncio de la invocación de un dragón en ese mundo. (Ver config para mas info)");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editmundo toggleRespawnOnDeath <MundoEnd>");
                        sender.sendMessage(helpPrefix + "Activa/desactiva el respawn al morir el dragón.");
                        sender.sendMessage(separador);
                        return true;
                    }
                    switch (args[1]) {
                        case "toggle":
                            if(noTieneElPermiso("dragonrewards.editmundo.toggle",sender)) return true;
                            if(!(sender instanceof Player) && args.length < 3){
                                sender.sendMessage(needWorldName);
                                return true;
                            }
                            World world;
                            if (args.length < 3){
                                world = ((Player) sender).getWorld();
                            } else {
                                world = Bukkit.getWorld(args[2]);
                            }
                            if(worldsConfig.noEsUnMundoEndValido(world,sender)) return true;
                            assert world != null;
                            boolean actual = worldsConfig.getActivo(world.getName());
                            worldsConfig.setActivo(world.getName(),!actual);
                            if(actual){
                                sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Has " + ChatColor.RED + "desactivado" + ChatColor.GRAY + " el mundo: '" + ChatColor.GOLD +world.getName() + ChatColor.GRAY + "'");
                            }else {
                                sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Has " + ChatColor.GREEN + "activado" + ChatColor.GRAY + " el mundo: '" + ChatColor.GOLD +world.getName() + ChatColor.GRAY + "'");

                            }
                            return true;
                        case "addDragon":
                            if(noTieneElPermiso("dragonrewards.editmundo.adddragon",sender)) return true;
                            if(argsNoContieneMundoValido(args,2,sender)) return true;
                            if (args.length < 4) {
                                sender.sendMessage(needDragonName);
                                return true;
                            }
                            if (noEsUnDragonFileNameValido(args[3], sender)) return true;
                            List<String> dragons;
                            if (worldsConfig.getConfig().contains(args[2] + WorldsConfig.DRAGONS)) {
                                dragons = worldsConfig.getDragons(args[2]);
                            } else {
                                dragons = new ArrayList<>();
                            }
                            dragons.add(args[3]);
                            worldsConfig.setDragons(args[2],dragons);
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has agregado al dragón '" + ChatColor.GOLD + args[3] + ChatColor.GREEN + "' al mundo '" + ChatColor.GOLD + args[2] + ChatColor.GREEN + "'!");
                            return true;
                        case "remDragon":
                            if(noTieneElPermiso("dragonrewards.editmundo.remdragon",sender)) return true;
                            if(argsNoContieneMundoValido(args,2,sender)) return true;
                            if (args.length < 4) {
                                sender.sendMessage(needDragonName);
                                return true;
                            }
                            if (noEsUnDragonFileNameValido(args[3], sender)) return true;
                            List<String> dragons1;
                            if (worldsConfig.getConfig().contains(args[2] + WorldsConfig.DRAGONS)) {
                                dragons1 = worldsConfig.getDragons(args[2]);
                                if(dragons1.isEmpty()){
                                    sender.sendMessage(pluginPrefix + ChatColor.RED + "Ese mundo no tiene dragones asignados!");
                                    return true;
                                }
                            } else {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Ese mundo no tiene dragones asignados!");
                                return true;
                            }
                            if(dragons1.contains(args[3])){
                                dragons1.remove(args[3]);
                                worldsConfig.setDragons(args[2],dragons1);
                                sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has removido al dragón '" + ChatColor.GOLD + args[3] + ChatColor.GREEN + "' del mundo '" + ChatColor.GOLD + args[2] + ChatColor.GREEN + "'!");
                            }else {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "El dragón '" + ChatColor.GOLD + args[3] + ChatColor.RED +"' no esta asignado al mundo '" + ChatColor.GOLD + args[2] + ChatColor.RED + "'!");
                            }

                            return true;
                        case "setAlias":
                            if(noTieneElPermiso("dragonrewards.editmundo.setalias",sender)) return true;
                            if(argsNoContieneMundoValido(args,2,sender)) return true;
                            if (args.length < 4) {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el nuevo Alias del mundo!");
                                return true;
                            }
                            worldsConfig.setAlias(args[2], obtenerStringDelArgsDesde(args,3));
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Alias modificado correctamente!");
                            return true;
                        case "setAnuncioMuerte":
                            if(noTieneElPermiso("dragonrewards.editmundo.setanunciomuerte",sender)) return true;
                            if(argsNoContieneMundoValido(args,2,sender)) return true;
                            if (args.length < 4) {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el valor de anuncio! ('-2'[server], '-1'[mundo], '0'[desactivar] o mas[área])");
                                return true;
                            }
                            int valor1;
                            try {
                                valor1 = Integer.parseInt(args[3]);
                            }catch (Exception e){
                                sender.sendMessage(pluginPrefix + "Necesitas escribir un número entero mayor o igual a -2!");
                                return true;
                            }

                            if(valor1 < -2){
                                sender.sendMessage(pluginPrefix + "No se permiten números menores a -2!");
                                return true;
                            }

                            switch (valor1){
                                case 0:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por muerte: " + ChatColor.RED + "Desactivado");
                                    break;
                                case -1:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por muerte: " + ChatColor.WHITE + "Solo para el mundo actual");
                                    break;
                                case -2:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por muerte: " + ChatColor.WHITE + "Para todo el servidor");
                                    break;
                                default:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por muerte: " + ChatColor.WHITE + "En un radio de " + valor1 + " alrededor del centro (Donde aparece el portal)" );
                                    break;
                            }

                            worldsConfig.setMostrarAnuncioMuerte(args[2],valor1);
                            return true;
                        case "setAnuncioInvocacion":
                            if(noTieneElPermiso("dragonrewards.editmundo.setanuncioinvocacion",sender)) return true;
                            if(argsNoContieneMundoValido(args,2,sender)) return true;
                            if (args.length < 4) {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el valor de anuncio! ('-2'[server], '-1'[mundo], '0'[desactivar] o mas[área])");
                                return true;
                            }
                            int valor2;
                            try {
                                valor2 = Integer.parseInt(args[3]);
                            }catch (Exception e){
                                sender.sendMessage(pluginPrefix + "Necesitas escribir un número entero mayor o igual a -2!");
                                return true;
                            }

                            if(valor2 < -2){
                                sender.sendMessage(pluginPrefix + "No se permiten números menores a -2!");
                                return true;
                            }

                            switch (valor2){
                                case 0:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por invocación: " + ChatColor.RED + "Desactivado");
                                    break;
                                case -1:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por invocación: " + ChatColor.WHITE + "Solo para el mundo actual");
                                    break;
                                case -2:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por invocación: " + ChatColor.WHITE + "Para todo el servidor");
                                    break;
                                default:
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has establecido el anuncio por invocación: " + ChatColor.WHITE + "En un radio de " + valor2 + " alrededor del centro (Donde aparece el portal)" );
                                    break;
                            }

                            worldsConfig.setMostrarAnuncioInvocacion(args[2],valor2);
                            return true;
                        case "toggleRespawnOnDeath":
                            if(noTieneElPermiso("dragonrewards.editmundo.togglerespawnondeath",sender)) return true;
                            if(!(sender instanceof Player) && args.length < 3){
                                sender.sendMessage(needWorldName);
                                return true;
                            }
                            World world2;
                            if (args.length < 3){
                                world2 = ((Player) sender).getWorld();
                            } else {
                                world2 = Bukkit.getWorld(args[2]);
                            }
                            if(worldsConfig.noEsUnMundoEndValido(world2,sender)) return true;
                            assert world2 != null;
                            boolean respawnOnDeathActual = worldsConfig.getRespawnOnDeath(world2.getName());
                            worldsConfig.setRespawnOnDeath(world2.getName(),!respawnOnDeathActual);
                            if(respawnOnDeathActual){
                                sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Has " + ChatColor.RED + "desactivado" + ChatColor.GRAY + " el respawn en el mundo: '" + ChatColor.GOLD + world2.getName() + ChatColor.GRAY + "'");
                            }else {
                                sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Has " + ChatColor.GREEN + "activado" + ChatColor.GRAY + " el respawn en el mundo: '" + ChatColor.GOLD + world2.getName() + ChatColor.GRAY + "'");

                            }
                            return true;
                        default:
                            sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Subcomandos de editmundo: " + grayColor +"addDragon"+ ChatColor.GRAY +"," + grayColor + " remDragon " + ChatColor.GRAY + "y" + grayColor + " toggle.");
                            return true;
                    }
                    /*
                    dragonr editdragon
                    dragonr editdragon delete [NombreDragon]
                    dragonr editdragon newdragon [Nombre]
                    dragonr editdragon setname [NombreDragon] [NombreDisplay]
                    dragonr editdragon setattribute vida [NombreDragon] [valor]
                    dragonr editdragon setattribute armadura [NombreDragon] [valor]
                    dragonr editdragon setattribute daño [NombreDragon] [valor]
                     */
                case "editdragon":
                    if(noTieneElPermiso("dragonrewards.editdragon",sender)) return true;
                    if(args.length == 1){
                        sender.sendMessage(pluginTitle);
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editdragon reload [NombreDragon]");
                        sender.sendMessage(helpPrefix + "Recarga la configuración de un dragón que esté vivo (Por si cambias alguna configuración y querés que se aplique al momento)");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editdragon delete [NombreDragon]");
                        sender.sendMessage(helpPrefix + "Elimina al dragón con el nombre de archivo seleccionado. (No se puede deshacer esta acción)");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editdragon newdragon [Nombre]");
                        sender.sendMessage(helpPrefix + "Crea un nuevo dragón con los valores default.");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editdragon setname [NombreDragon] [NombreDisplay]");
                        sender.sendMessage(helpPrefix + "Cambia el nombre de display (El que se ve in-game) del dragón.");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr editdragon setattribute [vida/armadura/daño] [NombreDragon] [valor]");
                        sender.sendMessage(helpPrefix + "Cambia el atributo seleccionado.");
                        sender.sendMessage(separador);
                        return true;
                    }
                    DragonsConfig dragonsConfig;
                    switch (args[1]) {
                        case "reload":
                            if(noTieneElPermiso("dragonrewards.editdragon.reload",sender)) return true;
                            if (args.length < 3) {
                                sender.sendMessage(needDragonName);
                                return true;
                            }
                            if (noEsUnDragonFileNameValido(args[2], sender)) return true;
                            if (!elDragonEstaActivo(args[2])) {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "El dragón no está activo, no es necesario recargarlo!");
                                return true;
                            }
                            for(Dragon dragon : getActiveDragonInstances(args[2])){
                                dragon.reloadDragonProperties();
                            }
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha recargado al dragón '" + ChatColor.GOLD + args[2] + ChatColor.GREEN + "' exitosamente.");
                            return true;
                        case "delete":
                            if(noTieneElPermiso("dragonrewards.editdragon.delete",sender)) return true;
                            if (args.length < 3) {
                                sender.sendMessage(needDragonName);
                                return true;
                            }
                            if (noEsUnDragonFileNameValido(args[2], sender)) return true;
                            if (elDragonEstaActivo(args[2])) {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "No se puede eliminar un dragon que esta vivo! (Use: 'dragonr admin matar [MundoDondeEstaVivo]' para matarlo");
                                return true;
                            }
                            dragonsConfig = new DragonsConfig(args[2]);
                            dragonsConfig.delete();
                            dragonesDisponibles.remove(obtenerPorDragonFileName(args[2]));
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha eliminado al dragón '" + ChatColor.GOLD + args[2] + ChatColor.GREEN + "' exitosamente.");
                            return true;
                        case "newdragon":
                            if(noTieneElPermiso("dragonrewards.editdragon.newdragon",sender)) return true;
                            if (args.length < 3) {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el nombre del nuevo dragón!");
                                return true;
                            }
                            if (!noEsUnDragonFileNameValido(args[2])) {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "No puedes seleccionar ese nombre para el dragón porque ya existe uno que se llama así.");
                                return true;
                            }
                            new DragonsConfig(args[2]);
                            DragonNameAndFileName dragonNameAndFileName = new DragonNameAndFileName();
                            dragonNameAndFileName.setDragonName(args[2]);
                            dragonNameAndFileName.setDragonFileName(args[2]);
                            dragonesDisponibles.add(dragonNameAndFileName);
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Dragón creado con éxito!");
                            return true;
                        case "setname":
                            if(noTieneElPermiso("dragonrewards.editdragon.setname",sender)) return true;
                            if (args.length < 4) {
                                sender.sendMessage(needDragonName);
                                return true;
                            }
                            if (noEsUnDragonFileNameValido(args[2], sender)) return true;
                            if (args.length < 5){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el nuevo nombre de display!");
                                return true;
                            }
                            dragonsConfig = new DragonsConfig(args[2]);
                            dragonsConfig.getConfig().set(DragonsConfig.NOMBRE_DRAGON, obtenerStringDelArgsDesde(args,3));
                            dragonsConfig.saveConfig();
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has cambiado el nombre de display de '" + ChatColor.GOLD + args[2] + ChatColor.GREEN + "'!");
                            return true;
                        case "setattribute":
                            if(noTieneElPermiso("dragonrewards.editdragon.setattribute",sender)) return true;
                            double valor;
                            if(args.length < 3){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el atributo que quieres cambiar!");
                                return true;
                            }
                            switch (args[2]) {
                                case "vida":
                                    if(noTieneElPermiso("dragonrewards.editdragon.setattribute.vida",sender)) return true;
                                    if (args.length < 4) {
                                        sender.sendMessage(needDragonName);
                                        return true;
                                    }
                                    if (noEsUnDragonFileNameValido(args[3], sender)) return true;
                                    try {
                                        valor = Double.parseDouble(args[4]);
                                    } catch (Exception e) {
                                        sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir un valor numérico (Puede ser con punto)!");
                                        return true;
                                    }
                                    if(valor > 2048.0 || valor <= 0){
                                        sender.sendMessage(pluginPrefix + ChatColor.RED + "El valor de vida no puede ser mayor 2048.0 ni ser menor o igual a 0!");
                                        return true;
                                    }
                                    dragonsConfig = new DragonsConfig(args[3]);
                                    dragonsConfig.getConfig().set(DragonsConfig.VIDA_MAXIMA, valor);
                                    dragonsConfig.saveConfig();
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has cambiado la vida máxima del dragón '" + ChatColor.GOLD + args[3] + ChatColor.GREEN + "' a " + ChatColor.GOLD + valor + ChatColor.GREEN + "!");
                                    return true;
                                case "armadura":
                                    if(noTieneElPermiso("dragonrewards.editdragon.setattribute.armadura",sender)) return true;
                                    if (args.length < 4) {
                                        sender.sendMessage(needDragonName);
                                        return true;
                                    }
                                    if (noEsUnDragonFileNameValido(args[3], sender)) return true;
                                    try {
                                        valor = Double.parseDouble(args[4]);
                                    } catch (Exception e) {
                                        sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir un valor numérico (Puede ser con punto)!");
                                        return true;
                                    }
                                    dragonsConfig = new DragonsConfig(args[3]);
                                    dragonsConfig.getConfig().set(DragonsConfig.ARMADURA, valor);
                                    dragonsConfig.saveConfig();
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has cambiado la armadura del dragón '" + ChatColor.GOLD + args[3] + ChatColor.GREEN + "' a " + ChatColor.GOLD + valor + ChatColor.GREEN + "!");
                                    return true;
                                case "daño":
                                    if(noTieneElPermiso("dragonrewards.editdragon.setattribute.danio",sender)) return true;
                                    if (args.length < 4) {
                                        sender.sendMessage(needDragonName);
                                        return true;
                                    }
                                    if (noEsUnDragonFileNameValido(args[3], sender)) return true;
                                    try {
                                        valor = Double.parseDouble(args[4]);
                                    } catch (Exception e) {
                                        sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir un valor numérico (Puede ser con punto)!");
                                        return true;
                                    }
                                    dragonsConfig = new DragonsConfig(args[3]);
                                    dragonsConfig.getConfig().set(DragonsConfig.DANIO, valor);
                                    dragonsConfig.saveConfig();
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Has cambiado el daño del dragón '" + ChatColor.GOLD + args[3] + ChatColor.GREEN + "' a " + ChatColor.GOLD + valor + ChatColor.GREEN + "!");
                                    return true;
                                default:
                                    sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Atributos que se pueden cambiar: vida, armadura y daño.");
                                    return true;
                            }

                        default:
                            sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Subcomandos de editdragon: " + grayColor + "delete" + ChatColor.GRAY + "," + grayColor + " newdragon" + ChatColor.GRAY + "," + grayColor + " setname" + ChatColor.GRAY + "," + grayColor + " setworld " + ChatColor.GRAY + "y" + grayColor + " setattribute");
                            return true;
                    }
                    //dragonr editloot <CustomLootTable>
                    //dragonr editloot [CustomLootTable] addcomando [Comando]
                    //dragonr editloot [CustomLootTable] setexp [CantExp]
                    //dragonr editloot [CustomLootTable] setdinero [CantDinero]
                case "editloot":
                    if(noTieneElPermiso("dragonrewards.editloot",sender)) return true;
                    if(args.length <=2){
                        if(!(sender instanceof Player)){
                            sender.sendMessage(langManager.getConfigTranslation("not-console-command"));
                            return true;
                        }
                        player = (Player) sender;
                        if(args.length == 1){
                            (new EditLootTablesListGUI(this,player)).buildAndOpenGUI();
                        }else {
                            if(noEsUnaLootTableValida(args[1],player)) return true;
                            (new EditLootTableOptionsGUI(this,player,args[1],null)).buildAndOpenGUI();
                        }
                        return true;
                    }
                    if(noEsUnaLootTableValida(args[1],sender)) return true;
                    switch (args[2]) {
                        case "comandos":
                            if (noTieneElPermiso("dragonrewards.editloot.comandos", sender)) return true;
                            (new EditLootTableCommandsChatGUI(this,sender,args[1])).buildAndOpenGUI();
                            break;
                        case "addcomando":
                            if (noTieneElPermiso("dragonrewards.editloot.addcomando", sender)) return true;
                            if (args.length >= 4) {
                                if (args[3].startsWith("[consola]") || args[3].startsWith("[usuario]")) {
                                    new CustomLootTable(args[1]).agregarComando(obtenerStringDelArgsDesde(args,3));
                                    (new EditLootTableCommandsChatGUI(this,sender,args[1])).buildAndOpenGUI();
                                    sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha agregado el comando correctamente!");
                                } else {
                                    sender.sendMessage(pluginPrefix + ChatColor.RED + "El comando debe empezar con [consola] o [usuario] para hacer referencia a quien lo ejecuta!");
                                }
                            } else {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir el comando!");
                            }
                            break;
                        case "remcomando":
                            if (noTieneElPermiso("dragonrewards.editloot.remcomando", sender)) return true;
                            if (args.length >= 4) {
                                try {
                                    new CustomLootTable(args[1]).remComando(Integer.parseInt(args[3]));
                                }catch(Exception e){
                                    sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir un número entero!");
                                    break;
                                }
                                (new EditLootTableCommandsChatGUI(this,sender,args[1])).buildAndOpenGUI();
                                sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha eliminado el comando correctamente!");
                            } else {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir la posición del comando!");
                            }
                            break;
                        case "setexp":
                            if (noTieneElPermiso("dragonrewards.editloot.setexp", sender)) return true;
                            if (args.length == 3){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir la cantidad de experiencia o niveles (Con una L al final)!");
                                break;
                            }
                            String exp = args[3];
                            try {
                                if(exp.endsWith("L")){
                                    Integer.parseInt(exp.replace("L",""));
                                }else {
                                    Integer.parseInt(exp);
                                }
                            }catch (NumberFormatException e){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir un número entero!");
                                break;
                            }
                            new CustomLootTable(args[1]).setExperiencia(args[3]);
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha establecido la experiencia correctamente!");
                            break;
                        case "setdinero":
                            if (noTieneElPermiso("dragonrewards.editloot.setdinero", sender)) return true;
                            if (args.length == 3){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir la cantidad de dinero!");
                                break;
                            }
                            double dinero;
                            try {
                                dinero = Double.parseDouble(args[3]);
                            }catch (NumberFormatException e){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "Necesitas escribir un valor numérico (Puede ser con punto)!");
                                break;
                            }
                            new CustomLootTable(args[1]).setDinero(dinero);
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha establecido el dinero correctamente!");
                            if(!useVault)
                                sender.sendMessage(ChatColor.RED + "Advertencia: Se encuentran desactivadas las recompensas económicas (Ver la config para mas info)");
                            break;

                        default:
                            sender.sendMessage(pluginPrefix + grayColor + "Comandos disponibles: addcomando, setexp y setdinero");
                            break;
                    }

                   return true;


                    /*
                    dragonr admin
                    dragonr admin darrecompensa [CustomLootTable] [jugador/@a]
                    dragonr admin remrecompensas [jugador]
                    dragonr admin invocar [Mundo] [Dragon]
                    dragonr admin matar [Mundo]
                    */
                case "admin":
                    if(noTieneElPermiso("dragonrewards.admin",sender)) return true;
                    if(args.length == 1){
                        sender.sendMessage(pluginTitle);
                        sender.sendMessage(ChatColor.WHITE + "/dragonr admin darrecompensa [LootTable] [Jugador/@a]");
                        sender.sendMessage(helpPrefix + "Da la recompensa al jugador. (El jugador debe estar Online)");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr admin remrecompensas [Jugador]");
                        sender.sendMessage(helpPrefix + "Elimina todas las recompensas del jugador.");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr admin invocar [MundoEnd] [Dragon]");
                        sender.sendMessage(helpPrefix + "Invoca un dragón en el mundo.");
                        sender.sendMessage(ChatColor.WHITE + "/dragonr admin matar [MundoEnd]");
                        sender.sendMessage(helpPrefix + "Mata al dragón.");
                        sender.sendMessage(separador);
                        return true;
                    }
                    switch (args[1]){
                        case "darrecompensa": // dragonr admin darrecompensa [CustomLootTable] [Jugador/@a]
                            if(noTieneElPermiso("dragonrewards.admin.darrecompensa",sender)) return true;
                            if(args.length < 3){
                                sender.sendMessage(needLootTable);
                                return true;
                            }
                            if(noEsUnaLootTableValida(args[2],sender)) return true;
                            if(args.length < 4){
                                sender.sendMessage(needUserName);
                                return true;
                            }

                            if(args[3].equalsIgnoreCase("@a")){
                                for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                                    playerDataConfig.agregarLootAJugador(args[2],onlinePlayer,true);
                                }
                                sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha agregado la recompensa a todos los jugadores online correctamente!");
                                return true;
                            }

                            player = Bukkit.getPlayer(args[3]);
                            if(player == null){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "No se pudo encontrar al jugador '" + args[3] + ChatColor.RED +"' (No está online)");
                                return true;
                            }

                            playerDataConfig.agregarLootAJugador(args[2],player,true);
                            sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha agregado la recompensa correctamente!");
                            return true;

                        case "remrecompensas": // dragonr admin remrecompensas [jugador]
                            if(noTieneElPermiso("dragonrewards.admin.remrecompensas",sender)) return true;
                            if(args.length < 3){
                                sender.sendMessage(needUserName);
                                return true;
                            }
                            player = Bukkit.getPlayer(args[2]);
                            if(player == null){
                                uuid = playerDataConfig.getUUID(args[2]);
                                if(uuid == null){
                                    sender.sendMessage(pluginPrefix + ChatColor.RED + "No se pudo encontrar al jugador '" + args[2] + ChatColor.RED +"'");
                                    return true;
                                }
                            }else {
                                uuid = player.getUniqueId();
                            }
                            if(playerDataConfig.tieneLootDisponible(uuid)){
                                playerDataConfig.sacarTodoElLoot(uuid);
                                sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se han eliminado todas las recompensas de '" + args[2] + "' correctamente.");
                            }else {
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "El jugador '" + ChatColor.GOLD + args[2] + ChatColor.RED + "' no tiene recompensas que reclamar.");
                            }
                            return true;

                        case "invocar": // dragonr admin invocar [Mundo] [Dragon]
                            if(noTieneElPermiso("dragonrewards.admin.invocar",sender)) return true;
                            if(argsNoContieneMundoValido(args,2,sender)) return true;
                            if(worldsConfig.noEstaActivoEnEseMundo(args[2], sender)) return true;
                            if(args.length < 4){
                                sender.sendMessage(needDragonName);
                                return true;
                            }
                            if(noEsUnDragonFileNameValido(args[3],sender)) return true;

                            Dragon dragon = new Dragon(args[3],args[2],plugin,true);
                            if(dragon.spawn()){
                               sender.sendMessage(pluginPrefix + ChatColor.GREEN + "Se ha iniciado la invocación de " + ChatColor.GOLD + args[3] + ChatColor.GREEN + " en el mundo "+ ChatColor.GOLD + args[2] + ChatColor.GREEN + " exitosamente.");
                            }else {
                               sender.sendMessage(pluginPrefix + ChatColor.RED + "No se ha podido iniciar la invocación de " + ChatColor.GOLD + args[3] + ChatColor.RED + " en el mundo "+ ChatColor.GOLD + args[2] + ChatColor.RED + " (Revisar consola!).");
                            }
                            return true;

                        case "matar": // dragonr admin matar [Mundo]
                            if(noTieneElPermiso("dragonrewards.admin.matar",sender)) return true;
                            String mundoName;
                            if(args.length < 3){
                                if(!(sender instanceof Player)){
                                    sender.sendMessage(needWorldName);
                                    return true;
                                }
                                mundoName = ((Player) sender).getWorld().getName();
                            }else {
                                mundoName = args[2];
                            }
                            if(worldsConfig.noEsUnMundoEndValido(mundoName,sender)) return true;
                            if (dragonesActivos.get(Bukkit.getWorld(mundoName)) == null){
                                sender.sendMessage(pluginPrefix + ChatColor.RED + "El dragón del mundo '" + ChatColor.GOLD +mundoName + ChatColor.RED + "' ya está muerto o no se pudo detectar!");
                                return true;
                            }
                            dragonesActivos.get(Bukkit.getWorld(mundoName)).kill(sender);
                            return true;

                        default:
                            sender.sendMessage(pluginPrefix + ChatColor.GRAY + "Subcomandos de admin: " + grayColor + "darrecompensa"+ ChatColor.GRAY +","+ grayColor +" remrecompensas"+ ChatColor.GRAY +","+ grayColor + " invocar " + ChatColor.GRAY +"y" + grayColor +" matar");
                            return true;
                    }

                default:
                    sender.sendMessage(ChatColor.GRAY + "Ayuda: " + grayColor + "/dragonrewards help");
                    return true;
            }

        }
        return false;
    }

    private boolean argsNoContieneMundoValido(String[] args, int posMundo, CommandSender sender) {
        if (args.length < posMundo + 1) {
            sender.sendMessage(needWorldName);
            return true;
        }
        return worldsConfig.noEsUnMundoEndValido(args[posMundo], sender);
    }

    /*
    * Retorna un string con todos los strings del args acomodados con espacios entre medio desde un punto del array
    * */
    private String obtenerStringDelArgsDesde(String[] args, int desdeDondeTomar){
        String retorno = "";
        for (int i = desdeDondeTomar; i < args.length; i++) {
            retorno = retorno.concat(args[i]);
            if (i != args.length - 1)
                retorno = retorno.concat(" ");
        }
        return retorno;
    }

    public String devolverSeparadoConComas(final List<String> cadena){
        StringBuilder builder = new StringBuilder();
        int i = cadena.size();
        for(String elem : cadena){
            i--;
            if(builder.length() != 0)
                builder.append(" ");
            builder.append(elem);
            if(i == 1){
                builder.append(" y");
                continue;
            }else if (i != 0){
                builder.append(',');
                continue;
            }
            break;
        }
        return builder.toString();
    }

    public boolean noEsUnaLootTableValida(String lootTableName, CommandSender sender){
        if(lootTableName == null){
            sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "Esa LootTable no existe o no esta disponible.");
            return true;
        }
        if(lootTablesDisponibles.contains(lootTableName)) return false;
        sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "La LootTable '" + ChatColor.GOLD + lootTableName + ChatColor.RED + "' no existe o no esta disponible.");
        return true;
    }
    public boolean noTieneElPermiso(final String permiso, final CommandSender sender){
        if(!sender.hasPermission(permiso)){
            sender.sendMessage(Objects.requireNonNull(langManager.getConfigTranslation("no-permission")));
            return true;
        }
        return false;
    }

    public List<Dragon> getActiveDragonInstances(String dragonFileName){
        if(noEsUnDragonFileNameValido(dragonFileName)) return new ArrayList<>();
        List<Dragon> activeDragonInstances = new ArrayList<>();
        for(Dragon dragon : dragonesActivos.values()){
            if(dragon.getDragonFileName().equalsIgnoreCase(dragonFileName)) activeDragonInstances.add(dragon);
        }
        return activeDragonInstances;
    }



    private List<String> scannerDeNombresArchivos(final String path){
        File directoryPath = new File(plugin.getDataFolder() + path);
        String[] listaDeArchivos =  directoryPath.list();
        List<String> retorno = new ArrayList<>();
        if (listaDeArchivos == null) return null;
        for (String elem: listaDeArchivos){
            if(elem.endsWith(".yml")){
                retorno.add(elem.replace(".yml", ""));
            }
        }
        return retorno;
    }

    private void scannerDeDragones(){
        List<String> nombreArchivos = scannerDeNombresArchivos(ConfigFileManager.directoryPath.get(ConfigFileManager.directoryEnum.DRAGON));
        if(nombreArchivos == null) return;
        DragonsConfig dragonConfig;
        DragonNameAndFileName dragon = new DragonNameAndFileName();
        for(String elem : nombreArchivos){
            if(!noEsUnDragonFileNameValido(elem)) continue;
            dragonConfig = new DragonsConfig(elem);
            if(dragonConfig.getConfig().contains(DragonsConfig.NOMBRE_DRAGON)){
                dragon.setDragonFileName(elem);
                String nombre = dragonConfig.getConfig().getString(DragonsConfig.NOMBRE_DRAGON,elem);
                nombre = LangManager.agregarTodosLosColores(nombre);
                dragon.setDragonName(nombre);
                if(!dragonesDisponibles.contains(dragon))
                dragonesDisponibles.add(dragon);
                dragon = new DragonNameAndFileName();
            }
        }
    }

    private void scannerDeLootTables(){
        List<String> nombreArchivos = scannerDeNombresArchivos(ConfigFileManager.directoryPath.get(ConfigFileManager.directoryEnum.LOOTTABLE));
        if(nombreArchivos == null) return;
        for(String elem : nombreArchivos){
            if(lootTablesDisponibles.contains(elem)) continue;
            lootTablesDisponibles.add(elem);
        }
    }

    public static boolean noEsUnDragonFileNameValido(final String dragonFileName, final CommandSender sender){
        if (dragonesDisponibles.stream().anyMatch(elem -> elem.getDragonFileName().equals(dragonFileName))) return false;
        sender.sendMessage(Core.plugin.pluginPrefix + ChatColor.RED + "El dragón '" + ChatColor.GOLD + dragonFileName + ChatColor.RED + "' no existe o no esta disponible.");
        return true;
    }

    public static boolean noEsUnDragonFileNameValido(final String dragonFileName){
        for (DragonNameAndFileName elem : dragonesDisponibles) {
            if (elem.getDragonFileName().equals(dragonFileName)) return false;
        }
        return true;
    }

    public static boolean noEsUnDragonNameValido(String dragonName){
        dragonName = ChatColor.stripColor(dragonName);
        for (DragonNameAndFileName elem : dragonesDisponibles) {
            if (elem.getDragonName().equals(dragonName)) return false;
        }
        return true;
    }

    public boolean elDragonEstaActivo(final String fileNameDragon){
        for(Dragon elem : dragonesActivos.values()){
            if(elem.getDragonFileName().equalsIgnoreCase(fileNameDragon)){
                return true;
            }
        }
        return false;
    }

    public boolean elMundoTieneUnDragon(final World world){
        return dragonesActivos.containsKey(world);
    }

    public static DragonNameAndFileName obtenerPorDragonFileName(final String dragonFileName){
        for (Core.DragonNameAndFileName elem : Core.dragonesDisponibles){
            if(elem.getDragonFileName().equals(dragonFileName)) return elem;
        }
        return null;
    }



    public static DragonNameAndFileName obtenerPorDragonName(String dragonName){
        dragonName = ChatColor.stripColor(dragonName);
        for (Core.DragonNameAndFileName elem : Core.dragonesDisponibles){
            if(elem.getDragonName().equals(dragonName)) return elem;
        }
        return null;
    }

    private void generateDefaultConfigFiles(){
        ConfigFileManager.loadConfigFromJar("default_assist_loot.yml","/LootTables");
        ConfigFileManager.loadConfigFromJar("default_lasthit_loot.yml","/LootTables");
        ConfigFileManager.loadConfigFromJar("Smaug.yml","/Dragones");
    }

}
