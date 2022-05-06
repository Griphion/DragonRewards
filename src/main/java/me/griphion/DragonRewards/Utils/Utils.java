package me.griphion.DragonRewards.Utils;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    /*
     * Considerando que:
     * 1 mes son: 52596000 ticks (30.4375 dias)
     * 1 semana son: 12096000 ticks
     * 1 dia son: 1728000 ticks
     * 1 hora son: 72000 ticks
     * 1 minuto son: 1200 ticks
     * 1 segundo son: 20 ticks
     * */
    public static final Map<String,Long> groupNamesAndTicks;
    static {
        groupNamesAndTicks = new HashMap<>();
        groupNamesAndTicks.put("SEMANAS",12096000L);
        groupNamesAndTicks.put("DIAS",1728000L);
        groupNamesAndTicks.put("HORAS",72000L);
        groupNamesAndTicks.put("MINUTOS",1200L);
        groupNamesAndTicks.put("SEGUNDOS",20L);
    }

    static public boolean getChance(double probability){
        return (new SplittableRandom()).nextDouble() <= (probability/100);
    }
    static public boolean getChance(int probability){
        return (new SplittableRandom()).nextInt(0,101) <= probability;
    }

    static public Player getRandomClosePlayer(Entity entity, double xRadius, double yRadius, double zRadius){
        List<Player> closePlayers = getAllClosePlayers(entity,xRadius,yRadius,zRadius);
        if(closePlayers.isEmpty()) return null;
        return closePlayers.get(0);
    }

    static public List<Player> getAllClosePlayers(Entity entity, double xRadius, double yRadius, double zRadius){
        List<Player> players = new ArrayList<>();
        for (Entity entity2 : entity.getNearbyEntities(xRadius,yRadius,zRadius)) {
            if ((entity2 instanceof Player) && (!entity2.isDead()) && ( ((Player) entity2).getGameMode().equals(GameMode.SURVIVAL) || ((Player) entity2).getGameMode().equals(GameMode.ADVENTURE)) ) {
                players.add((Player) entity2);
            }
        }
        return players;
    }

    static public long formatToTicks(final String format){ //Con el formato (Semana)w(Dia)d(Hora)h(Minutos)m(Segundos)s
        Pattern pat = Pattern.compile("((?<SEMANAS>\\d+)w)?((?<DIAS>\\d+)d)?((?<HORAS>\\d+)h)?((?<MINUTOS>\\d+)m)?((?<SEGUNDOS>\\d+)s)?");
        long retorno = 0;
        Matcher matcher = pat.matcher(format);
        while (matcher.find()){
            for(String elem: groupNamesAndTicks.keySet()){
                if(matcher.group(elem) == null) continue;
                retorno += Integer.parseInt(matcher.group(elem))*groupNamesAndTicks.get(elem);
            }
        }
        return  retorno;
    }

    static public boolean isCloseTo(final Location from, final Location to, final int maxDistance){
        return (Math.sqrt(NumberConversions.square(to.getX() - from.getX()) + NumberConversions.square(to.getY() - from.getY()) + NumberConversions.square(to.getZ() - from.getZ())) < maxDistance);
    }

    static public String devolverSeparadoConComasConUUID(final List<Player> cadena){
        StringBuilder builder = new StringBuilder();
        int i = cadena.size();
        for(Player elem : cadena){
            i--;
            if(builder.length() != 0)
                builder.append(" ");
            builder.append(elem.getName()).append(" (").append(elem.getUniqueId()).append(")");
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

    static public String devolverSeparadoConComas(final List<Player> cadena){
        StringBuilder builder = new StringBuilder();
        int i = cadena.size();
        for(Player elem : cadena){
            i--;
            if(builder.length() != 0)
                builder.append(" ");
            builder.append(elem.getDisplayName());
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

    /**
     * Parámetros: Los dos stacks de items que se quieren stackear
     * Retorna: Los items stackeados (Una lista con 1 o 2 stacks de items)
     * */
    static public List<ItemStack> stackItems(final ItemStack stack1, final ItemStack stack2){
        List<ItemStack> items = new ArrayList<>();

        if(stack1.getType() != stack2.getType()){
            items.add(stack1);
            items.add(stack2);
            return items;
        }

        if(stack1.getAmount() + stack2.getAmount() > stack1.getMaxStackSize()){
            stack2.setAmount(stack1.getAmount()+stack2.getAmount()-stack1.getMaxStackSize());
            stack1.setAmount(stack1.getMaxStackSize());
            items.add(stack1);
            items.add(stack2);
        }else {
            stack1.setAmount(stack1.getAmount()+stack2.getAmount());
            items.add(stack1);
        }
        return items;
    }

    static public boolean canBeStacked(ItemStack item){
        return item.getAmount() < item.getMaxStackSize();
    }
}
