package me.griphion.DragonRewards.DragonPackage;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.griphion.DragonRewards.Core;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;

public class HologramManager {

    private final Core plugin;
    static private HashMap<World, Hologram> hologramas;

    public HologramManager(Core plugin){
        this.plugin = plugin;
        hologramas = new HashMap<>();
        detectHolograms();
    }

    public void clearHolograms(){
        for (Hologram holo: hologramas.values()){
            holo.delete();
        }
        hologramas.clear();
        hologramas = null;
    }

    public void detectHolograms(){
        Collection<Hologram> holograms = HologramsAPI.getHolograms(plugin);
        if(holograms == null) return;
        for (Hologram holo : holograms){
            if(hologramas.containsKey(holo.getWorld())){ //Cuidado que NO puede haber mas de un holograma por mundo!
                holo.delete();
                continue;
            }
            hologramas.put(holo.getWorld(),holo);
        }
    }

    static public void agregarHolograma(final World world, final Hologram hologram){
        if(hologramas.containsKey(world)){
            eliminarHolograma(world);
        }
        hologramas.put(world,hologram);
    }

    static public void eliminarHolograma(final World world){
        if(!hologramas.containsKey(world)) return;
        hologramas.get(world).delete();
        hologramas.remove(world);
    }
}
