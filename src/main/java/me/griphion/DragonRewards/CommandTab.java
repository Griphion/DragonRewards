package me.griphion.DragonRewards;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTab implements TabCompleter {

    private final List<String> argumentosBase = Arrays.asList("help", "reload", "mundo", "recompensas", "loottable", "reclamar", "top", "editloot", "editmundo" ,"admin", "editdragon", "stats", "dragones", "dragon");

    private final List<String> argumentosAdmin = Arrays.asList("darrecompensa", "remrecompensas", "invocar", "matar");

    private final List<String> argumentosTop = Arrays.asList("kill", "assist");

    private final List<String> argumentosEditDragon = Arrays.asList("reload","delete", "newdragon","setname", "setattribute");

    private final List<String> argumentosEditLoot = Arrays.asList("comandos", "addcomando", "remcomando", "setexp", "setdinero");

    private final List<String> argumentosSetAttribute = Arrays.asList("vida", "armadura","daño");

    private final List<String> argumentosEditMundo = Arrays.asList("toggle", "addDragon", "remDragon", "setAlias","setAnuncioMuerte","setAnuncioInvocacion","toggleRespawnOnDeath");
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, final String[] args) {

        List<String> result = new ArrayList<>();

        // -- Argumentos --


        if(args.length == 1){
            for(String a : argumentosBase){
                if(a.toLowerCase().startsWith(args[0].toLowerCase()) && sender.hasPermission("dragonrewards." + a.toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("top") ){
            for(String a : argumentosTop){
                if(a.toLowerCase().startsWith(args[1].toLowerCase())  && sender.hasPermission("dragonrewards.top." + a.toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("admin")){
            for(String a : argumentosAdmin){
                if(a.toLowerCase().startsWith(args[1].toLowerCase()) && sender.hasPermission("dragonrewards.admin." + a.toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("editdragon")){
            for(String a : argumentosEditDragon){
                if(a.toLowerCase().startsWith(args[1].toLowerCase()) && sender.hasPermission("dragonrewards.editdragon." + a.toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("editmundo")){
            for(String a : argumentosEditMundo){
                if(a.toLowerCase().startsWith(args[1].toLowerCase()) && sender.hasPermission("dragonrewards.editmundo." + a.toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(args.length == 3 && args[0].equalsIgnoreCase("editloot") ){
            for(String a : argumentosEditLoot){
                if(a.toLowerCase().startsWith(args[2].toLowerCase())  && sender.hasPermission("dragonrewards.editloot." + a.toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(args.length == 3 && args[0].equalsIgnoreCase("editdragon") && args[1].equalsIgnoreCase("setattribute")){
            for(String a : argumentosSetAttribute){
                if(a.toLowerCase().startsWith(args[2].toLowerCase()) && sender.hasPermission("dragonrewards.editdragon.setattribute." + a.toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        // ---- ---- ---- ----


        // -- LootTablesDisponibles --
        if(args.length == 2 && (
                   (args[0].equalsIgnoreCase("editloot") && sender.hasPermission("dragonrewards.editloot"))
                || (args[0].equalsIgnoreCase("loottable") && sender.hasPermission("dragonrewards.loottable"))
        )){
            for(String a : Core.lootTablesDisponibles){
                if(a.toLowerCase().startsWith(args[1].toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(sender.hasPermission("dragonrewards.admin.darrecompensa"))
        if(args.length == 3 && args[1].equalsIgnoreCase("darrecompensa") ){
            for(String a : Core.lootTablesDisponibles){
                if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                    result.add(a);
            }
            return result;
        }
        // ---- ---- ---- ----

        // -- MundosEnd --
        if(sender.hasPermission("dragonrewards.mundo"))
        if(args.length == 2 && (args[0].equalsIgnoreCase("mundo")) ){
            for(String a : Core.worldsConfig.mundosEndNames){
                if(a.toLowerCase().startsWith(args[1].toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if(args.length == 3 &&
                (  (args[1].equalsIgnoreCase("toggle") && sender.hasPermission("dragonrewards.editmundo.toggle"))
                        || (args[1].equalsIgnoreCase("invocar") && sender.hasPermission("dragonrewards.admin.invocar"))
                        || (args[1].equalsIgnoreCase("addDragon")&& sender.hasPermission("dragonrewards.editmundo.adddragon"))
                        || (args[1].equalsIgnoreCase("remDragon")&& sender.hasPermission("dragonrewards.editmundo.remdragon"))
                        || (args[1].equalsIgnoreCase("setAlias")&& sender.hasPermission("dragonrewards.editmundo.setAlias"))
                        || (args[1].equalsIgnoreCase("setAnuncioMuerte")&& sender.hasPermission("dragonrewards.editmundo.setAnuncioMuerte"))
                        || (args[1].equalsIgnoreCase("setAnuncioInvocacion")&& sender.hasPermission("dragonrewards.editmundo.setAnuncioInvocacion"))
                        || (args[1].equalsIgnoreCase("toggleRespawnOnDeath")&& sender.hasPermission("dragonrewards.editmundo.toggleRespawnOnDeath"))
                        || (args[1].equalsIgnoreCase("matar") && sender.hasPermission("dragonrewards.admin.matar"))) ){
            for(String a : Core.worldsConfig.mundosEndNames){
                if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        // ---- ---- ---- ----

        // -- DragonesDisponibles --
        if(args.length == 2 && (
                (args[0].equalsIgnoreCase("recompensas") && sender.hasPermission("dragonrewards.recompensas"))
                || (args[0].equalsIgnoreCase("dragon") && sender.hasPermission("dragonrewards.dragon"))
        )){
            for(Core.DragonNameAndFileName a : Core.dragonesDisponibles){
                if(a.getDragonFileName().toLowerCase().startsWith(args[1].toLowerCase()))
                    result.add(a.getDragonFileName());
            }
            return result;
        }

        if(args.length == 3 && (
                (args[1].equalsIgnoreCase("delete") && sender.hasPermission("dragonrewards.editdragon.delete"))
                || (args[1].equalsIgnoreCase("newdragon") && sender.hasPermission("dragonrewards.editdragon.newdragon"))
                || (args[1].equalsIgnoreCase("setname") && sender.hasPermission("dragonrewards.editdragon.setname"))
                || (args[1].equalsIgnoreCase("reload") && sender.hasPermission("dragonrewards.editdragon.reload"))
        ) ){
            for(Core.DragonNameAndFileName a : Core.dragonesDisponibles){
                if(a.getDragonFileName().toLowerCase().startsWith(args[2].toLowerCase()))
                    result.add(a.getDragonFileName());
            }
            return result;
        }

        if(args.length == 4 &&
                (  (args[1].equalsIgnoreCase("addDragon") && sender.hasPermission("dragonrewards.editmundo.adddragon"))
                || (args[1].equalsIgnoreCase("remDragon") && sender.hasPermission("dragonrewards.editmundo.remdragon"))
                || (args[1].equalsIgnoreCase("invocar") && sender.hasPermission("dragonrewards.admin.invocar"))
                || (args[2].equalsIgnoreCase("vida") && sender.hasPermission("dragonrewards.editdragon.setattribute.vida"))
                || (args[2].equalsIgnoreCase("daño") && sender.hasPermission("dragonrewards.editdragon.setattribute.danio"))
                || (args[2].equalsIgnoreCase("armadura") && sender.hasPermission("dragonrewards.editdragon.setattribute.armadura"))
                )
        ){
            for(Core.DragonNameAndFileName a : Core.dragonesDisponibles){
                if(a.getDragonFileName().toLowerCase().startsWith(args[3].toLowerCase()))
                    result.add(a.getDragonFileName());
            }
            return result;
        }

        // ---- ---- ---- ----

        return null;
    }
}

